# Command Line Execution plug-in for [Kobalt](http://beust.com/kobalt/home/index.html)

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause) [![release](https://img.shields.io/github/release/ethauvin/kobalt-exec.svg)](https://github.com/ethauvin/kobalt-exec/releases/latest) [![Build Status](https://travis-ci.org/ethauvin/kobalt-exec.svg?branch=master)](https://travis-ci.org/ethauvin/kobalt-exec) [![CircleCI](https://circleci.com/gh/ethauvin/kobalt-exec/tree/master.svg?style=shield)](https://circleci.com/gh/ethauvin/kobalt-exec/tree/master) [![Download](https://api.bintray.com/packages/ethauvin/maven/kobalt-exec/images/download.svg)](https://bintray.com/ethauvin/maven/kobalt-exec/_latestVersion)

The plug-in allows for the execution of system commands, similarly to the [Gradle Exec](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Exec.html) or [Ant Exec](https://ant.apache.org/manual/Tasks/exec.html) tasks.

To use the plug-in include the following in your `Build.kt` file:

```kotlin
import net.thauvin.erik.kobalt.plugin.exec.*

val bs = buildScript {
    plugins("net.thauvin.erik:kobalt-exc:")
}

val p = project {
    name = "example"

    exec {
       commandLine("echo", "Hello, World!")
    }
}
```
[View Example](https://github.com/ethauvin/kobalt-exec/blob/master/example/kobalt/src/Build.kt)

To invoke the `exec` task:

```sh
./kobaltw exec
```

## `commandLine` Directive

The `commandLine` directive is used to execute command line(s) during the build process:

```kotlin
exec {
    commandLine("cmd", "/c", "stop.bat", dir = "../tomcat/bin", os = setOf(Os.WINDOWS))
    commandLine("./stop.sh", dir = "../tomcat/bin", os = setOf(Os.MAC, Os.LINUX))
    commandLine("sh", "-c", "ps aux | grep tomcat", os = setOf(Os.MAC, Os.LINUX), fail = setOf(Fail.EXIT))
    commandLine("cmd", "/c", "tasklist | find \"tomcat\"", os = setOf(Os.WINDOWS), fail = setOf(Fail.EXIT))
}
```

## Parameters

### `args`

The full command line including the executable and all arguments.

```kotlin
exec {
    commandLine(args = "ls")
    commandLine("ls", "-l")
    commandLine("cmd", "/c", "dir /Q")
}
```

### `dir`

The working directory in which the command should be executed. Defaults to the project directory.

```kotlin
exec {
    commandLine("cmd", "/c", "stop.bat", dir = "../tomcat/bin")
    commandLine("./stop.sh", dir = "../tomcat/bin")
}
```

### `os`

List of operating systems on which the command may be executed. If the current OS is contained within the list, the command will be executed.

The following predefined values are available:

Name          | Operating System
:-------------|:-------------------------
`Os.CYGWIN`   | Cygwin for Windows
`Os.FREEBSD`  | FreeBSD
`Os.LINUX`    | Linux
`Os.MAC`      | Apple Macintosh / OS X
`Os.MINGW`    | Minimalist GNU for Windows
`OS.MSYS`     | MinGW Minimal System
`Os.OPENVMS`  | OpenVMS
`Os.OS400`    | OS/400
`Os.SOLARIS`  | Solaris / SunOS
`Os.TANDEM`   | Tandem's Non-Stop
`Os.WINDOWS`  | Microsoft Windows*
`Os.ZOS`      | z/OS / OS/390

<sub>* Excluding Cygwin, MinGW and MSYS.</sub>

```kotlin
exec {
    commandLine("cmd", "/c", "stop.bat", os = setOf(Os.WINDOWS))
    commandLine("./stop.sh", os = setOf(Os.LINUX, Os.MAC))
}
```

### `fail`

List of error options to specify whether data returned to the standard streams and/or an abnormal exit value constitute build failure signaling.

The following predefined values are available:

Name          | Failure When
:-------------|:----------------------------------------------------------------
`Fail.EXIT`   | Exit value > 0
`Fail.NORMAL` | Exit value > 0 or any data to the standard error stream (stderr)
`Fail.OUTPUT` | Any data to the standard output stream (stdout) or stderr.
`Fail.STDERR` | Any data to stderr.
`Fail.STDOUT` | Any data to stdout.
`Fail.ALL`    | Any of the conditions above.
`Fail.NONE`   | Never fails.

`Fail.NORMAL` is the default value.

```kotlin
exec {
    commandLine("cmd", "/c", "stop.bat", fail = setOf(Fail.EXIT))
    commandLine("./stop.sh", fail = setOf(Fail.EXIT, Fail.STDOUT))
}
```

## taskName

Additionally, you can specify a task name to easily identify multiple `exec` tasks.

```kotlin
exec {
    taskName = "start"
    commandLine("./start.sh", os = setOf(Os.LINUX, Os.MAC))
}

exec {
    taskName = "stop"
    commandLine("./stop.sh", os = setOf(Os.LINUX, Os.MAC))
}
```

```sh
./kobaltw start
./kobaltw stop
```

## dependsOn


By default the `exec` task depends on `assemble`, use the `dependsOn` parameter to change the dependencies:

```kotlin
exec {
    dependsOn = listOf("assemble", "run")
    commandLine("cmd", "/c", "start.bat", fail = setOf(Fail.EXIT))
}
```

## Logging / Debugging

To view the output of the `exec` task, use:

```sh
./kobaltw exec --log 2
```
You could also redirect the error stream to a file:

```kotlin
exec {
    commandLine("sh", "-c", "./stop.sh 2> error.txt", os = setOf(Os.LINUX))
    commandLine("cmd", "/c", "stop.bat 2> error.txt", os = setOf(Os.WINDOWS))
}
```