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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.Entry;

/** A base class for all flat file writers. */
public abstract class FlatFileWriter {

  private static final int EMBL_OPTIMAL_LINE_LENGTH = 80;

  private static final int GENBANK_OPTIMAL_LINE_LENGTH = 79;

  private static final int MAXIMUM_LINE_LENGTH = 51200;

  protected WrapChar wrapChar;

  protected WrapType wrapType;

  protected Entry entry;

  /**
   * If true and if the line is longer than the optimal line length and we can't break the line on
   * space before the optimal line length then we will force a line break at the optimal line
   * length.
   */
  private boolean forceLineBreak = false;

  /**
   * The optimal maximum line length. We will attempt to break blocks into lines using space so that
   * each line is no longer the optimal line length.
   */
  private Integer optimalLineLength;

  private Integer maximumLineLength;

  public FlatFileWriter(Entry entry) {
    this.entry = entry;
  }

  public FlatFileWriter(Entry entry, WrapType wrapType) {
    this.entry = entry;
    this.wrapType = wrapType;
  }

  public void setWrapType(WrapType wrapType) {
    this.wrapType = wrapType;
  }

  public void setWrapChar(WrapChar wrapChar) {
    this.wrapChar = wrapChar;
  }

  private static boolean isWrapChar(char c, WrapChar wrapChar) {
    return (wrapChar == WrapChar.WRAP_CHAR_SPACE) && (c == ' ');
  }

  private static boolean isSplitChar(char c, WrapChar wrapChar) {
    return ((wrapChar == WrapChar.WRAP_CHAR_COMMA) && c == ',')
        || ((wrapChar == WrapChar.WRAP_CHAR_SEMICOLON) && c == ';');
  }

  private static final Pattern PATTERN = Pattern.compile("[\t\n\r]");

  /**
   * Writes flat file lines to an output stream.
   *
   * @param writer the output stream.
   * @return true if something was actually written to the output stream.
   * @throws IOException is there was an error in writing to the output stream.
   */
  public abstract boolean write(Writer writer) throws IOException;

  /** Writes a flat file block to an output stream. Does not write anything if the line is empty. */
  protected void writeBlock(Writer writer, String header, String block) throws IOException {
    writeBlock(writer, null, header, block);
  }

  /** Writes a flat file block to an output stream. Does not write anything if the line is empty. */
  protected void writeBlock(Writer writer, String firstLineHeader, String header, String block)
      throws IOException {
    writeBlock(
        writer,
        firstLineHeader,
        header,
        block,
        wrapChar,
        wrapType,
        header.length(),
        forceLineBreak,
        this.optimalLineLength,
        this.maximumLineLength);
  }

  public static void writeBlock(
      Writer writer,
      String firstLineHeader,
      String header,
      String block,
      WrapChar wrapChar,
      WrapType wrapType,
      int headerLength,
      boolean forceBreak,
      Integer optimalLineLength,
      Integer maximumLineLength)
      throws IOException {

    // Subtract header length from the optimal line length.
    optimalLineLength =
        optimalLineLength == null
            ? getDefaultOptimalLineLength(wrapType) - headerLength
            : optimalLineLength - headerLength;
    // Subtract header length from the maximum line length.
    int maxLineLength =
        maximumLineLength == null
            ? MAXIMUM_LINE_LENGTH - headerLength
            : maximumLineLength - headerLength;

    int remainingLineLength = block.length();

    int lineNumber = 0;

    // Remove whitespace characters.
    block = PATTERN.matcher(block).replaceAll(" ");

    while (remainingLineLength > optimalLineLength) {
      int end = optimalLineLength;

      // Split line on wrap character.
      while (end > 0 && !isWrapChar(block.charAt(end), wrapChar)) {
        end--;
      }

      // Split line on other characters if did not find a wrap character.
      if (end == 0) {
        end = optimalLineLength;
        while (end > 0 && !isSplitChar(block.charAt(end - 1), wrapChar)) end--;
      }

      // Break line at optimal line length if no luck finding a split
      // character and we have set to break.
      if (end == 0 && forceBreak) {
        end = optimalLineLength;
      }

      // Split line between optimal line length and maximum line length or
      // break line at maximum line length.
      if (end == 0) {
        end = optimalLineLength;
        while (end < remainingLineLength
            && !isWrapChar(block.charAt(end), wrapChar)
            && !isSplitChar(block.charAt(end - 1), wrapChar)) {
          ++end;
          // Break line at maximum line length.
          if (end == maxLineLength) break;
        }
      }
      // No terminating quote on its own line.
      if ((remainingLineLength - end) == 1 && block.charAt(end) == '"') {
        ++end;
      }

      int writeLength = end;
      ++lineNumber;
      writeLine(
          writer,
          getHeaderToWrite(firstLineHeader, header, lineNumber),
          block.substring(0, writeLength));

      // Discard space character.
      if (!(end > block.length() - 1)) {
        final char c = block.charAt(end);
        if (c == 32) {
          ++writeLength;
        }
      }

      block = block.substring(writeLength);
      remainingLineLength -= writeLength;
    }

    if (remainingLineLength > 0) {
      ++lineNumber;
      writeLine(writer, getHeaderToWrite(firstLineHeader, header, lineNumber), block);
    }
  }

  public static int getDefaultOptimalLineLength(WrapType wrapType) {
    int optimalLineLength = EMBL_OPTIMAL_LINE_LENGTH;
    if (wrapType == WrapType.GENBANK_WRAP) {
      optimalLineLength = GENBANK_OPTIMAL_LINE_LENGTH;
    }
    if (wrapType == WrapType.NO_WRAP) {
      optimalLineLength = MAXIMUM_LINE_LENGTH;
    }
    return optimalLineLength;
  }

  protected static void writeLine(Writer writer, String header, String line) throws IOException {
    writer.write(header);
    writer.write(line);
    writer.write("\n");
  }

  private static String getHeaderToWrite(String firstLineHeader, String header, int lineNumber) {
    return lineNumber == 1 && firstLineHeader != null ? firstLineHeader : header;
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

  public void setForceLineBreak(boolean forceLineBreak) {
    this.forceLineBreak = forceLineBreak;
  }

  public void setOptimalLineLength(int optimalLineLength) {
    this.optimalLineLength = optimalLineLength;
  }

  public void setMaximumLineLength(Integer maximumLineLength) {
    this.maximumLineLength = maximumLineLength;
  }
}
