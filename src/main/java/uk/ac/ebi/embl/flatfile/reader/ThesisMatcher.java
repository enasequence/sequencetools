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
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Thesis;

public class ThesisMatcher extends FlatFileMatcher {

  public ThesisMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  private static final Pattern PATTERN =
      Pattern.compile(
          "^\\s*Thesis\\s*"
              + "\\("
              + "\\s*"
              + "(\\d+)?"
              + // year
              "\\s*"
              + "\\)"
              + "\\s*"
              + ",?"
              + "\\s*"
              + "(.+)?$" // institute
          );

  private static int GROUP_YEAR = 1;
  private static int GROUP_INSTITUTE = 2;

  public Thesis getThesis(Publication publication) {
    Thesis thesis = null;
    if (publication != null) {
      thesis = (new ReferenceFactory()).createThesis(publication);
      thesis.setOrigin(publication.getOrigin());
    } else {
      thesis = (new ReferenceFactory()).createThesis();
    }
    thesis.setYear(getYear(GROUP_YEAR));
    thesis.setInstitute(getString(GROUP_INSTITUTE));
    return thesis;
  }
}
