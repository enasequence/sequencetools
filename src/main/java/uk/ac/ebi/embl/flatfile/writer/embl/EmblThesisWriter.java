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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Thesis;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the thesis lines. */
public class EmblThesisWriter extends FlatFileWriter {

  private final Thesis thesis;
  private String header = EmblPadding.RL_PADDING;

  public EmblThesisWriter(Entry entry, Thesis thesis, WrapType wrapType) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
    this.thesis = thesis;
  }

  public EmblThesisWriter(Entry entry, Thesis thesis, WrapType wrapType, String header) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
    this.thesis = thesis;
    this.header = header;
  }

  public boolean write(Writer writer) throws IOException {
    StringBuilder block = new StringBuilder();
    block.append("Thesis (");
    if (thesis.getYear() != null) {
      block.append(FlatFileDateUtils.formatAsYear(thesis.getYear()));
    }
    block.append(")");
    if (!isBlankString(thesis.getInstitute())) {
      block.append(", ");
      block.append(thesis.getInstitute());
    }
    block.append(".");
    writeBlock(writer, header, block.toString());
    return true;
  }
}
