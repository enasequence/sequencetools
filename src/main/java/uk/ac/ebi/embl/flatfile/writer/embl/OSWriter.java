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
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

/** Flat file writer for the OS lines. */
public class OSWriter extends FlatFileWriter {

  public OSWriter(Entry entry, SourceFeature sourceFeature, WrapType wrapType) {
    super(entry, wrapType);
    this.sourceFeature = sourceFeature;
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
  }

  private final SourceFeature sourceFeature;

  public boolean write(Writer writer) throws IOException {
    Taxon taxon = sourceFeature.getTaxon();
    StringBuilder block = new StringBuilder();
    if (taxon != null) {
      block.append(taxon.getScientificName());
      if (!isBlankString(taxon.getCommonName())) {
        block.append(" (");
        block.append(taxon.getCommonName());
        block.append(")");
      }
    } else if (!isBlankString(sourceFeature.getScientificName())) {
      block.append((sourceFeature.getScientificName()));
    }
    writeBlock(writer, EmblPadding.OS_PADDING, block.toString());
    return true;
  }
}
