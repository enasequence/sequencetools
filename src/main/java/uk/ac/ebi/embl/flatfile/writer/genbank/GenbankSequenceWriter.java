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
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the sequence lines. */
public class GenbankSequenceWriter extends FlatFileWriter {

  private Sequence sequence;

  public GenbankSequenceWriter(Entry entry, Sequence sequence) {
    super(entry);
    this.sequence = sequence;
  }

  public boolean write(Writer writer) throws IOException {
    if (sequence == null || sequence.getSequenceByte() == null) {
      return false;
    }

    writer.write(GenbankPadding.ORIGIN_PADDING);
    writer.write("\n");

    byte[] bases = sequence.getSequenceByte();
    int charNumber = 0;
    int lineNumber = 1;

    for (int i = 0; i < bases.length; i++) {
      if (charNumber == 0) {
        String baseCount = Integer.toString(60 * (lineNumber - 1) + 1);
        int baseCountPadding = 9 - baseCount.length();
        for (int j = 1; j < baseCountPadding; j++) {
          writer.write(" ");
        }
        writer.write(baseCount);
        writer.write(" ");
      }

      charNumber++;
      if (charNumber == 11
          || charNumber == 21
          || charNumber == 31
          || charNumber == 41
          || charNumber == 51) {
        writer.write(" ");
      }

      writer.write((char) bases[i]);

      if (charNumber == 60) {
        lineNumber++;
        charNumber = 0;
        writer.write("\n");
      }
    }

    if (charNumber != 0) {
      writer.write("\n");
    }
    return true;
  }
}
