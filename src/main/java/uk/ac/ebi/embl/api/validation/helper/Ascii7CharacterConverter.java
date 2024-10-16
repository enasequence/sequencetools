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
package uk.ac.ebi.embl.api.validation.helper;

import java.text.Normalizer;
import org.apache.commons.lang3.CharUtils;

/**
 * Converts UTF8 to ASCII7 by removing diacritics and control characters, and replacing printable
 * characters with ASCII7 equivalents or ?.
 */
public class Ascii7CharacterConverter {

  /**
   * Converts UTF8 to ASCII7 by removing diacritics and control characters, and replacing printable
   * characters with ASCII7 equivalents or ?.
   *
   * @param str the input string in UTF8 format
   * @return the input string in ASCII7 format
   */
  public static String convert(String str) {
    if (str == null) {
      return null;
    }

    if (!doConvert(str)) {
      return str;
    }

    // Replace diacritics.
    str =
        Normalizer.normalize(str, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

    // Replace all non-ASCII characters (not 0-255) with ?.
    str = str.replaceAll("[^\\x00-\\xff]", "?");

    // Remove ASCII-7 control characters except \t and \n.
    str = str.replaceAll("[\\x00-\\x08]", "");
    str = str.replaceAll("[\\x0b-\\x1f]", "");
    str = str.replaceAll("[\\x7f]", "");

    // Remove ASCII-8 ISO-LATIN-1 control characters.
    str = str.replaceAll("[\\x80-\\xa0]", "");

    char[] a = str.toCharArray();
    // Replace printable ASCII-8 ISO-LATIN-1 characters.
    for (int i = 0; i < a.length; ++i) {
      if (a[i] == 'Æ') {
        a[i] = 'A';
      } else if (a[i] == 'Ð') {
        a[i] = 'E';
      } else if (a[i] == 'Ø') {
        a[i] = 'O';
      } else if (a[i] == 'ß') {
        a[i] = 's';
      } else if (a[i] == 'æ') {
        a[i] = 'a';
      } else if (a[i] == 'ð') {
        a[i] = 'e';
      } else if (a[i] == 'ø') {
        a[i] = 'o';
      } else if (a[i] == 'þ') {
        a[i] = 'p';
      }
    }
    str = String.valueOf(a);

    // Replace remaining ASCII-8 characters with ?.
    str = str.replaceAll("[\\xa1-\\xff]", "?");

    return str;
  }

  /**
   * Returns true if the UTF8 string needs to be converted into ASCII7.
   *
   * @param str the input string
   * @return true if the UTF8 string needs to be converted into ASCII7.
   */
  public static boolean doConvert(String str) {
    if (str == null) {
      return false;
    }

    char[] a = str.toCharArray();
    for (int i = 0; i < a.length; ++i) {
      if (!CharUtils.isAscii(a[i]) || CharUtils.isAsciiControl(a[i])) {
        return true;
      }
    }
    return false;
  }
}
