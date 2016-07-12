import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project
import com.beust.kobalt.repos

val repos = repos()

val dev = false
val kobaltDependency = if (dev) "kobalt" else "kobalt-plugin-api"

val p = project {

    name = "kobalt-exec"
    group = "net.thauvin.erik"
    artifactId = name
    version = "0.5.0-beta"

    sourceDirectories {
        path("src/main/kotlin")
    }

    sourceDirectoriesTest {
        path("src/test/kotlin")
    }

    dependencies {
        compile("com.beust:$kobaltDependency:0.842")
    }

    dependenciesTest {
        compile("org.testng:testng:")

    }

    assemble {
        mavenJars {
        }
    }

    bintray {
        publish = false
    }
}
