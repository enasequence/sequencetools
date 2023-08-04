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
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class XRefMatcher extends FlatFileMatcher {

  public XRefMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  private static final Pattern PATTERN =
      Pattern.compile(
          "^\\s*"
              + "([^;]+)"
              + // database
              "\\s*;\\s*"
              + "([^;]+)"
              + // primary accession
              "(\\s*;\\s*"
              + "([^;]+)?)?"
              + // secondary accession
              "\\s*.?\\s*$");

  private static int GROUP_DATABASE = 1;
  private static int GROUP_PRIMARY_ACCESSION = 2;
  private static int GROUP_SECONDARY_ACCESSION = 4;

  public boolean match(String string) {
    boolean matches = super.match(string);
    if (matches) {
      if (getString(GROUP_DATABASE) == null) {
        matches = false;
      } else if (getString(GROUP_PRIMARY_ACCESSION) == null) {
        matches = false;
      }
    }
    return matches;
  }

  public XRef getXRef() {
    XRef xref = (new EntryFactory()).createXRef();
    xref.setDatabase(FlatFileUtils.trimRight(getString(GROUP_DATABASE), '.'));
    xref.setPrimaryAccession(FlatFileUtils.trimRight(getString(GROUP_PRIMARY_ACCESSION), '.'));
    String secondaryAccession = getString(GROUP_SECONDARY_ACCESSION);
    if (secondaryAccession != null) {
      xref.setSecondaryAccession(FlatFileUtils.trimRight(secondaryAccession, '.'));
    }
    return xref;
  }
}
