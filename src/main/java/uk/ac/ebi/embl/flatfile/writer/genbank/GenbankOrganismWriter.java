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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the organism lines. */
public class GenbankOrganismWriter extends FlatFileWriter {

  private final SourceFeature sourceFeature;

  public GenbankOrganismWriter(Entry entry, SourceFeature sourceFeature, WrapType wrapType) {
    super(entry, wrapType);
    this.sourceFeature = sourceFeature;
  }

  public boolean write(Writer writer) throws IOException {
    boolean writeBlock = false;
    writeBlock |= new SourceWriter(entry, sourceFeature, wrapType).write(writer);
    writeBlock |= new OrganismWriter(entry, sourceFeature, wrapType).write(writer);
    return writeBlock;
  }
}
