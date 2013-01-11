package net.jpountz.lz4;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static net.jpountz.util.Utils.checkRange;

/**
 * {@link LZ4Decompressor} implemented with JNI bindings to the original C
 * implementation of LZ4.
 */
enum LZ4JNIUnknownSizeDecompressor implements LZ4UnknownSizeDecompressor {

  INSTANCE {

    public final int decompress(byte[] src, int srcOff, int srcLen, byte[] dest, int destOff) {
      checkRange(src, srcOff, srcLen);
      checkRange(dest, destOff);
      final int result = LZ4JNI.LZ4_decompress_unknownOutputSize(src, srcOff, srcLen, dest, destOff, dest.length - destOff);
      if (result < 0) {
        throw new LZ4Exception("Error decoding offset " + (srcOff - result) + " of input buffer");
      }
      return result;
    }
 
  };

  @Override
  public String toString() {
    return getDeclaringClass().getSimpleName();
  }

}
