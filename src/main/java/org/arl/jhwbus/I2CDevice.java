package org.arl.jhwbus;

import java.util.*;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class I2CDevice {

  // singleton management logic

  private static class Handle {
    final String deviceID;
    int fd = -1;
    int users = 0;

    private Handle (String devID){
      this.deviceID = devID;
    }
  }

  private static Map<String,Handle> handles = new HashMap<>();

  public static I2CDevice open(String deviceID, int addr) throws IOException {
    if (addr < 0 || addr > 255) throw new IOException("Bad I2C address");
    synchronized (handles) {
      Handle handle = handles.get(deviceID);
      if (handle == null) {
        handle = new Handle(deviceID);
        handle.fd = I2COpen(deviceID);
        if (handle.fd < 0) throw new IOException("Error opening I2C device " + deviceID + ": " + handle.fd);
        handles.put(deviceID, handle);
      }
      handle.users++;
      return new I2CDevice(handle, addr);
    }
  }

  public static void closeAll() {
    synchronized (handles) {
      for (Handle handle: handles.values()) {
        synchronized (handle) {
          handle.users = 0;
          if (handle.fd >= 0) I2CClose(handle.fd);
          handle.fd = -1;
        }
      }
      handles.clear();
    }
  }

  // instance attributes & methods

  private Handle handle;
  private final int addr;

  private I2CDevice(Handle handle, int addr) {
    this.handle = handle;
    this.addr = addr;
  }

  public void close() {
    if (handle == null) return;
    if (handle.users <= 0 || handle.fd < 0) return;
    synchronized (handle) {
      handle.users--;
      if (handle.users == 0) {
        I2CClose(handle.fd);
        handle.fd = -1;
      }
    }
    synchronized (handles) {
      handles.remove(handle.deviceID);
    }
    handle = null;
  }

  public int readByte() throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CReadByte(handle.fd);
    }
  }

  public int writeByte(byte data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWriteByte(handle.fd, data);
    }
  }

  public int writeByteData(byte cmd, byte data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWriteByteData(handle.fd, cmd, data);
    }
  }

  public int writeWordData(byte cmd, int data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWriteWordData(handle.fd, cmd, data);
    }
  }

  public int readByteData(byte cmd) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CReadByteData(handle.fd, cmd);
    }
  }

  public int readWordData(byte cmd) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CReadWordData(handle.fd, cmd);
    }
  }

  @Override
  public void finalize() {
    close();
  }

  @Override
  public String toString() {
    if (handle == null || handle.fd < 0) return "I2CDevice: closed";
    return "I2CDevice : " + handle.deviceID + ":" + String.format("%02X",addr);
  }

  // Utility methods

  public static int byteArrayToWord(byte [] bytes) {
    if (bytes.length != 2) throw new IllegalArgumentException("Can only convert 2 bytes to a 16 bit word");
    return ByteBuffer.wrap(bytes).getInt();
  }

  public static byte [] wordToByteArray(int word) {
    if (word > 65535 || word < 0) throw new IllegalArgumentException("Word has to be between 0 and 65535");
    return ByteBuffer.allocate(2).putInt(word).array();
  }

  // JNI interface

  static {
    System.loadLibrary("i2c");
  }

  private native static int  I2COpen(String dev);
  private native int  I2CSetAddr(int fd, int addr);
  private native int  I2CReadByte(int fd);
  private native int  I2CWriteByte(int fd, int data);
  private native int  I2CWriteByteData(int fd, int cmd, int data);
  private native int  I2CWriteWordData(int fd, int cmd, int data);
  private native int  I2CReadByteData(int fd, int cmd);
  private native int  I2CReadWordData(int fd, int cmd);
  private native static  void I2CClose(int fd);

}
