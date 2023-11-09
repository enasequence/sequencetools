/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.helper;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.lang.ArrayUtils;

public class ByteBufferUtils {

  public static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(ArrayUtils.EMPTY_BYTE_ARRAY);
  public static final Charset UTF_8 = StandardCharsets.UTF_8;

  /**
   * Decode a String representation. This method assumes that the encoding charset is UTF_8.
   *
   * @param buffer a byte buffer holding the string representation
   * @return the decoded string
   */
  public static String string(ByteBuffer buffer) throws CharacterCodingException {
    return string(buffer, UTF_8);
  }

  /**
   * Decode a String representation. This method assumes that the encoding charset is UTF_8.
   *
   * @param buffer a byte buffer holding the string representation
   * @param position the starting position in {@code buffer} to start decoding from
   * @param length the number of bytes from {@code buffer} to use
   * @return the decoded string
   */
  public static String string(ByteBuffer buffer, int position, int length)
      throws CharacterCodingException {
    return string(buffer, position, length, UTF_8);
  }

  /**
   * Decode a String representation.
   *
   * @param buffer a byte buffer holding the string representation
   * @param position the starting position in {@code buffer} to start decoding from
   * @param length the number of bytes from {@code buffer} to use
   * @param charset the String encoding charset
   * @return the decoded string
   */
  public static String string(ByteBuffer buffer, int position, int length, Charset charset)
      throws CharacterCodingException {
    ByteBuffer copy = buffer.duplicate();
    copy.position(position);
    copy.limit(copy.position() + length);
    return string(copy, charset);
  }

  /**
   * Decode a String representation.
   *
   * @param buffer a byte buffer holding the string representation
   * @param charset the String encoding charset
   * @return the decoded string
   */
  public static String string(ByteBuffer buffer, Charset charset) throws CharacterCodingException {
    return charset.newDecoder().decode(buffer.duplicate()).toString();
  }

  /** You should almost never use this. Instead, use the write* methods to avoid copies. */
  public static byte[] getArray(ByteBuffer buffer) {
    int length = buffer.remaining();

    if (buffer.hasArray()) {
      int boff = buffer.arrayOffset() + buffer.position();
      if (boff == 0 && length == buffer.array().length) return buffer.array();
      else return Arrays.copyOfRange(buffer.array(), boff, boff + length);
    }
    // else, DirectByteBuffer.get() is the fastest route
    byte[] bytes = new byte[length];
    buffer.duplicate().get(bytes);

    return bytes;
  }

  /**
   * ByteBuffer adaptation of org.apache.commons.lang.ArrayUtils.lastIndexOf method
   *
   * @param buffer the array to traverse for looking for the object, may be <code>null</code>
   * @param valueToFind the value to find
   * @param startIndex the start index (i.e. BB position) to travers backwards from
   * @return the last index (i.e. BB position) of the value within the array [between
   *     buffer.position() and buffer.limit()]; <code>-1</code> if not found.
   */
  public static int lastIndexOf(ByteBuffer buffer, byte valueToFind, int startIndex) {
    assert buffer != null;

    if (startIndex < buffer.position()) {
      return -1;
    } else if (startIndex >= buffer.limit()) {
      startIndex = buffer.limit() - 1;
    }

    for (int i = startIndex; i >= buffer.position(); i--) {
      if (valueToFind == buffer.get(i)) return i;
    }

    return -1;
  }

  /**
   * Encode a String in a ByteBuffer using UTF_8.
   *
   * @param s the string to encode
   * @return the encoded string
   */
  public static ByteBuffer bytes(String s) {
    if (s == null) return null;
    return ByteBuffer.wrap(s.getBytes(UTF_8));
  }

  /**
   * Encode a String in a ByteBuffer using the provided charset.
   *
   * @param s the string to encode
   * @param charset the String encoding charset to use
   * @return the encoded string
   */
  public static ByteBuffer bytes(String s, Charset charset) {
    return ByteBuffer.wrap(s.getBytes(charset));
  }

  /**
   * @return a new copy of the data in @param buffer USUALLY YOU SHOULD USE ByteBuffer.duplicate()
   *     INSTEAD, which creates a new Buffer (so you can mutate its position without affecting the
   *     original) without copying the underlying array.
   */
  public static ByteBuffer clone(ByteBuffer buffer) {
    assert buffer != null;

    if (buffer.remaining() == 0) return EMPTY_BYTE_BUFFER;

    ByteBuffer clone = ByteBuffer.allocate(buffer.remaining());

    if (buffer.hasArray()) {
      System.arraycopy(
          buffer.array(),
          buffer.arrayOffset() + buffer.position(),
          clone.array(),
          0,
          buffer.remaining());
    } else {
      clone.put(buffer.duplicate());
      clone.flip();
    }

    return clone;
  }

