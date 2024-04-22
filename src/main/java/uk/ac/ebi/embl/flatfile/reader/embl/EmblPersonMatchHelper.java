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

import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;

public class EmblPersonMatchHelper {

  FlatFileLineReader reader;

  public EmblPersonMatchHelper(FlatFileLineReader reader) {
    this.reader = reader;
  }

  private EmblPersonMatcher personMatcher;

  public Person getPerson() {
    return personMatcher.getPerson();
  }

  public boolean match(String string) {
    EmblPersonMatcher startInitialPersonMatcher = new EmblPersonStartInitialMatcher(reader);
    EmblPersonMatcher endInitialPersonMatcher = new EmblPersonEndInitialMatcher(reader);

    if (!startInitialPersonMatcher.match(string)) {
      if (endInitialPersonMatcher.match(string)) {
        personMatcher = endInitialPersonMatcher;
        return true;
      }
    } else {
      personMatcher = startInitialPersonMatcher;
      return true;
    }
    return false;
  }
}
