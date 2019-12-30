#include <sys/time.h>
#include <inttypes.h>
#include <jni.h>

#ifdef __linux__
    #include <linux/i2c-dev.h>
    #include <sys/ioctl.h>
    #include <fcntl.h>
    #include <unistd.h>
#endif

static inline uint64_t current_time_ms() {
  struct timeval te;
  gettimeofday(&te, NULL);
  return te.tv_sec*1000ll + te.tv_usec/1000;
}

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

int Java_I2CDevice_I2COpen(JNIEnv* env, jobject obj, jstring dev, jint addr){
    int fd = 42;
    const char* devName = (*env)->GetStringUTFChars(env, dev, NULL) ;
    log_open("sentuator-i2c.txt");
    log_info("Opening I2C device %s", devName);
#ifdef __linux__
    fd = open(devName, O_RDWR);
#endif
    return fd;
}

int JAVA_I2CDevice_I2CSetAddr(JNIEnv* env, jobject obj, int fd, jint addr){
    log_info("Setting I2C addr %02X", addr);
    int rv = 0;
    #ifdef __linux__
        rv = ioctl(fd, I2C_SLAVE, addr);
    #endif
    return rv;
}

int Java_I2CDevice_I2CReadByte(JNIEnv* env, jobject obj, int fd){
    int rv = 0;
    log_info("Reading a byte");
    #ifdef __linux__
        rv = i2c_smbus_read_byte(fd);
    #endif
    return rv;
}

int Java_I2CDevice_I2CWriteByte(JNIEnv* env, jobject obj, int fd, int data){
    int rv = 0;
    log_info("Writing a byte %02X", data);
    #ifdef __linux__
        rv = i2c_smbus_write_byte(fd, data);
    #endif
    return rv;
}

int Java_I2CDevice_I2CWriteByteData(JNIEnv* env, jobject obj, int fd, int cmd, int data){
    int rv = 0;
    log_info("Writing a command %02X and byte data %02X", cmd, data);
    #ifdef __linux__
        rv = i2c_smbus_write_byte_data(fd, cmd, data);
    #endif
    return rv;
}

int Java_I2CDevice_I2CWriteWordData(JNIEnv* env, jobject obj, int fd, int cmd, int data){
    int rv = 0;
    log_info("Writing a command %02X and word data %04X", cmd, data);
    #ifdef __linux__
        rv = i2c_smbus_write_word_data(fd, cmd, data);
    #endif
    return rv;
}

int Java_I2CDevice_I2CReadByteData(JNIEnv* env, jobject obj, int fd, int cmd){
    int rv = 0;
    log_info("Reading a byte data with the command %02X", cmd);
    #ifdef __linux__
        rv = i2c_smbus_read_byte_data(fd, cmd);
    #endif
    return rv;
}

int Java_I2CDevice_I2CReadWordData(JNIEnv* env, jobject obj, int fd, int cmd){
    int rv = 0;
    log_info("Reading a word data with the command %02X", cmd);
    #ifdef __linux__
        rv = i2c_smbus_read_word_data(fd, cmd);
    #endif
    return rv;
}

void Java_I2CDevice_I2CClose(JNIEnv* env, jobject obj, int fd){
    log_info("Closing the I2C Interface");
    #ifdef __linux__
        close(fd);
    #endif
}
