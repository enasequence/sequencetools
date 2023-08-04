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
package uk.ac.ebi.embl.agp.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;

public class AGPFileWriter {
  private final Entry entry;
  private final Writer writer;

  public AGPFileWriter(Entry entry, Writer writer) {
    this.entry = entry;
    this.writer = writer;
  }

  public void write() throws IOException {
    AGPRowWriter agpRowWriter = null;
    for (AgpRow agpRow : entry.getSequence().getAgpRows()) {
      agpRowWriter = new AGPRowWriter(agpRow, writer);
      agpRowWriter.write();
      writer.write("\n");
    }
  }
}
