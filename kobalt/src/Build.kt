
import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.autoGitTag
import com.beust.kobalt.plugin.publish.bintray
import net.thauvin.erik.kobalt.plugin.versioneye.versionEye
import org.apache.maven.model.*

val bs = buildScript {
    plugins("net.thauvin.erik:kobalt-versioneye:")
}

val dev by profile()
val kobaltDependency = if (dev) "kobalt" else "kobalt-plugin-api"

val p = project {

    name = "kobalt-exec"
    group = "net.thauvin.erik"
    artifactId = name
    version = "0.6.4"

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