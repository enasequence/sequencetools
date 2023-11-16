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
package uk.ac.ebi.embl.api.validation;

import java.io.IOException;
import java.io.Writer;

public class FlatFileOrigin implements Origin {

  private static final long serialVersionUID = -581987225251503899L;

  private String fileId; // the name of the text file or some other file id
  private final int firstLineNumber;
  private final int lastLineNumber;

  public FlatFileOrigin(String fileId, int lineNumber) {
    this.fileId = fileId;
    this.firstLineNumber = lineNumber;
    this.lastLineNumber = lineNumber;
  }

  public FlatFileOrigin(String fileId, int firstLineNumber, int lastLineNumber) {
    this.fileId = fileId;
    this.firstLineNumber = firstLineNumber;
    this.lastLineNumber = lastLineNumber;
  }

  public FlatFileOrigin(int lineNumber) {
    this.firstLineNumber = lineNumber;
    this.lastLineNumber = lineNumber;
  }

  public FlatFileOrigin(int firstLineNumber, int lastLineNumber) {
    this.firstLineNumber = firstLineNumber;
    this.lastLineNumber = lastLineNumber;
  }

  public String getFileId() {
    return fileId;
  }

  public int getFirstLineNumber() {
    return firstLineNumber;
  }

  public int getLastLineNumber() {
    return lastLineNumber;
  }

  public void writeTextOrigin(Writer writer) throws IOException {
    if (firstLineNumber == lastLineNumber) {
      StringBuilder originString = new StringBuilder(" line: ").append(getFirstLineNumber());
      if (fileId != null) {
        originString.append(" of ").append(fileId);
      }
      writer.write(originString.toString());
    } else {
      StringBuilder originString =
          new StringBuilder(" line: ")
              .append(getFirstLineNumber())
              .append("-")
              .append(getLastLineNumber());
      if (fileId != null) {
        originString.append(" of ").append(fileId);
      }
      writer.write(originString.toString());
    }
  }

  public void writeXmlOrigin(Writer writer) throws IOException {}

  @Override
  public String getOriginText() {
    StringBuilder originString = new StringBuilder();

    if (firstLineNumber == lastLineNumber) {
      originString.append(" line: ");
      originString.append(getFirstLineNumber());

      if (fileId != null) {
        originString.append(" of ").append(fileId);
      }
    } else {
      originString.append(" line: ");
      originString.append(getFirstLineNumber());
      originString.append("-");
      originString.append(getLastLineNumber());

      if (fileId != null) {
        originString.append(" of ").append(fileId);
      }
    }

    return originString.toString();
  }
}
