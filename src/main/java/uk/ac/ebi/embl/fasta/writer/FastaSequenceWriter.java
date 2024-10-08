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

public class FastaSequenceWriter {
  Writer writer;
  Entry entry;

  public FastaSequenceWriter(Writer writer, Entry entry) {
    this.writer = writer;
    this.entry = entry;
  }

  public void write() throws IOException {
    byte[] sequence = entry.getSequence().getSequenceByte();
    StringBuffer line = new StringBuffer();
    int i = 0;
    for (byte sequenceByte : sequence) {
      line.append((char) sequenceByte);
      if ((line.length()) % 60 == 0 && line.length() != 0) {
        if (i > 0) writer.write("\n");
        writer.write(line.toString().toUpperCase().trim());
        line = new StringBuffer();
        i++;
      }
    }
    if (line.length() > 0) {
      if (i > 0) writer.write("\n");
      writer.write(line.toString().toUpperCase());
    }
  }
}
