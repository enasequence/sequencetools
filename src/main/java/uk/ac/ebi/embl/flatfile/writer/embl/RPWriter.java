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
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the RP lines. */
public class RPWriter extends FlatFileWriter {

  private final Reference reference;

  public RPWriter(Entry entry, Reference reference, WrapType wrapType) {
    super(entry, wrapType);
    this.reference = reference;
    setWrapChar(WrapChar.WRAP_CHAR_COMMA);
  }

  public boolean write(Writer writer) throws IOException {
    String location = renderLocation(reference);
    writeBlock(writer, EmblPadding.RP_PADDING, location);
    return !location.isEmpty();
  }

  public static String renderLocation(Reference reference) {
    StringBuilder block = new StringBuilder();
    CompoundLocation<LocalRange> locations = reference.getLocations();
    boolean isFirstLocation = true;
    for (Location location : locations.getLocations()) {
      if (!isFirstLocation) {
        block.append(", ");
      } else {
        isFirstLocation = false;
      }
      block.append(location.getBeginPosition());
      block.append("-");
      block.append(location.getEndPosition());
    }
    return block.toString();
  }
}
