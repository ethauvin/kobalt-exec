import com.beust.kobalt.*
import com.beust.kobalt.plugin.application.*
import com.beust.kobalt.plugin.packaging.*
import net.thauvin.erik.kobalt.plugin.exec.*

// ./kobaltw exec echo ps --log 2
// ./kobaltw exec --log 2
// ./kobaltw echo --log 2
// ./kobaltw ps --log 2

val bs = buildScript {
    repos(file("K:/maven/repository"))
    plugins("net.thauvin.erik:kobalt-exec:0.6.6")
}

val example = project {

    name = "example"
    group = "com.example"
    artifactId = name
    version = "0.1"

    assemble {
        jar {
        }
    }

    application {
        mainClass = "com.example.Main"
    }

    exec {
        commandLine("echo", "Test Example 1", os = setOf(Os.LINUX))
        commandLine("cmd", "/c", "echo", "Test Example 1", os = setOf(Os.WINDOWS))
        commandLine("ls", "-l", dir = "../libs", os = setOf(Os.LINUX))
        commandLine("cmd", "/c", "dir /Q", dir = "../libs", os = setOf(Os.WINDOWS))
    }

    exec {
        taskName = "echo"
        dependsOn = listOf("exec", "run")
        commandLine("cmd", "/c", "echo", "Test Example 2", os = setOf(Os.WINDOWS))
        commandLine("echo", "Test example 2", os = setOf(Os.LINUX))
    }

    exec {
        taskName = "ps"
        dependsOn = listOf() // no dependencies
        commandLine("cmd", "/c", "tasklist | find \"cmd.exe\"", os = setOf(Os.WINDOWS), fail = setOf(Fail.NONE))
        commandLine("/bin/sh", "-c", "ps aux | grep bash", os = setOf(Os.LINUX))
    }
}