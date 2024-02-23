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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;
import uk.ac.ebi.embl.flatfile.reader.OrganismMatcher;

public class OrganismReader extends MultiLineBlockReader {

  public OrganismReader(LineReader lineReader) {
    super(lineReader, ConcatenateType.CONCATENATE_BREAK);
  }

  @Override
  public String getTag() {
    return GenbankTag.ORGANISM_TAG;
  }

  @Override
  protected void read(String block) {
    boolean isFirstLine = true;
    StringBuilder str = new StringBuilder();
    for (String line : FlatFileUtils.split(block, "\n")) {
      if (isFirstLine) {
        isFirstLine = false;
        OrganismMatcher organismMatcher = new OrganismMatcher(this);
        if (organismMatcher.match(line)) {
          getCache().setScientificName(organismMatcher.getScientificName());
          String commonName = organismMatcher.getCommonName();
          if (commonName != null) {
            getCache().setCommonName(commonName);
          }
        } else {
          getCache().setScientificName(line);
        }
      } else {
        str.append(" ");
        str.append(line);
      }
    }
    String lineage = str.toString();
    lineage = FlatFileUtils.trimLeft(lineage);
    lineage = FlatFileUtils.trimRight(lineage, '.');
    getCache().setLineage(lineage);
  }
}
