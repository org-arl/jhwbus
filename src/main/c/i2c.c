#include <sys/time.h>
#include <inttypes.h>
#include <jni.h>

#ifdef __linux__
    #include <linux/i2c-dev.h>
    #include <i2c/smbus.h>
    #include <sys/ioctl.h>
    #include <fcntl.h>
    #include <unistd.h>
#endif

#ifdef DEBUG
static inline uint64_t current_time_ms() {
  struct timeval te;
  gettimeofday(&te, NULL);
  return te.tv_sec*1000ll + te.tv_usec/1000;
}
#endif

int log_open(const char* filename) {
#ifdef DEBUG
  if (freopen(filename, "a", stderr) == NULL) return -1;
#endif
  return 0;
}

void log_info(const char *format, ...) {
#ifdef DEBUG
    va_list args;
    va_start(args, format);
    fprintf(stderr, "%" PRIu64 "|INFO|%s:%d|", current_time_ms(), __FILE__, __LINE__);
    vfprintf(stderr, format, args);
    fprintf(stderr, "\n");
    fflush(stderr);
    va_end(args);
#endif
}

int Java_org_arl_jhwbus_I2CDevice_I2COpen(JNIEnv* env, jobject obj, jstring dev, jint addr){
    int fd = 42;
    const char* devName = (*env)->GetStringUTFChars(env, dev, NULL) ;
    log_open("jhwbus-i2c.txt");
    log_info("Opening I2C device %s", devName);
#ifdef __linux__
    fd = open(devName, O_RDWR);
#endif
    return fd;
}

int Java_org_arl_jhwbus_I2CDevice_I2CSetAddr(JNIEnv* env, jobject obj, jint fd, jbyte addr){
    log_info("Setting I2C addr 0x%02X", addr);
    int rv = 0;
    #ifdef __linux__
        rv = ioctl(fd, I2C_SLAVE, addr);
    #endif
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CReadByte(JNIEnv* env, jobject obj, jint fd){
    int rv = 0;
    log_info("Reading a byte");
    #ifdef __linux__
        rv = i2c_smbus_read_byte(fd);
    #endif
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CWriteByte(JNIEnv* env, jobject obj, jint fd, jbyte data){
    int rv = 0;
    log_info("Writing a byte 0x%02X", data);
    #ifdef __linux__
        rv = i2c_smbus_write_byte(fd, data);
    #endif
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CWriteByteData(JNIEnv* env, jobject obj, jint fd, jbyte cmd, jbyte data){
    int rv = 0;
    log_info("Writing a command 0x%02X and byte data 0x%02X", cmd, data);
    #ifdef __linux__
        rv = i2c_smbus_write_byte_data(fd, cmd, data);
    #endif
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CWriteWordData(JNIEnv* env, jobject obj, jint fd, jbyte cmd, jint data){
    int rv = 0;
    log_info("Writing a command 0x%02X and word data 0x%04X", cmd, data);
    #ifdef __linux__
        rv = i2c_smbus_write_word_data(fd, cmd, data);
    #endif
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CReadByteData(JNIEnv* env, jobject obj, jint fd, jbyte cmd){
    int rv = 0;
    log_info("Reading a byte data with the command 0x%02X", cmd);
    #ifdef __linux__
        rv = i2c_smbus_read_byte_data(fd, cmd);
    #endif
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CReadWordData(JNIEnv* env, jobject obj, jint fd, jbyte cmd){
    int rv = 0;
    log_info("Reading a word data with the command 0x%02X", cmd);
    #ifdef __linux__
        rv = i2c_smbus_read_word_data(fd, cmd);
    #endif
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CRead(JNIEnv* env, jobject obj, jint fd, jbyteArray arr){
    jsize len = (*env)->GetArrayLength(env, arr);
    jbyte *rbuf = (*env)->GetByteArrayElements(env, arr, 0);
    log_info("Reading %d bytes of data", len);
    int rv = 0;
    #ifdef __linux__
        rv = read(fd, rbuf, len);
    #endif
    (*env)->ReleaseByteArrayElements(env, arr, rbuf, 0);
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CWrite(JNIEnv* env, jobject obj, jint fd, jbyteArray arr){
    jsize len = (*env)->GetArrayLength(env, arr);
    jbyte *wbuf = (*env)->GetByteArrayElements(env, arr, 0);
    log_info("Writing %d bytes of data", len);
    int rv = 0;
    #ifdef __linux__
        rv = write(fd, wbuf, len);
    #endif
    (*env)->ReleaseByteArrayElements(env, arr, wbuf, 0);
    return rv;
}

int Java_org_arl_jhwbus_I2CDevice_I2CWriteRead(JNIEnv* env, jobject obj, jint fd, jbyteArray warr, jbyteArray rarr){
    jsize wlen = (*env)->GetArrayLength(env, warr);
    jbyte *wbuf = (*env)->GetByteArrayElements(env, warr, 0);
    jsize rlen = (*env)->GetArrayLength(env, rarr);
    jbyte *rbuf = (*env)->GetByteArrayElements(env, rarr, 0);
    log_info("Writing %d bytes of data, reading %d bytes of data", wlen, rlen);
    int rv = 0;
    #ifdef __linux__
        rv = write(fd, wbuf, wlen);
        if (rv == wlen) rv = read(fd, rbuf, rlen);
        else rv = -1;
    #endif
    (*env)->ReleaseByteArrayElements(env, warr, wbuf, 0);
    (*env)->ReleaseByteArrayElements(env, rarr, rbuf, 0);
    return rv;
}

void Java_org_arl_jhwbus_I2CDevice_I2CClose(JNIEnv* env, jobject obj, jint fd){
    log_info("Closing the I2C Interface");
    #ifdef __linux__
        close(fd);
    #endif
}
