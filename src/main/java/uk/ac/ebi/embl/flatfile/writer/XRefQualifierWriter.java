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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.flatfile.EmblPadding;

/** Flat file writer for the database cross-reference qualifiers on the FT lines. */
public class XRefQualifierWriter extends FlatFileWriter {

  private final XRef xref;

  public XRefQualifierWriter(Entry entry, XRef xref) {
    super(entry);
    this.xref = xref;
  }

  public boolean write(Writer writer) throws IOException {
    if (xref == null) {
      return false;
    }
    StringBuilder line = new StringBuilder();
    line.append(EmblPadding.QUALIFIER_PADDING);
    line.append("/db_xref=\"");
    if (!isBlankString(xref.getDatabase())) {
      line.append(xref.getDatabase());
    }
    line.append(":");
    if (!isBlankString(xref.getPrimaryAccession())) {
      line.append(xref.getPrimaryAccession());
    }
    line.append("\"\n");
    writer.write(line.toString());
    return true;
  }
}
