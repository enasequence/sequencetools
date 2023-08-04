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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the ERROR_MSG lines. */
public class ErrorMsgWriter extends FlatFileWriter {

  private List<String> errorMsgs;

  public ErrorMsgWriter(Entry entry, WrapType wrapType, List<String> errorMsgList) {
    super(entry, wrapType);
    this.errorMsgs = errorMsgList;
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
    setForceLineBreak(true);
  }

  public boolean write(Writer writer) throws IOException {
    for (String errorMsg : errorMsgs) {
      writeBlock(writer, GenbankPadding.ERROR_MSG_PADDING, GenbankPadding.BLANK_PADDING, errorMsg);
    }
    return true;
  }
}
