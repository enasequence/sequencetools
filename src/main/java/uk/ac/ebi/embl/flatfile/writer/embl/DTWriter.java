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
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the DT lines. */
public class DTWriter extends FlatFileWriter {
  private boolean isConvff = false;

  public DTWriter(Entry entry, boolean isConvff) {
    super(entry);
    this.isConvff = isConvff;
  }

  public DTWriter(Entry entry) {
    super(entry);
  }

  public boolean write(Writer writer) throws IOException {
    if (entry.getFirstPublic() == null
        || entry.getFirstPublicRelease() == null
        || entry.getLastUpdated() == null
        || entry.getLastUpdatedRelease() == null
        || entry.getVersion() == null) {
      if (entry.getLastUpdated() != null) {
        if (isConvff) {
          writer.write(EmblPadding.DT_PADDING);
          writer.write(
              FlatFileDateUtils.formatAsDay(
                  entry.getFirstPublic() == null
                      ? entry.getLastUpdated()
                      : entry.getFirstPublic()));
          writer.write(" (Created)\n");
        }

        writer.write(EmblPadding.DT_PADDING);
        writer.write(FlatFileDateUtils.formatAsDay(entry.getLastUpdated()));
        writer.write(" (Last updated)\n");
        return true;
      }
      return false;
    }
    writer.write(EmblPadding.DT_PADDING);
    writer.write(FlatFileDateUtils.formatAsDay(entry.getFirstPublic()));
    writer.write(" (Rel. ");
    writer.write(entry.getFirstPublicRelease().toString());
    writer.write(", Created)\n");
    writer.write(EmblPadding.DT_PADDING);
    writer.write(FlatFileDateUtils.formatAsDay(entry.getLastUpdated()));
    writer.write(" (Rel. ");
    writer.write(entry.getLastUpdatedRelease().toString());
    writer.write(", Last updated, Version ");
    writer.write(entry.getVersion().toString());
    writer.write(")\n");
    return true;
  }
}