  public static void arrayCopy(
      ByteBuffer buffer, int position, byte[] bytes, int offset, int length) {
    if (buffer.hasArray())
      System.arraycopy(buffer.array(), buffer.arrayOffset() + position, bytes, offset, length);
    else ((ByteBuffer) buffer.duplicate().position(position)).get(bytes, offset, length);
  }

  /**
   * Transfer bytes from one ByteBuffer to another. This function acts as System.arrayCopy() but for
   * ByteBuffers.
   *
   * @param src the source ByteBuffer
   * @param srcPos starting position in the source ByteBuffer
   * @param dst the destination ByteBuffer
   * @param dstPos starting position in the destination ByteBuffer
   * @param length the number of bytes to copy
   */
  public static void arrayCopy(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int length) {
    if (src.hasArray() && dst.hasArray()) {
      System.arraycopy(
          src.array(), src.arrayOffset() + srcPos, dst.array(), dst.arrayOffset() + dstPos, length);
    } else {
      if (src.limit() - srcPos < length || dst.limit() - dstPos < length)
        throw new IndexOutOfBoundsException();

      for (int i = 0; i < length; i++)
        // TODO: ByteBuffer.put is polymorphic, and might be slow here
        dst.put(dstPos++, src.get(srcPos++));
    }
  }

  /**
   * Convert a byte buffer to an integer. Does not change the byte buffer position.
   *
   * @param bytes byte buffer to convert to integer
   * @return int representation of the byte buffer
   */
  public static int toInt(ByteBuffer bytes) {
    return bytes.getInt(bytes.position());
  }

  public static long toLong(ByteBuffer bytes) {
    return bytes.getLong(bytes.position());
  }

  public static float toFloat(ByteBuffer bytes) {
    return bytes.getFloat(bytes.position());
  }

  public static double toDouble(ByteBuffer bytes) {
    return bytes.getDouble(bytes.position());
  }

  public static ByteBuffer bytes(int i) {
    return ByteBuffer.allocate(4).putInt(0, i);
  }

  public static ByteBuffer bytes(long n) {
    return ByteBuffer.allocate(8).putLong(0, n);
  }

  public static ByteBuffer bytes(float f) {
    return ByteBuffer.allocate(4).putFloat(0, f);
  }

  public static ByteBuffer bytes(double d) {
    return ByteBuffer.allocate(8).putDouble(0, d);
  }

  public static InputStream inputStream(ByteBuffer bytes) {
    final ByteBuffer copy = bytes.duplicate();

    return new InputStream() {
      public int read() {
        if (!copy.hasRemaining()) return -1;

        return copy.get() & 0xFF;
      }

      @Override
      public int read(byte[] bytes, int off, int len) {
        if (!copy.hasRemaining()) return -1;

        len = Math.min(len, copy.remaining());
        copy.get(bytes, off, len);
        return len;
      }

      @Override
      public int available() {
        return copy.remaining();
      }
    };
  }

  /**
   * Compare two ByteBuffer at specified offsets for length. Compares the non equal bytes as
   * unsigned.
   *
   * @param bytes1 First byte buffer to compare.
   * @param offset1 Position to start the comparison at in the first array.
   * @param bytes2 Second byte buffer to compare.
   * @param offset2 Position to start the comparison at in the second array.
   * @param length How many bytes to compare?
   * @return -1 if byte1 is less than byte2, 1 if byte2 is less than byte1 or 0 if equal.
   */
  public static int compareSubArrays(
      ByteBuffer bytes1, int offset1, ByteBuffer bytes2, int offset2, int length) {
    if (null == bytes1) {
      if (null == bytes2) return 0;
      else return -1;
    }
    if (null == bytes2) return 1;

    assert bytes1.limit() >= offset1 + length
        : "The first byte array isn't long enough for the specified offset and length.";
    assert bytes2.limit() >= offset2 + length
        : "The second byte array isn't long enough for the specified offset and length.";
    for (int i = 0; i < length; i++) {
      byte byte1 = bytes1.get(offset1 + i);
      byte byte2 = bytes2.get(offset2 + i);
      if (byte1 == byte2) continue;
      // compare non-equal bytes as unsigned
      return (byte1 & 0xFF) < (byte2 & 0xFF) ? -1 : 1;
    }
    return 0;
  }
}
