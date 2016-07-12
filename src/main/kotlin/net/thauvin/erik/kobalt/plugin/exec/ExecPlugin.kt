/*
 * ExecPlugin.kt
 *
 * Copyright (c) 2016, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.thauvin.erik.kobalt.plugin.exec

import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.*
import com.beust.kobalt.api.annotation.Directive
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.misc.log
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class ExecPlugin : BasePlugin(), ITaskContributor {
    // ITaskContributor
    override fun tasksFor(project: Project, context: KobaltContext): List<DynamicTask> {
        return emptyList()
    }

    companion object {
        const val NAME: String = "Exec"
    }

    override val name = NAME

    @Task(name = "exec", description = "Execute a command line process.")
    fun taskExec(project: Project): TaskResult {
        return executeCommands(project)
    }

    private fun executeCommands(project: Project): TaskResult {
        var success = true
        val config = configs[project.name]
        val errorMessage = StringBuilder()

        if (config != null) {
            for ((args, dir, os, fail) in config.commandLines) {
                val wrkDir = File(if (dir.isNullOrBlank()) project.directory else dir)
                if (wrkDir.isDirectory) {
                    var execute = (os.size == 0)
                    if (!execute) {
                        val curOs: String = System.getProperty("os.name")
                        for (name in os) {
                            execute = curOs.startsWith(name, true)
                            if (execute) break
                        }
                    }
                    if (execute) {
                        log(2, "> " + args.joinToString(" "))
                        val pb = ProcessBuilder().command(args.toList())
                        pb.directory(wrkDir)
                        val proc = pb.start()
                        val err = proc.waitFor(30, TimeUnit.SECONDS)
                        val stdout = if (proc.inputStream.available() > 0) fromStream(proc.inputStream) else emptyList()
                        val stderr = if (proc.errorStream.available() > 0) fromStream(proc.errorStream) else emptyList()
                        val cmdInfo = "Program \"" + args.joinToString(" ") + "\" (in directory \"${wrkDir.path}\"): "

                        if (err == false) {
                            errorMessage.append(cmdInfo).append("TIMEOUT")
                            success = false
                        } else if (fail.isNotEmpty()) {
                            val all = fail.contains(Fail.ALL)
                            val output = fail.contains(Fail.OUTPUT)
                            if ((all || fail.contains(Fail.EXIT) || fail.contains(Fail.NORMAL)) && proc.exitValue() > 0) {
                                errorMessage.append(cmdInfo).append("EXIT ${proc.exitValue()}")
                                if (stderr.isNotEmpty()) errorMessage.append(", STDERR: ").append(stderr[0])
                                success = false
                            } else if ((all || output || fail.contains(Fail.STDERR) || fail.contains(Fail.NORMAL))
                                    && stderr.isNotEmpty()) {
                                errorMessage.append(cmdInfo).append("STDERR, ").append(stderr[0])
                                success = false
                            } else if ((all || output || fail.contains(Fail.STDOUT)) && stdout.isNotEmpty()) {
                                errorMessage.append(cmdInfo).append("STDOUT, ").append(stdout[0])
                                success = false
                            }
                        }
                    }
                } else {
                    errorMessage.append("Invalid working directory: \"${wrkDir.canonicalPath}\"")
                    success = false
                }

                if (!success) break
            }
        }

        //@TODO until cedric fixes it.
        if (!success) error(errorMessage)

        return TaskResult(success, errorMessage.toString())
    }

    private val configs = hashMapOf<String, ExecConfig>()
    fun addExecConfig(projectName: String, config: ExecConfig) {
        configs.put(projectName, config)
    }

    private fun fromStream(ins: InputStream): List<String> {
        val result = arrayListOf<String>()
        val br = BufferedReader(InputStreamReader(ins))
        var line = br.readLine()

        while (line != null) {
            result.add(line)
            log(2, line)
            line = br.readLine()
        }
        log(2, "")

        return result
    }
}

enum class Fail() {
    ALL, NORMAL, STDERR, STDOUT, OUTPUT, EXIT
}

data class CommandLine(var args: List<String> = emptyList(), var dir: String = "",
                       var os: List<String> = emptyList(), var fail: Set<Fail> = emptySet())

data class ExecConfig(val project: Project) {
    val commandLines = arrayListOf<CommandLine>()

    @Directive fun commandLine(args: List<String> = emptyList(), dir: String = "", os: List<String> = emptyList(),
                               fail: Set<Fail> = emptySet()) {
        if (args.size > 0) commandLines.add(CommandLine(args, dir, os, fail))
    }
}

@Directive
fun Project.exec(init: ExecConfig.() -> Unit): ExecConfig {
    with(ExecConfig(this)) {
        init()
        (Kobalt.findPlugin(ExecPlugin.NAME) as ExecPlugin).addExecConfig(name, this)
        return this
    }
}