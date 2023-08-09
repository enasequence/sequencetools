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
package uk.ac.ebi.embl.flatfile.reader;

import java.util.regex.Pattern;

public class XRefTaxonMatcher extends FlatFileMatcher {

  public XRefTaxonMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  private static final Pattern PATTERN = Pattern.compile("\\s*taxon\\s*:(\\w+)\\s*");

  private static final int GROUP_TAX_ID = 1;

  public Long getTaxId() {
    return getLong(GROUP_TAX_ID);
  }
}
