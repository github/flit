# Flit - Twirp RPC Generator Framework

This project is a generator for the [Twitch TV Twirp](https://github.com/twitchtv/twirp "Twitch TV Twirp") RPC
framework.

It supports the generation of Java based servers with the following flavours supported:

+ [Spring Boot/Spring MVC](https://spring.io/projects/spring-boot "Spring Boot")
+ [Undertow](http://undertow.io/ "Undertow")
+ JAX-RS ([Jersey](https://eclipse-ee4j.github.io/jersey/), [Apache CFX](http://cxf.apache.org/))
+ [Jakarta EE](https://jakarta.ee/ "Jakarta")

# Contents

<!-- Generated with https://github.com/thlorenz/doctoc -->
<!-- doctoc README.md >

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Using](#using)
  - [Runtime libraries](#runtime-libraries)
  - [Protoc plugin](#protoc-plugin)
    - [via gradle](#via-gradle)
    - [via protoc](#via-protoc)
- [Development](#development)
  - [Requirements](#requirements)
  - [Modules](#modules)
  - [Gradle commands](#gradle-commands)
  - [Remote Debug](#remote-debug)
- [Guides](#guides)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Using

### Runtime libraries

Follow the [Working with the Gradle registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry) docs to set up authentication with GitHub packages.

[Published](https://github.com/orgs/github/packages?repo_name=flit) runtime libraries example Gradle setup:
```groovy
repositories {
    maven {
        name = 'GithubPackages'
        url 'https://maven.pkg.github.com/github/flit'
        credentials {
            username = project.findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
            password = project.findProperty('gpr.key') ?: System.getenv('GITHUB_TOKEN')
        }
        content {
            includeGroup('com.flit')
        }
    }
}

dependencies {
    implementation("com.flit:flit-core-runtime:<version>")
    implementation("com.flit:flit-spring-runtime:<version>")
    implementation("com.flit:flit-jaxrs-runtime:<version>")
    implementation("com.flit:flit-jakarta-runtime:<version>")
    implementation("com.flit:flit-undertow-runtime:<version>")
}
```

### Protoc plugin

Plugin `com.flit.flit-plugin` shadow jar can be downloaded from [here](https://github.com/github/flit/packages/1832284).

The flit plugin accepts the following plugin parameters:

| Name      | Required  | Type                                       | Description                                            |
|:----------|:---------:|:-------------------------------------------|:-------------------------------------------------------|
| `target`  | Y         | `enum[server]`                             | The type of target to generate e.g. server, client etc |
| `type`    | Y         | `enum[spring,undertow,boot,jakarta,jaxrs]` | Type of target to generate                             |
| `context` | N         | `string`                                   | Base context for routing, default is `/twirp`          |
| `request` | N         | `string`                                   | If the request parameter should pass to the service    |

#### via gradle

The plugin can be called from the Gradle protobuf plugin via additional configuration. For example:

```groovy
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protocVersion"
    }
  
    plugins {
        flit {
            path = "${projectDir}/script/protoc-gen-flit"
        }
    }

    generateProtoTasks {
        ofSourceSet('main')*.plugins {
            flit {
                option 'target=server'
                option 'type=spring'
                option 'request=Service'
            }
        }
    }
}
```
```shell
#!/usr/bin/env bash

DIR=$(dirname "$0")

JAR=$(ls -c ${DIR}/flit-plugin-*.jar | head -1)
java ${FLIT_JAVA_OPTS} -jar $JAR $@
```
Where the plugin jar is located in the same directly as the script.

#### via protoc

The plugin is executed as part of a protoc compilation step:

    protoc \
        --proto_path=. \
        --java_out=../java \
        --flit_out=target=server,type=undertow:../java \
        ./haberdasher.proto

## Development

### Requirements

The build has been tested with [Zulu's OpenJDK](https://www.azul.com/downloads/#zulu "JDK Downloads") version 17.

The build uses gradle to generate the artifacts. No installation is required as the project uses the
[gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html "gradle wrapper") setup.

Optional: to test you will need an installation of the [protocol buffers compiler](https://github.com/protocolbuffers/protobuf/releases "protobuf releases").

### Modules

The project is split into the following modules:

| Module             | Description                                   |
|:-------------------|:----------------------------------------------|
| `plugin`           | The `protoc` plugin                           |
| `runtime:core`     | Core functionality required by generated code |
| `runtime:jakarta`  | Runtime library for Jakarta servers           |
| `runtime:jaxrs`    | Runtime library for JAX-RS servers            |
| `runtime:spring`   | Runtime library for Spring MVC/Boot servers   |
| `runtime:undertow` | Runtime library for Undertow servers          |

### Gradle commands

* clean `./gradlew clean`
* build `./gradlew build`
* test `./gradlew test`
* publish `./gradlew publish`

### Remote Debug

Use remote JVM debugging by setting:

```shell
export FLIT_JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y"
```

## Guides

| Platform  | Document                              |
|:----------|:--------------------------------------|
| Undertow  | [undertow.md](docs/undertow.md)       |
