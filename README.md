# jhwbus
Java hardware bus access

A simple, thin JNI based Java library to enable accessing hardware buses (for eg. I2C) from Java on Linux. Bundles the native library into the jar and dynamically loads it at runtime.

Currently supports:

- I2C/SMBUS

## Building

### Requirements

- Java
- gcc

Ensure that the environment variable `JAVA_HOME` is defined and points the Java installation.

### Commands

`make jar` : Generates a jar with the native library in `build/libs`

## License

BSD 3-Clause License

See [LICENSE](/LICENSE) file.
