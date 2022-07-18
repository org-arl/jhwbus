# jhwbus
Java hardware bus access

A simple, thin JNI based Java library to enable accessing hardware buses (for eg. I2C) from Java on Linux. Bundles the native library into the jar and dynamically loads it at runtime.

Currently supports the following hardware:

- I2C/SMBUS

Currently supports the following operating systems:

- Linux
- macOS (dummy support for testing only)

## Building

### Requirements

- Java
- gcc
- gradle

Ensure that the environment variable `JAVA_HOME` is defined and points the Java installation.

### Commands

`gradle` : Generates a jar with the native library included in `build/libs`

### Using

Since the library is very platform (OS and architecture) specific, it is recommended that you add it as [gradle source dependency](https://blog.gradle.org/introducing-source-dependencies) instead of a normal gradle dependency, so that it gets compiled on your platform.

In `settings.gradle` : 

```groovy
sourceControl {
  gitRepository("https://github.com/org-arl/jhwbus.git") {
    producesModule("org.arl.jhwbus:jhwbus")
  }
}
```

In `build.gradle` : 
```groovy
 implementation('org.arl.jhwbus:jhwbus:1.1.1') {
    version {
        branch = 'gradle'
    }
}
```

## License

BSD 3-Clause License

See [LICENSE](/LICENSE) file.
