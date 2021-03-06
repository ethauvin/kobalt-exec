import com.beust.kobalt.*
import com.beust.kobalt.plugin.application.*
import com.beust.kobalt.plugin.packaging.*
import net.thauvin.erik.kobalt.plugin.exec.*

// ./kobaltw exec echo ps --log 2
// ./kobaltw exec --log 2
// ./kobaltw echo --log 2
// ./kobaltw ps --log 2

val bs = buildScript {
    repos(localMaven())
    plugins("net.thauvin.erik:kobalt-exec:")
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
        commandLine("ls", "-l", dir = "../kobalt/wrapper", os = setOf(Os.LINUX, Os.MINGW, Os.CYGWIN))
        commandLine("cmd", "/c", "dir /Q", dir = "../kobalt/wrapper", os = setOf(Os.WINDOWS))
    }

    exec {
        taskName = "echo"
        dependsOn = listOf("exec", "run")

        val echo = arrayOf("echo", "Test", "Example")
        commandLine(*echo, os = setOf(Os.LINUX, Os.MINGW, Os.CYGWIN))
        commandLine("cmd", "/c", *echo, os = setOf(Os.WINDOWS))
    }

    exec {
        taskName = "ps"
        dependsOn = listOf() // no dependencies
        commandLine("sh", "-c", "ps aux | grep bash", os = setOf(Os.LINUX, Os.MINGW, Os.CYGWIN))
        commandLine("cmd", "/c", "tasklist | find \"cmd.exe\"", os = setOf(Os.WINDOWS), fail = setOf(Fail.EXIT))
    }
}