/******************************************************************************

Copyright (c) 2019, Mandar Chitre & Chinmay Pendharkar

This file is part of fjage which is released under Simplified BSD License.
See file LICENSE.txt or go to http://www.opensource.org/licenses/BSD-3-Clause
for full license details.

******************************************************************************/

package org.arl.jhwbus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * I2C device access.
 */
public final class I2CDevice {

  private static Logger log = Logger.getLogger("org.arl.jhwbus");

  // singleton management logic

  private static class Handle {
    final String dev;
    int fd = -1;
    int users = 0;

    private Handle (String dev){
      this.dev = dev;
    }
  }

  private static final Map<String,Handle> handles = new HashMap<>();

  /**
   * Open I2C device.
   *
   * @param dev I2C device file name.
   * @param addr I2C address of device.
   */
  public static I2CDevice open(String dev, byte addr) throws IOException {
    if (addr < 0) throw new IOException("Bad I2C address");
    synchronized (handles) {
      Handle handle = handles.get(dev);
      if (handle == null) {
        handle = new Handle(dev);
        handle.fd = I2COpen(dev);
        if (handle.fd < 0) throw new IOException("Error opening I2C device " + dev + ": " + handle.fd);
        handles.put(dev, handle);
      }
      handle.users++;
      return new I2CDevice(handle, addr);
    }
  }

  /**
   * Close all open I2C devices.
   */
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
  private final byte addr;

  private I2CDevice(Handle handle, byte addr) {
    this.handle = handle;
    this.addr = addr;
  }

  /**
   * Close I2C device.
   */
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
      handles.remove(handle.dev);
    }
    handle = null;
  }

  /**
   * Read byte from I2C device.
   */
  public int readByte() throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CReadByte(handle.fd);
    }
  }

  /**
   * Send command and read data byte from I2C device.
   */
  public int readByteData(byte cmd) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CReadByteData(handle.fd, cmd);
    }
  }

  /**
   * Send command and read data word from I2C device.
   */
  public int readWordData(byte cmd) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CReadWordData(handle.fd, cmd);
    }
  }

  /**
   * Write byte to I2C device.
   */
  public int writeByte(byte data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWriteByte(handle.fd, data);
    }
  }

  /**
   * Write command and data byte to I2C device.
   */
  public int writeByteData(byte cmd, byte data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWriteByteData(handle.fd, cmd, data);
    }
  }

  /**
   * Write command and data word to I2C device.
   */
  public int writeWordData(byte cmd, int data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWriteWordData(handle.fd, cmd, data);
    }
  }

  /**
   * Write an array of data bytes.
   */
  public int write(byte[] data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWrite(handle.fd, data);
    }
  }

  /**
   * Read an array of data bytes
   */
  public int read(byte[] data) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CRead(handle.fd, data);
    }
  }

  /**
   * Write an array of data bytes and read an array of bytes immediately after
   */
  public int writeRead(byte[] wdata, byte [] rdata) throws IOException {
    if (handle == null || handle.fd < 0) throw new IOException("I2C device already closed");
    synchronized (handle) {
      I2CSetAddr(handle.fd, addr);
      return I2CWriteRead(handle.fd, wdata, rdata);
    }
  }

  @Override
  public void finalize() {
    close();
  }

  @Override
  public String toString() {
    if (handle == null || handle.fd < 0) return "I2CDevice: closed";
    return "I2CDevice : " + handle.dev + ":" + String.format("%02X",addr);
  }

  // Utility methods

  /**
   * Convert 2-byte array to 16-bit word.
   */
  public static int byteArrayToWord(byte[] bytes) {
    if (bytes.length != 2) throw new IllegalArgumentException("Can only convert 2 bytes to a 16 bit word");
    return ByteBuffer.wrap(bytes).getInt();
  }

  /**
   * Convert 16-bit word to 2-byte array.
   */
  public static byte [] wordToByteArray(int word) {
    if (word > 65535 || word < 0) throw new IllegalArgumentException("Word has to be between 0 and 65535");
    return ByteBuffer.allocate(2).putInt(word).array();
  }

  // JNI interface

  static {
    String libSuffix = "";
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("linux")) libSuffix += ".so";
    else if (os.contains("mac")) libSuffix += ".dylib";

    String libName = "libjhwbus" + libSuffix;

    AccessController.doPrivileged(new PrivilegedAction() {
      public Object run() {
        try {
          // Check if native lib exists in classpath.
          System.loadLibrary(libName);
        } catch (Throwable e) {
          try {
            // Create a temp dir
            File temporaryDir = new File(System.getProperty("java.io.tmpdir"), "jhwbus-" + System.nanoTime());
            if (!temporaryDir.mkdir()) throw new IOException("Failed to create temp directory " + temporaryDir.getName());
            temporaryDir.deleteOnExit();
            // Extract the file to the temp dir
            File temp = new File(temporaryDir, libName);
            try (InputStream is = I2CDevice.class.getResourceAsStream("/"+libName)) {
              if (is == null){
                temp.delete();
                throw new FileNotFoundException("File " + libName + " was not found inside JAR.");
              }
              Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ie) {
              temp.delete();
              throw e;
            } catch (Exception ne) {
              temp.delete();
              throw new FileNotFoundException("File " + libName + " was not found inside JAR.");
            }
            // Load the file
            try {
              System.load(temp.getAbsolutePath());
            } finally {
              temp.delete();
            }
          } catch (Exception ie) {
            throw new RuntimeException(ie);
          }
        }
        return null;
      }
    });
  }

  private native static int  I2COpen(String dev);
  private native int  I2CSetAddr(int fd, byte addr);
  private native int  I2CReadByte(int fd);
  private native int  I2CWriteByte(int fd, byte data);
  private native int  I2CWriteByteData(int fd, byte cmd, byte data);
  private native int  I2CWriteWordData(int fd, byte cmd, int data);
  private native int  I2CReadByteData(int fd, byte cmd);
  private native int  I2CReadWordData(int fd, byte cmd);
  private native int  I2CRead(int fd, byte [] data);
  private native int  I2CWrite(int fd, byte [] data);
  private native int  I2CWriteRead(int fd, byte [] wdata, byte [] rdata);
  private native static  void I2CClose(int fd);

}
