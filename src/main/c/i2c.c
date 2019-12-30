#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <inttypes.h>
#include <unistd.h>
#include <math.h>
#include <jni.h>

#ifndef DEBUG
#include <linux/i2c.h>
#include <linux/i2c-dev.h>
#include <linux/types.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#endif

#include "logging.h"

int Java_I2CDevice_I2COpen(JNIEnv* env, jobject obj, jstring dev, jint addr, jint flags){
    int fd = -1;
    const char* devName = (*env)->GetStringUTFChars(env, dev, NULL) ;
#ifdef DEBUG
    log_open("sentuator-i2c.txt", 64);
    info("Opening I2C device %s, addr : %02X", devName, addr);
    fd = 42;
#else
    fd = open(devName, i2cFlags);
    if (ioctl(fd, I2C_SLAVE, addr) < 0) {
        close(fd);
        return -1;
    }
#endif
    return fd;
}

int Java_I2CDevice_I2CReadByte(JNIEnv* env, jobject obj, int fd){
    #ifdef DEBUG
        info("Reading a byte");
        return 0;
    #else
        return i2c_smbus_read_byte(fd);
    #endif

}

int Java_I2CDevice_I2CWriteByte(JNIEnv* env, jobject obj, int fd, int data){
    #ifdef DEBUG
        info("Writing a byte %02X", data);
        return 0;
    #else
        return i2c_smbus_write_byte(fd, data);
    #endif
}

int Java_I2CDevice_I2CWriteByteData(JNIEnv* env, jobject obj, int fd, int cmd, int data){
    #ifdef DEBUG
        info("Writing a command %02X and byte data %02X", cmd, data);
        return 0;
    #else
        return i2c_smbus_write_byte_data(fd, cmd, data);
    #endif
}

int Java_I2CDevice_I2CWriteWordData(JNIEnv* env, jobject obj, int fd, int cmd, int data){
    #ifdef DEBUG
        info("Writing a command %02X and word data %04X", cmd, data);
        return 0;
    #else
        return i2c_smbus_write_word_data(fd, cmd, data);
    #endif
}

int Java_I2CDevice_I2CReadByteData(JNIEnv* env, jobject obj, int fd, int cmd){
    #ifdef DEBUG
        info("Reading a byte data with the command %02X", cmd);
        return 0;
    #else
        return i2c_smbus_read_byte_data(fd, cmd);
    #endif
}

int Java_I2CDevice_I2CReadWordData(JNIEnv* env, jobject obj, int fd, int cmd){
    #ifdef DEBUG
        info("Reading a word data with the command %02X", cmd);
        return 0;
    #else
        return i2c_smbus_read_word_data(fd, cmd);
    #endif
}

void Java_I2CDevice_I2CClose(JNIEnv* env, jobject obj, int fd){
    #ifdef DEBUG
        info("Closing the I2C Interface");
    #else
        close(fd);
    #endif
}
