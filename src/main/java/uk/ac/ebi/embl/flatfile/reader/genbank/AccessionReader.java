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

import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file ACCESSION line. Accession number ranges will not be expanded. */
public class AccessionReader extends MultiLineBlockReader {

  public AccessionReader(LineReader lineReader) {
    super(lineReader, ConcatenateType.CONCATENATE_SPACE);
  }

  @Override
  public String getTag() {
    return GenbankTag.ACCESSION_TAG;
  }

  @Override
  protected void read(String block) {
    boolean isFirstAccession = true;
    for (String accession : FlatFileUtils.split(block, " ")) {
      if (isFirstAccession) {
        if (!accession.equals("XXX")) {
          entry.setPrimaryAccession(accession);
          entry.getSequence().setAccession(accession);
        }
        isFirstAccession = false;
      } else {
        entry.addSecondaryAccession(new Text(accession, getOrigin()));
      }
    }
  }
}
