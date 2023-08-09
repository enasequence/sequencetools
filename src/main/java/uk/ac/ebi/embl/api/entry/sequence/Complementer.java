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
package uk.ac.ebi.embl.api.entry.sequence;

public class Complementer {

  public String complement(String sequence) {
    if (sequence == null) {
      return null;
    }
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < sequence.length(); ++i) {
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
}
