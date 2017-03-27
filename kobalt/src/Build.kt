import com.beust.kobalt.*
import com.beust.kobalt.misc.*
import com.beust.kobalt.plugin.application.*
import com.beust.kobalt.plugin.packaging.*
import com.beust.kobalt.plugin.publish.*
import net.thauvin.erik.kobalt.plugin.exec.*
import net.thauvin.erik.kobalt.plugin.versioneye.*
import org.apache.maven.model.*
import java.io.*

val semver = "0.6.3"

val bs = buildScript {
    val p = with(File("kobaltBuild/libs/kobalt-exec-$semver.jar")) {
        if (exists()) {
            kobaltLog(1, "  >>> Using: $path")
            file(path)
        } else {
            "net.thauvin.erik:kobalt-exec:"
        }
    }
    plugins("net.thauvin.erik:kobalt-versioneye:", p)
}

val dev by profile()
val kobaltDependency = if (dev) "kobalt" else "kobalt-plugin-api"

val p = project {

    name = "kobalt-exec"
    group = "net.thauvin.erik"
    artifactId = name
    version = semver

    pom = Model().apply {
        description = "Command Line Execution plug-in for the Kobalt build system."
        url = "https://github.com/ethauvin/kobalt-exec"
        licenses = listOf(License().apply {
            name = "BSD 3-Clause"
            url = "https://opensource.org/licenses/BSD-3-Clause"
        })
        scm = Scm().apply {
            url = "https://github.com/ethauvin/kobalt-exec"
            connection = "https://github.com/ethauvin/kobalt-exec.git"
            developerConnection = "git@github.com:ethauvin/kobalt-exec.git"
        }
        developers = listOf(Developer().apply {
            id = "ethauvin"
            name = "Erik C. Thauvin"
            email = "erik@thauvin.net"
        })
    }

    dependencies {
        compile("com.beust:$kobaltDependency:")
    }

    dependenciesTest {
        compile("org.testng:testng:")

    }

    assemble {
        mavenJars {}
    }

    autoGitTag {
        enabled = true
        message = "Version $version"
    }

    bintray {
        publish = true
        description = "Release version $version"
        vcsTag = version
    }

    versionEye {
        org = "Thauvin"
        team = "Owners"
    }
}

val example = project(p) {

    name = "example"
    group = "com.example"
    artifactId = name
    version = "0.1"
    directory = ("example")

    assemble {
        jar {
        }
    }

    application {
        mainClass = "com.example.Main"
    }

    exec {
        commandLine(listOf("echo", "Test Example 1"), os = setOf(Os.LINUX))
        commandLine(listOf("cmd", "/c", "echo", "Test Example 1"), os = setOf(Os.WINDOWS))
        commandLine(args = listOf("ls", "-l"), dir = "../libs", os = setOf(Os.LINUX))
        commandLine(args = listOf("cmd", "/c", "dir /Q"), dir = "../libs", os = setOf(Os.WINDOWS))
    }
}

val example2 = project(p) {
    name = "example2"
    directory = "example"

    exec {
        commandLine(listOf("cmd", "/c", "echo", "Test Example 2"), os = setOf(Os.WINDOWS))
        commandLine(listOf("echo", "Test example 2"), os = setOf(Os.LINUX))
        //commandLine(listOf("cmd", "/c", "tasklist | find \"cmd.exe\""), os = setOf(Os.WINDOWS), fail = setOf(Fail.NONE))
        commandLine(listOf("/bin/sh", "-c", "ps aux | grep bash"), os = setOf(Os.LINUX))
    }
}
