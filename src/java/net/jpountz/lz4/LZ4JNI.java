package net.jpountz.lz4;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * JNI bindings to the original C implementation of LZ4.
 */
enum LZ4JNI {
  ;

  private enum OS {
    WINDOWS("windows", "dll"), LINUX("linux", "so"), MAC("mac", "dylib");
    public final String name, libExtension;

    private OS(String name, String libExtension) {
      this.name = name;
      this.libExtension = libExtension;
    }
  }

  private static String arch() {
    return System.getProperty("os.arch");
  }

  private static OS os() {
    String osName = System.getProperty("os.name");
    if (osName.contains("Linux")) {
      return OS.LINUX;
    } else if (osName.contains("Mac")) {
      return OS.MAC;
    } else if (osName.contains("Windows")) {
      return OS.WINDOWS;
    } else {
      throw new UnsupportedOperationException("Unsupported operating system: "
          + osName);
    }
  }

  private static String resourceName() {
    OS os = os();
    return "/" + os.name + "/" + arch() + "/liblz4-java." + os.libExtension;
  }

  static {
    String resourceName = resourceName();
    InputStream is = LZ4JNI.class.getResourceAsStream(resourceName);
    if (is == null) {
      throw new UnsupportedOperationException("Unsupported OS/arch, cannot find " + resourceName);
    }
    File tempLib;
    try {
      tempLib = File.createTempFile("liblz4-java", "." + os().libExtension);
      // copy to tempLib
      FileOutputStream out = new FileOutputStream(tempLib);
      try {
        byte[] buf = new byte[4096];
        while (true) {
          int read = is.read(buf);
          if (read == -1) {
            break;
          }
          out.write(buf, 0, read);
        }
        try {
          out.close();
          out = null;
        } catch (IOException e) {
          // ignore
        }
        System.load(tempLib.getAbsolutePath());

        // init library
        init();
      } finally {
        try {
          if (out != null) {
            out.close();
          }
        } catch (IOException e) {
          // ignore
        }
      }
    } catch (IOException e) {
        throw new ExceptionInInitializerError("Cannot unpack liblz4-java");
    }
  }

  static native void init();
  static native int LZ4_compress(byte[] src, int srcOff, int srcLen, byte[] dest, int destOff);
  static native int LZ4_compressHC(byte[] src, int srcOff, int srcLen, byte[] dest, int destOff);
  static native int LZ4_uncompress(byte[] src, int srcOff, byte[] dest, int destOff, int destLen);
  static native int LZ4_uncompress_unknownOutputSize(byte[] src, int srcOff, int srcLen, byte[] dest, int destOff, int maxDestLen);
  static native int LZ4_compressBound(int length);

}

