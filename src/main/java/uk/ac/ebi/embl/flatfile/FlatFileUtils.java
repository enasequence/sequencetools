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
package uk.ac.ebi.embl.flatfile;

import java.util.Vector;
import java.util.regex.Pattern;

public abstract class FlatFileUtils {

  /**
   * Removes all whitespace characters from the beginning and end of the string starting from the
   * given position.
   */
  public static String trim(String string, int pos) {
    int len = string.length();

    int leftPos = pos;
    int rightPos = len;

    for (; rightPos > 0; --rightPos) {
      char ch = string.charAt(rightPos - 1);
      if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r') {
        break;
      }
    }

    for (; leftPos < rightPos; ++leftPos) {
      char ch = string.charAt(leftPos);
      if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r') {
        break;
      }
    }

    if (rightPos <= leftPos) {
      return "";
    }

    return (leftPos == 0 && rightPos == string.length())
        ? string
        : string.substring(leftPos, rightPos);
  }

  /** Removes all whitespace characters from the end of the string. */
  public static String trimRight(String string) {
    for (int i = string.length(); i > 0; --i) {
      if (string.charAt(i - 1) != ' '
          && string.charAt(i - 1) != '\t'
          && string.charAt(i - 1) != '\n'
          && string.charAt(i - 1) != '\r') {
        return i == string.length() ? string : string.substring(0, i);
      }
    }
    return "";
  }

  /**
   * Removes all whitespace characters from the end of the string starting from the given position.
   */
  public static String trimRight(String string, int pos) {
    int i = string.length();
    for (; i > pos; --i) {
      char charAt = string.charAt(i - 1);
      if (charAt != ' ' && charAt != '\t' && charAt != '\n' && charAt != '\r') {
        break;
      }
    }

    if (i <= pos) {
      return "";
    }

    return (0 == pos && i == string.length()) ? string : string.substring(pos, i);
  }

  /**
   * Removes all whitespace characters and instances of the given character from the end of the
   * string.
   */
  public static String trimRight(String string, char c) {
    for (int i = string.length(); i > 0; --i) {
      char charAt = string.charAt(i - 1);
      if (charAt != c && charAt != ' ' && charAt != '\t' && charAt != '\n' && charAt != '\r') {
        return i == string.length() ? string : string.substring(0, i);
      }
    }
    return "";
  }

  /** Removes all whitespace characters from the beginning of the string. */
  public static String trimLeft(String string) {
    for (int i = 0; i < string.length(); ++i) {
      char charAt = string.charAt(i);
      if (charAt != ' ' && charAt != '\t' && charAt != '\n' && charAt != '\r') {
        return i == 0 ? string : string.substring(i);
      }
    }
    return string;
  }

  /**
   * Removes all whitespace characters and instances of the given character from the beginning of
   * the string.
   */
  public static String trimLeft(String string, char c) {
    for (int i = 0; i < string.length(); ++i) {
      char charAt = string.charAt(i);
      if (charAt != c && charAt != ' ' && charAt != '\t' && charAt != '\n' && charAt != '\r') {
        return i == 0 ? string : string.substring(i);
      }
    }
    return string;
  }

  /**
   * Split the string into values using the regular expression, removes whitespace from the
   * beginning and end of the resultant strings and replaces runs of whitespace with a single space.
   */
  public static Vector<String> split(String string, String regex) {
    Vector<String> strings = new Vector<String>();
    for (String value : string.split(new String(regex))) {
      value = value.trim();
      if (!value.equals("")) {
        strings.add(shrink(value));
      }
    }
    return strings;
  }

  private static final Pattern SHRINK = Pattern.compile(" {2,}");

  /** Trims the string and replaces runs of whitespace with a single space. */
  public static String shrink(String string) {
    if (string == null) {
      return null;
    }
    string = string.trim();
    return SHRINK.matcher(string).replaceAll(" ");
  }

  /** Trims the string and replaces runs of whitespace with a single character. */
  public static String shrink(String string, char c) {
    if (string == null) {
      return null;
    }
    string = string.trim();
    Pattern pattern = Pattern.compile("\\" + String.valueOf(c) + "{2,}");
    return pattern.matcher(string).replaceAll(String.valueOf(c));
  }

  /** Removes the characters from the string. */
  public static String remove(String string, char c) {
    return string.replaceAll(String.valueOf(c), "");
  }

  /**
   * Returns true if a string is either null or empty.
   *
   * @param string the input string.
   * @return true if the string is either null or empty.
   */
  public static boolean isBlankString(String string) {
    return string == null || string.trim().equals("");
  }

  /**
   * Takes a string and converts any hex representations of characters (prefixed with % character)
   * and replaces with ASCII.
   *
   * @param seqId
   * @return
   */
  public static String parseHexString(String seqId) {
    // todo implement this
    return seqId;
  }
}
