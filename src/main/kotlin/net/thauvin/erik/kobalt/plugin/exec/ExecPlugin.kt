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

class ExecPlugin : BasePlugin(), ITaskContributor {

/*
    fun main(argv: Array<String>) {
        com.beust.kobalt.main(argv)
    }
*/

    // ITaskContributor
    override fun tasksFor(project: Project, context: KobaltContext): List<DynamicTask> {
        return emptyList()
    }


    companion object {
        const val NAME: String = "kobalt-exec"
    }

    override val name = NAME

    @Task(name = "exec", description = "Executes a command line process.")
    fun taskExec(project: Project): TaskResult {
        return executeCommands(project)
    }

    private fun executeCommands(project: Project): TaskResult {
        var success = false
        val config = configs[project.name]

        if (config != null) {
            config.commandLines.forEach {
                log(2, "Executing: '" + { it.args.joinToString { " " } } + "' in '{$it.dir}'")
                success = true
            }
        }

        return TaskResult(success)
    }

    private val configs = hashMapOf<String, ExecConfig>()
    fun addExecConfig(projectName: String, config: ExecConfig) {
        configs.put(projectName, config)
    }
}

data class CommandLine(var os: String = "", var dir: String = "", var args: Array<String> = emptyArray())

data class ExecConfig(val project: Project) {
    val commandLines = arrayListOf<CommandLine>()

    @Directive
    public fun commandLine(os: String = "", dir: String = "", args: Array<String> = emptyArray()) {
        if (args.size > 0) commandLines.add(CommandLine(os, dir, args))
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