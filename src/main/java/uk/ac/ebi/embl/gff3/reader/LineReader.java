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
package uk.ac.ebi.embl.gff3.reader;

import java.io.BufferedReader;
import java.io.IOException;
import uk.ac.ebi.embl.flatfile.reader.LineReaderCache;

/** Reader for flat file lines. */
public class LineReader {

  private static final String GFF3_COMMENT_CHAR = "#";

  public LineReader() {}

  public LineReader(BufferedReader reader) {
    this.reader = reader;
  }

  public LineReader(BufferedReader reader, String fileId) {
    this.reader = reader;
    this.fileId = fileId;
  }

  public void setReader(BufferedReader reader) {
    this.reader = reader;
  }

  private BufferedReader reader;

  /** an identifier of the thing being read - a file path or an accession */
  private String fileId = null;

  private String currentLine = null;

  private String nextLine = null;

  private int currentLineNumber = 0;

  private int nextLineNumber = 0;

  private LineReaderCache cache = new LineReaderCache();

  public String getFileId() {
    return fileId;
  }

  private boolean isLine(String line) {
    return line != null;
  }

  /** Return true if the current line exists. */
  public boolean isCurrentLine() {
    return isLine(currentLine);
  }

  /** Return true if the next line exists. */
  public boolean isNextLine() {
    return isLine(nextLine);
  }

  /** Return current line trimmed. */
  public String getCurrentLine() {
    if (!isCurrentLine()) {
      return null;
    }
    return currentLine.trim();
  }

  /** Return next line without tag. */
  public String getNextLine() {
    if (!isNextLine()) {
      return null;
    }
    return nextLine.trim();
  }

  public String getCurrentRawLine() {
    if (!isCurrentLine()) {
      return null;
    }
    return currentLine;
  }

  /** Return current line number. */
  public int getCurrentLineNumber() {
    return currentLineNumber;
  }

  public boolean readLine() throws IOException {
    if (currentLineNumber == 0) {
      while (true) {
        currentLine = reader.readLine();
        ++currentLineNumber;
        ++nextLineNumber;
        if (currentLine != null) {
          if (currentLine.length() == 0) {
            continue;
          }
          if (isSkipLine(currentLine)) {
            continue;
          }
          //    				currentLine = REPLACE.matcher(currentLine).replaceAll(" ");
        }
        break;
      }
    } else {
      currentLine = nextLine;
      currentLineNumber = nextLineNumber;
    }
    if (currentLine == null) {
      return false;
    }
    while (true) {
      nextLine = reader.readLine();
      ++nextLineNumber;
      if (nextLine != null) {
        if (nextLine.length() == 0) {
          continue;
        }
        if (isSkipLine(nextLine)) {
          continue;
        }
        //				nextLine = REPLACE.matcher(nextLine).replaceAll(" ");
      }
      break;
    }

    //        System.out.println("currentLine = " + currentLine);

    return true;
  }

  public LineReaderCache getCache() {
    return cache;
  }

  protected boolean isSkipLine(String line) {
    return line.startsWith(GFF3_COMMENT_CHAR);
  }
}
