# Command Line Execution plug-in for [Kobalt](http://beust.com/kobalt/home/index.html)

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause) [![Build Status](https://travis-ci.org/ethauvin/kobalt-exec.svg?branch=master)](https://travis-ci.org/ethauvin/kobalt-exec)

```kotlin
var pl = plugins("net.thauvin.erik:kobalt-exc:")

var p = project {
    exec {
       commandLine(listOf("echo", "Hello, World!"))
    }
}
```

```sh
./kobaltw assemble exec
```