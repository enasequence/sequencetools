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
package uk.ac.ebi.embl.fasta.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;

public class FastaFileWriter {
  private final Entry entry;
  private final Writer writer;
  private final FastaHeaderFormat headerFormat;

  public enum FastaHeaderFormat {
    DEFAULT_HEADER_FORMAT,
    ANALYSIS_HEADER_FORMAT,
    ENA_HEADER_FORMAT,
    POLYSAMPLE_HEADER_FORMAT
  }

  public FastaFileWriter(Entry entry, Writer writer) {
    this.entry = entry;
    this.writer = writer;
    this.headerFormat = FastaHeaderFormat.DEFAULT_HEADER_FORMAT;
  }

  public FastaFileWriter(Entry entry, Writer writer, FastaHeaderFormat headerFormat) {
    this.entry = entry;
    this.writer = writer;
    this.headerFormat = headerFormat;
  }

  public void write() throws IOException {
    String header = null;
    switch (headerFormat) {
      case DEFAULT_HEADER_FORMAT:
        header =
            String.format(
                ">EM_%s:%s %s %s:%s",
                entry.getDivision(),
                entry.getPrimaryAccession(),
                entry.getSequence().getAccessionwithVersion(),
                entry.getDataClass(),
                entry.getDescription().getText());
        break;
      case ANALYSIS_HEADER_FORMAT:
        header =
            String.format(">%s %s", entry.getPrimaryAccession(), entry.getSubmitterAccession());
        break;
      case ENA_HEADER_FORMAT:
        header =
            String.format(
                ">ENA|%s|%s %s",
                entry.getPrimaryAccession(),
                entry.getSequence().getAccessionwithVersion(),
                entry.getDescription().getText());
        break;
        case POLYSAMPLE_HEADER_FORMAT:
          header =
                  String.format(">%s|%s", entry.getPrimaryAccession(), entry.getSubmitterAccession());
      default:
        break;
    }
    FastaSequenceWriter sequenceWriter = new FastaSequenceWriter(writer, entry);
    writer.write(header + "\n");
    sequenceWriter.write();
    writer.write("\n");
  }
}
