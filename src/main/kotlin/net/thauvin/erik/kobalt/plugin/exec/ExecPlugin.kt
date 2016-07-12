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

        if (config != null) {
            config.commandLines.forEach {
                val dir = if (it.dir.isNullOrBlank()) project.directory else it.dir
                val loc = File(dir)
                if (loc.isDirectory()) {
                    var execute = (it.os.size == 0)
                    if (!execute) {
                        val curOs: String = System.getProperty("os.name")
                        it.os.forEach os@ {
                            execute = curOs.startsWith(it, true)
                            if (execute) return@os
                        }
                    }
                    if (execute) {
                        log(2, "> " + it.args.joinToString(" "))
                        val pb = ProcessBuilder().command(it.args.toList())
                        pb.directory(loc)
                        val proc = pb.start()
                        val err = proc.waitFor(30, TimeUnit.SECONDS)
                        val stdout = if (proc.inputStream.available() > 0) fromStream(proc.inputStream) else emptyList()
                        val stderr = if (proc.errorStream.available() > 0) fromStream(proc.errorStream) else emptyList()

                        val errMsg = "Program \"" + it.args.joinToString(" ") + "\" (in directory \"$dir\"): "

                        if (err == false) {
                            error(errMsg + "TIMEOUT")
                        } else if (it.fail.isNotEmpty()) {
                            val all = it.fail.contains(Fail.ALL)
                            val output = it.fail.contains(Fail.OUTPUT)
                            if ((all || it.fail.contains(Fail.EXIT)) && proc.exitValue() > 0) {
                                error(errMsg + "EXIT ${proc.exitValue()}")
                            } else if ((all || output || it.fail.contains(Fail.STDERR)) && stderr.isNotEmpty()) {
                                error(errMsg + "STDERR, " + stderr[0])
                            } else if ((all || output || it.fail.contains(Fail.STDOUT)) && stdout.isNotEmpty()) {
                                error(errMsg + "STDOUT, " + stdout[0])
                            }
                        }

                        success = true
                    }
                } else {
                    error("Invalid directory: ${loc.canonicalPath}")
                }
            }
        }

        return TaskResult(success)
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
    ALL, STDERR, STDOUT, OUTPUT, EXIT
}

data class CommandLine(var args: List<String> = emptyList(), var dir: String = "",
                       var os: List<String> = emptyList(), var fail: Set<Fail> = emptySet())

data class ExecConfig(val project: Project) {
    val commandLines = arrayListOf<CommandLine>()

    @Directive
    public fun commandLine(args: List<String> = emptyList(), dir: String = "", os: List<String> = emptyList(),
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