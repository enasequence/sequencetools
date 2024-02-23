/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.checksum;

public class CRC32 {
  private static final long polynomial = 0xEDB88320L;
  private static long crc = 0;
  private static final long[] values = new long[256];
  private static boolean init_done = false;

  /** Calculates a CRC value for a byte to be used by CRC calculation functions. */
  private static void initialize() {
    for (int i = 0; i < 256; ++i) {
      long crc = i;
      for (int j = 8; j > 0; j--) {
        if ((crc & 1) == 1) crc = (crc >>> 1) ^ polynomial;
        else crc >>>= 1;
      }
      values[i] = crc;
    }
    init_done = true;
  }

  /** Calculates the CRC-32 of a block of data all at once */
  public static long calculateCRC32(byte[] buffer, int offset, int length) {
    if (!init_done) {
      initialize();
    }
    for (int i = offset; i < offset + length; i++) {
      long tmp1 = (crc >>> 8) & 0x00FFFFFFL;
      long tmp2 = values[(int) ((crc ^ Character.toUpperCase((char) buffer[i])) & 0xff)];
      crc = tmp1 ^ tmp2;
      // System.out.println("CRC:   "+crc);
    }
    return crc;
  }

  /**
   * Calculates the CRC-32 of a block of data all at once
   *
   * @return
   */
  public static long calculateCRC32(byte[] buffer) {
    reset();
    return calculateCRC32(buffer, 0, buffer.length);
  }

  /** Resets the state to process more data. */
  public static void reset() {

    crc = 0xFFFFFFFFL;
  }
}
