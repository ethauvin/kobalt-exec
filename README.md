# Command Line Execution plug-in for [Kobalt](http://beust.com/kobalt/home/index.html)

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause) [![Build Status](https://travis-ci.org/ethauvin/kobalt-exec.svg?branch=master)](https://travis-ci.org/ethauvin/kobalt-exec)

The plug-in allows for the execution of system commands, similarly to the [Gradle Exec](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Exec.html) or [Ant Exec](https://ant.apache.org/manual/Tasks/exec.html) tasks.

To use the plug-in included the following in your `Build.kt` file:

```kotlin
import net.thauvin.erik.kobalt.plugin.exec.*

var pl = plugins("net.thauvin.erik:kobalt-exc:")

var p = project {
    name = "example"
	
    exec {
       commandLine(listOf("echo", "Hello, World!"))
    }
}
```
[Examples](https://github.com/ethauvin/kobalt-exec/blob/master/example/kobalt/src/Build.kt)

To invoke the `exec` task:

```sh
./kobaltw exec
```

## `commandLine` Directive

The `commandLine` directive is used to execute command line(s) during the build process:

```kotlin
exec {
    commandLine(listOf("cmd", "/c", "stop.bat"), dir = "../tomcat/bin", os = setOf(Os.WINDOWS))
    commandLine(listOf("./stop.sh"), dir = "../tomcat/bin", os = setOf(Os.MAC, Os.LINUX))
    commandLine(listOf("/bin/sh", "-c", "ps aux | grep tomcat"), fail = setOf(Fail.EXIT))
}
```

## Parameters

### `args`

The full command line including the executable and all parameters.

```kotlin
exec {
    commandLine(listOf("ls", "-l"))
    commandLine(args = listOf("cmd", "/c", "dir /Q"))
}
```

### `dir`

The working directory in which the command should be executed. Defaults to the project directory.

```kotlin
exec {
    commandLine(listOf("cmd", "/c", "stop.bat"), dir = "../tomcat/bin")
    commandLine("./stop.sh", dir = "../tomcat/bin")
}
```

### `os`

List of operating systems on which the command may be executed. If the current OS is contained within the list, the command will be executed.

The following predefined values are available:

Name          | Operating System
--------------|-----------------------
`Os.FREEBSD`  | FreeBSD
`Os.LINUX`    | Linux
`Os.MAC`      | Apple Macintosh / OS X
`Os.OPENVMS`  | OpenVMS
`Os.OS400`    | OS/400
`Os.SOLARIS`  | Solaris / SunOS
`Os.TANDEM`   | Tandem's Non-Stop
`Os.WINDOWS`  | Microsoft Windows
`Os.ZOS`      | z/OS / OS/390

```kotlin
exec {
    commandLine(listOf("cmd", "/c", "stop.cmd"), os = setOf(Os.WINDOWS))
    commandLine(listOf("./stop.sh"), os = setOf(Os.LINUX, Os.MAC))
}
```

### `fail`

List of error options to specify whether data returned to the standard streams and/or an abnormal exit value constitute build failure signaling.

The following predefined values are available:

Name          | Failure When
--------------|-----------------------------------------------------------------
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
    commandLine(listOf("cmd", "/c", "stop.bat"), fail = setOf(Fail.EXIT))
    commandLine(listOf("./stop.sh"), fail = setOf(Fail.EXIT, Fail.STDOUT))
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
    commandLine(listOf("/bin/sh", "-c", "./stop.sh 2> error.txt"), os = setOf(Os.LINUX))
    commandLine(listOf("cmd", "/c", "stop.bat 2> error.txt"), os = setOf(Os.WINDOWS))
}
```



