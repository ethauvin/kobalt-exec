import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.*
import com.beust.kobalt.plugin.application.*
import com.beust.kobalt.plugin.java.*
import net.thauvin.erik.kobalt.plugin.exec.*

val repos = repos("https://dl.bintray.com/ethauvin/maven/")

val pl = plugins(file("../kobaltBuild/libs/kobalt-exec-0.5.1-beta.jar"))
//val pl = plugins("net.thauvin.erik:kobalt-exec:0.5.1-beta")

val example = project {

    name = "example"
    group = "com.example"
    artifactId = name
    version = "0.1"

    sourceDirectories {
        path("src/main/java")
    }

    sourceDirectoriesTest {
        path("src/test/java")
    }

    dependencies {
    }

    dependenciesTest {
    }

    assemble {
        jar {
        }
    }

    application {
        mainClass = "com.example.Main"
    }

    exec {
        commandLine(listOf("cmd", "/c", "echo", "Test Example 1"), os = setOf("Win"))
		commandLine(args = listOf("ls", "-l"), dir = "../libs", os = setOf("Linux", "Win"))
    }
}

val example2 = project {
    name = "example2"

    exec {
		commandLine(listOf("cmd", "/c", "echo", "Test Example 2"), os = setOf("Win"))
    }
}