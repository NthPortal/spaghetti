# spaghetti

![Build Status](https://img.shields.io/github/workflow/status/NthPortal/spaghetti/Continuous%20Integration?logo=github&style=for-the-badge)
[![Maven Central](https://img.shields.io/maven-central/v/lgbt.princess/spaghetti_3?logo=apache-maven&style=for-the-badge)](https://mvnrepository.com/artifact/lgbt.princess/spaghetti)
[![Versioning](https://img.shields.io/badge/versioning-semver%202.0.0-blue.svg?style=for-the-badge)](http://semver.org/spec/v2.0.0.html)
[![Docs](https://www.javadoc.io/badge2/lgbt.princess/spaghetti_3/docs.svg?color=blue&style=for-the-badge)](https://www.javadoc.io/doc/lgbt.princess/spaghetti_3)

[comment]: <> ([![Coverage Status]&#40;https://img.shields.io/coveralls/github/NthPortal/spaghetti/main?logo=coveralls&style=for-the-badge&#41;]&#40;https://coveralls.io/github/NthPortal/spaghetti?branch=main&#41;)

A Scala 3 implementation of goto.

## ***DO NOT USE THIS LIBRARY IN PRODUCTION***

This library is intended only for testing against existing algorithms
and implementations that use `goto`. It is not efficient, and is a
bad way to program in general.

## Add to Your sbt Build

**Scala 3**

```sbtshell
libraryDependencies += "lgbt.princess" %% "spaghetti" % "0.1.0" % Test
```
