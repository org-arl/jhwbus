package org.arl.fjage.sentuator.devices

import java.nio.ByteBuffer

class I2CDevice {
    private static final int WORD_MAX = 65535
    private static final int I2C_FLAGS_O_RDWR = 0x0002

    static {
        System.loadLibrary("fjagesentuatori2c")
    }

    private int fd
    private final String deviceID
    private final int addr

    I2CDevice(String deviceID, int addr, int flags) throws FileNotFoundException {
        this.fd = I2COpen(deviceID, addr, flags)
        this.deviceID = deviceID
        this.addr = addr
        if (fd < 0) throw new FileNotFoundException("Error opening I2C device " + deviceID + " : " + fd)
    }

    I2CDevice(String deviceID, int addr) throws FileNotFoundException {
        this.fd = I2COpen(deviceID, addr, I2C_FLAGS_O_RDWR)
        this.deviceID = deviceID
        if (fd < 0) throw new FileNotFoundException("Error opening I2C device " + deviceID + " : " + fd)
        this.addr = addr
    }

    void close(){
        I2CClose(this.fd)
    }

    int readByte(){
        return I2CReadByte(this.fd)
    }

    int writeByte(byte data){
        return I2CWriteByte(this.fd, data)
    }

    int writeByteData(byte cmd, byte data){
        return I2CWriteByteData(this.fd, cmd, data)
    }

    int writeWordData(byte cmd, int data){
        return I2CWriteWordData(this.fd, cmd, data)
    }

    int readByteData(byte cmd){
        return I2CReadByteData(this.fd, cmd)
    }

    int readWordData(byte cmd){
        return I2CReadWordData(this.fd, cmd)
    }

    static int ByteArrayToWord(byte [] bytes){
        if (bytes.length != 2) throw new IllegalArgumentException("Can only convert 2 bytes to a 16bit word")
        return ByteBuffer.wrap(bytes).getInt()
    }

    static byte [] WordToByteArray(int word){
        if (word > WORD_MAX || word < 0) throw new IllegalArgumentException("Word has to be between 0 and 65535")
        return ByteBuffer.allocate(2).putInt(word).array()
    }

    @Override
    String toString() {
        return "I2CDevice : "+this.deviceID + " : " + this.addr != null ? String.format("0x%02X", this.addr) : ""
    }

    private native int I2COpen(String dev, int addr, int flags)
    private native int I2CReadByte(int fd)
    private native int I2CWriteByte(int fd, int data)
    private native int I2CWriteByteData(int fd, int cmd, int data)
    private native int I2CWriteWordData(int fd, int cmd, int data)
    private native int I2CReadByteData(int fd, int cmd)
    private native int I2CReadWordData(int fd, int cmd)
    private native void I2CClose(int fd)

}
