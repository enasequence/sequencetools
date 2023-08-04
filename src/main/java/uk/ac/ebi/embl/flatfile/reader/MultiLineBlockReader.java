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
package uk.ac.ebi.embl.flatfile.reader;

import java.io.IOException;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;

/** Reader for flat file blocks. */
public abstract class MultiLineBlockReader extends BlockReader {

  FileType fileType;
  private int firstLineNumber;
  private int lastLineNumber;
  private final ConcatenateType concatenateType;

  public MultiLineBlockReader(LineReader lineReader, ConcatenateType concatenateType) {
    super(lineReader);
    this.concatenateType = concatenateType;
  }

  public MultiLineBlockReader(
      LineReader lineReader, ConcatenateType concatenateType, FileType fileType) {
    super(lineReader);
    this.concatenateType = concatenateType;
    this.fileType = fileType;
  }

  public FlatFileOrigin getOrigin() {
    if (!EmblEntryReader.isOrigin) return null;
    else return new FlatFileOrigin(getLineReader().getFileId(), firstLineNumber, lastLineNumber);
  }

  public enum ConcatenateType {
    /**
     * Flat file lines are concatenated together after the removal of leading and trailing
     * whitespace. Multiple adjacent whitespace are trimmed down to a single whitespace. A single
     * space character is added between lines.
     */
    CONCATENATE_SPACE,
    /**
     * Flat file lines are concatenated together after the removal of leading and trailing
     * whitespace. Multiple adjacent whitespace are trimmed down to a single whitespace.
     */
    CONCATENATE_NOSPACE,
    /**
     * Flat file lines are concatenated together using line breaks ('\n') after removal or trailing
     * whitespace.
     */
    CONCATENATE_BREAK
  }

  protected void readLines() throws IOException {
    StringBuilder block = new StringBuilder();

    firstLineNumber = lineReader.getCurrentLineNumber();
    block.delete(0, block.length());
    if (concatenateType == ConcatenateType.CONCATENATE_SPACE
        || concatenateType == ConcatenateType.CONCATENATE_NOSPACE) {
      String line = lineReader.getCurrentShrinkedLine();
      if (line != null) {
        block.append(line);
      }
      while (lineReader.joinLine()) {
        lineReader.readLine();
        if (concatenateType == ConcatenateType.CONCATENATE_SPACE) {
          block.append(" ");
        }
        line = lineReader.getCurrentShrinkedLine();
        if (line != null) {
          block.append(line);
        }
      }
    } else if (concatenateType == ConcatenateType.CONCATENATE_BREAK) {
      String line = lineReader.getCurrentLine();
      if (line != null) {
        block.append(line);
      }
      while (lineReader.joinLine()) {
        lineReader.readLine();
        block.append("\n");
        line = lineReader.getCurrentLine();
        if (line != null) {
          block.append(line);
        }
      }
    }
    lastLineNumber = lineReader.getCurrentLineNumber();
    if (block.length() == 0) {
      return;
    }
    String blockString;

    if (lineReader.getReaderOptions().isIgnoreParserErrors()
        || (fileType != null && fileType == FileType.GENBANK)) {
      blockString = block.toString();
    } else {
      blockString = Utils.escapeASCIIHtmlEntities(block).toString();
    }

    if (concatenateType != ConcatenateType.CONCATENATE_BREAK) {
      // Remove double spaces.
      blockString = FlatFileUtils.shrink(blockString);
    }
    read(blockString);
  }
}
