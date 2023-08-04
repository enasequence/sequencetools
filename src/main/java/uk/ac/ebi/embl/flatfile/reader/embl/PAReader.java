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
package uk.ac.ebi.embl.flatfile.reader.embl;

import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the EMBL CDS flat file PA lines. */
public class PAReader extends SingleLineBlockReader {

  public PAReader(LineReader lineReader) {
    super(lineReader);
  }

  @Override
  public String getTag() {
    return EmblTag.PA_TAG;
  }

  @Override
  protected void read(String block) {
    XRef xref = new XRef("EMBL", block);
    xref.setOrigin(getOrigin());
    entry.addXRef(xref);
  }
}
