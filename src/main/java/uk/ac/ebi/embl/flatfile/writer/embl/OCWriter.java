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
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

/** Flat file writer for the OC lines. */
public class OCWriter extends FlatFileWriter {

  public OCWriter(Entry entry, SourceFeature sourceFeature, WrapType wrapType) {
    super(entry, wrapType);
    this.sourceFeature = sourceFeature;
    setWrapChar(WrapChar.WRAP_CHAR_SEMICOLON);
  }

  private final SourceFeature sourceFeature;

  public boolean write(Writer writer) throws IOException {
    Taxon taxon = sourceFeature.getTaxon();
    if (taxon != null && taxon.getLineage() != null && taxon.getFamilyNames().size() > 0) {
      StringBuilder block = new StringBuilder();
      boolean isFirstTaxon = true;
      for (String familyName : taxon.getFamilyNames()) {
        if (!isFirstTaxon) {
          block.append("; ");
        } else {
          isFirstTaxon = false;
        }
        block.append(familyName);
      }
      block.append(".");
      writeBlock(writer, EmblPadding.OC_PADDING, block.toString());
    } else {
      writeBlock(writer, EmblPadding.OC_PADDING, "unclassified sequences.");
    }
    return true;
  }
}
