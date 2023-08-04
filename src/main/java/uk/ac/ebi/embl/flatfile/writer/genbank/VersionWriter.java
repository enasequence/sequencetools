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
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the VERSION lines. */
public class VersionWriter extends FlatFileWriter {

  public VersionWriter(Entry entry) {
    super(entry);
  }

  public boolean write(Writer writer) throws IOException {
    if (entry.getSequence() == null) {
      return false;
    }
    writer.write(GenbankPadding.VERSION_PADDING);
    if (entry.getSequence().getAccession() != null) {
      writer.write(entry.getSequence().getAccession());
    } else {
      writer.write("XXX");
    }
    writer.write(".");
    if (entry.getSequence().getVersion() != null) {
      writer.write(entry.getSequence().getVersion().toString());
    } else {
      writer.write("XXX");
    }
    if (entry.getSequence().getGIAccession() != null) {
      writer.write("  ");
      writer.write(entry.getSequence().getGIAccession());
    }
    writer.write("\n");
    return true;
  }
}
