# jhwbus
Java hardware bus access for Linux

A simple, thin JNI based Java library to access simple hardware buses (for e.g. I2C) from Java on Linux. The library jar brings along the native library (`libjhwbus.so`) and dynamically loads it at runtime. Since Linux only supports simple hardware buses directly on ARM, this library is **only supported for Linux on ARM machines**.

Currently, supports the following hardware buses:

- I2C/SMBUS - [Linux I2C](https://www.kernel.org/doc/html/latest/i2c/index.html)

## Building

The JNI library is written in C and uses the `i2c-dev` interface to access the I2C bus. The library is built using the `gcc` compiler.

The default `./gradlew` build task will build and package the library into a jar file.

On a native Linux OS (running on an ARM machine), the build task can be run directly. On non-Linux OS, the build task can be run in a containerized environment using Docker.

The Dockerfile used for the containerized build is [available here](Dockerfile)

### Requirements

#### On Linux

- Java JDK8
- gcc
- [libi2c-dev](https://www.kernel.org/doc/html/latest/i2c/dev-interface.html) - `sudo apt-get install libi2c-dev`

> Ensure that the environment variable `JAVA_HOME` is defined and points the Java installation.

#### On non-Linux (macos)

- Container runtime like [Docker Desktop](https://www.docker.com/products/docker-desktop) or [Colima](https://github.com/abiosoft/colima)

Since we need to build the native library for ARM architecture, the container runtime has to either run on an ARM machine or support ARM emulation.

### Commands

`./gradlew` : Generates a jar with the native library included in `build/libs`

### Using

The library is published to GitHub Packages and can be included in the project using the following `build.gradle` configuration:

```groovy
repositories {
    maven {
        name = "GitHubPackages-jhwbus"
        url = uri("https://maven.pkg.github.com/jhwbus/jhwbus")
    }
}

dependencies {
    implementation 'io.github.jhwbus:jhwbus:1.2.0'
}
```

## License

BSD 3-Clause License

See [LICENSE](/LICENSE) file.
