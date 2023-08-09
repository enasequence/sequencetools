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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Patent;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the patent lines. */
public class EmblPatentWriter extends FlatFileWriter {

  private final Patent patent;
  private String header = EmblPadding.RL_PADDING;

  public EmblPatentWriter(Entry entry, Patent patent, WrapType wrapType) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
    this.patent = patent;
  }

  public EmblPatentWriter(Entry entry, Patent patent, WrapType wrapType, String header) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
    this.patent = patent;
    this.header = header;
  }

  public boolean write(Writer writer) throws IOException {
    writer.write(header);
    writer.write("Patent number ");
    if (!isBlankString(patent.getPatentOffice())) {
      writer.write(patent.getPatentOffice());
    }
    if (!isBlankString(patent.getPatentNumber())) {
      writer.write(patent.getPatentNumber());
    }
    writer.write("-");
    if (!isBlankString(patent.getPatentType())) {
      writer.write(patent.getPatentType());
    }
    writer.write("/");
    if (patent.getSequenceNumber() != null) {
      writer.write(patent.getSequenceNumber().toString());
    }
    writer.write(", ");
    if (patent.getDay() != null) {
      writer.write(FlatFileDateUtils.formatAsDay(patent.getDay()));
    }
    writer.write(".\n");
    if (patent.getApplicants() != null) {
      int lastApplicant = patent.getApplicants().size();
      int currentApplicant = 0;
      for (String applicant : patent.getApplicants()) {
        ++currentApplicant;
        StringBuilder block = new StringBuilder();
        if (!isBlankString(applicant)) {
          block.append(applicant);
        }
        if (currentApplicant < lastApplicant) {
          block.append(";");
        } else {
          block.append(".");
        }
        writeBlock(writer, header, block.toString());
      }
    }
    return true;
  }
}
