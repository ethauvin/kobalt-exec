/*
 * ExecPlugin.kt
 *
 * Copyright (c) 2016-2017, Erik C. Thauvin (erik@thauvin.net)
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

import com.beust.kobalt.Plugins
import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.*
import com.beust.kobalt.api.annotation.Directive
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.misc.log
import com.google.inject.Inject
import com.google.inject.Singleton
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit

@Singleton
class ExecPlugin @Inject constructor(val configActor: ConfigActor<ExecConfig>) :
        BasePlugin(), ITaskContributor, IConfigActor<ExecConfig> by configActor {
    // ITaskContributor
    override fun tasksFor(project: Project, context: KobaltContext): List<DynamicTask> {
        return emptyList()
    }

    companion object {
        const val NAME: String = "Exec"
    }

    override val name = NAME

    @Suppress("unused")
    @Task(name = "exec", description = "Execute a command line process.")
    fun taskExec(project: Project): TaskResult {
        configurationFor(project)?.let { config ->
            return executeCommands(project, config)
        }

        return TaskResult()
    }

    private fun matchOs(os: Os): Boolean {
        val curOs: String = System.getProperty("os.name").toLowerCase(Locale.US)
        when (os) {
            Os.WINDOWS -> {
                return curOs.contains("windows")
            }
            Os.MAC -> {
                return (curOs.contains("mac") || curOs.contains("darwin") || curOs.contains("osx"))
            }
            Os.LINUX -> {
                return curOs.contains("linux")
            }
            Os.FREEBSD -> {
                return curOs.contains("freebsd")
            }
            Os.SOLARIS -> {
                return (curOs.contains("sunos") || curOs.contains("solaris"))
            }
            Os.OPENVMS -> {
                return curOs.contains("openvms")
            }
            Os.ZOS -> {
                return (curOs.contains("z/os") || curOs.contains("os/390"))
            }
            Os.TANDEM -> {
                return curOs.contains("nonstop_kernel")
            }
            Os.OS400 -> {
                return curOs.contains("os/400")
            }
        }
    }

    private fun executeCommands(project: Project, config: ExecConfig): TaskResult {
        var success = true
        val errorMessage = StringBuilder()

        for ((args, dir, os, fail) in config.commandLines) {
            val wrkDir = File(if (dir.isNullOrBlank()) project.directory else dir)
            if (wrkDir.isDirectory) {
                var execute = (os.isEmpty())
                if (!execute) {
                    for (name in os) {
                        execute = matchOs(name)
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

                    if (!err) {
                        errorMessage.append(cmdInfo).append("TIMEOUT")
                        success = false
                    } else if (!fail.contains(Fail.NONE)) {
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

        //@TODO until Cedric fixes it.
        if (!success) error(errorMessage)

        return TaskResult(success)
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

enum class Fail {
    ALL, EXIT, NONE, NORMAL, OUTPUT, STDERR, STDOUT
}

enum class Os {
    FREEBSD, LINUX, MAC, OPENVMS, OS400, SOLARIS, TANDEM, WINDOWS, ZOS
}

data class CommandLine(var args: List<String> = emptyList(), var dir: String = "", var os: Set<Os> = emptySet(),
                       var fail: Set<Fail> = setOf(Fail.NORMAL))

@Directive
class ExecConfig {
    val commandLines = arrayListOf<CommandLine>()

    @Suppress("unused")
    fun commandLine(args: List<String> = emptyList(), dir: String = "", os: Set<Os> = emptySet(),
                    fail: Set<Fail> = setOf(Fail.NORMAL)) {
        if (args.isNotEmpty()) commandLines.add(CommandLine(args, dir, os, fail))
    }
}

@Suppress("unused")
@Directive
fun Project.exec(init: ExecConfig.() -> Unit) {
    ExecConfig().let { config ->
        config.init()
        (Plugins.findPlugin(ExecPlugin.NAME) as ExecPlugin).addConfiguration(this, config)
    }
}