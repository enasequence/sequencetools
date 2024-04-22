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
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;

public class EmblPersonEndInitialMatcher extends EmblPersonMatcher {

  private static final int GROUP_NAME = 1;
  private static final int GROUP_INITIAL = 2;

  public EmblPersonEndInitialMatcher(FlatFileLineReader reader) {
    super(reader, END_INITIAL_PATTERN);
  }

  public static final Pattern END_INITIAL_PATTERN =
      Pattern.compile(
          "^([^\\.]+)" // Name
              + "(\\s+[^\\s\\.]*\\s*\\..*)?$"); // Initial

  @Override
  public Person getPerson() {
    // GROUP_INITIAL must be mapped in initial
    String initial = getString(GROUP_INITIAL);
    if (initial != null) {
      initial = FlatFileUtils.shrink(initial.trim(), '.');
    }
    // GROUP_NAME must be mapped in surname
    String surame = getString(GROUP_NAME);
    if (surame != null) {
      surame = FlatFileUtils.shrink(surame.trim(), '.');
    }
    return (new ReferenceFactory()).createPerson(surame, initial);
  }
}
