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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.util.regex.Pattern;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the EMBL CDS flat file OS lines. */
public class OXReader extends SingleLineBlockReader {

  private static final Pattern PATTERN = Pattern.compile("^\\s*NCBI_TaxID\\s*=\\s*(\\d+);?");

  private static final int GROUP_TAX_ID = 1;

  public OXReader(LineReader lineReader) {
    super(lineReader);
  }

  @Override
  public String getTag() {
    return EmblTag.OX_TAG;
  }

  @Override
  protected void read(String block) {
    FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
    if (!matcher.match(block)) {
      error("FF.1", getTag());
      return;
    }
    Long taxId = matcher.getLong(GROUP_TAX_ID);
    getCache().setTaxId(taxId);
  }
}
