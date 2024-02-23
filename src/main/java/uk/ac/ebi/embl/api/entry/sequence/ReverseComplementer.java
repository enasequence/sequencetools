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
package uk.ac.ebi.embl.api.entry.sequence;

public class ReverseComplementer {

  @Deprecated
  public String reverseComplement(String sequence) {
    if (sequence == null) {
      return null;
    }
    StringBuilder str = new StringBuilder();
    for (int i = sequence.length() - 1; i >= 0; --i) {
      switch (sequence.charAt(i)) {
        case 'a':
          str.append('t');
          break;
        case 'c':
          str.append('g');
          break;
        case 'g':
          str.append('c');
          break;
        case 't':
          str.append('a');
          break;
        case 'u':
          str.append('a');
          break;
        case 'n':
          str.append('n');
          break;
        case 'y':
          str.append('r');
          break;
        case 'r':
          str.append('y');
          break;
        case 'm':
          str.append('k');
          break;
        case 'k':
          str.append('m');
          break;
        case 's':
          str.append('s');
          break;
        case 'w':
          str.append('w');
          break;
        case 'h':
          str.append('d');
          break;
        case 'b':
          str.append('v');
          break;
        case 'v':
          str.append('b');
          break;
        case 'd':
          str.append('h');
          break;
        case '*':
          str.append('*');
          break;
      }
    }
    return str.toString();
  }

  public byte[] reverseComplementByte(byte[] sequence) {
    if (sequence == null) {
      return null;
    }
    byte[] complementByte = new byte[sequence.length];
    int i = 0;
    int size = sequence.length;
    for (int j = size - 1; j >= 0; --j) {
      switch ((char) sequence[j]) {
        case 'a':
          complementByte[i++] = 't';
          break;
        case 'c':
          complementByte[i++] = 'g';
          break;
        case 'g':
          complementByte[i++] = 'c';
          break;
        case 't':
          complementByte[i++] = 'a';
          break;
        case 'u':
          complementByte[i++] = 'a';
          break;
        case 'n':
          complementByte[i++] = 'n';
          break;
        case 'y':
          complementByte[i++] = 'r';
          break;
        case 'r':
          complementByte[i++] = 'y';
          break;
        case 'm':
          complementByte[i++] = 'k';
          break;
        case 'k':
          complementByte[i++] = 'm';
          break;
        case 's':
          complementByte[i++] = 's';
          break;
        case 'w':
          complementByte[i++] = 'w';
          break;
        case 'h':
          complementByte[i++] = 'd';
          break;
        case 'b':
          complementByte[i++] = 'v';
          break;
        case 'v':
          complementByte[i++] = 'b';
          break;
        case 'd':
          complementByte[i++] = 'h';
          break;
        case '*':
          complementByte[i++] = '*';
          break;
      }
    }
    return complementByte;
  }
}
