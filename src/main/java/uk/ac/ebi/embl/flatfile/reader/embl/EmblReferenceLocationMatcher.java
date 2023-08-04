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

import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

public class EmblReferenceLocationMatcher extends FlatFileMatcher {

  public EmblReferenceLocationMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  private static final Pattern PATTERN = Pattern.compile("\\s*(\\d+)\\s*-(\\d+)\\s*");

  private static final int GROUP_BEGIN_POSITION = 1;
  private static final int GROUP_END_POSITION = 2;

  public LocalRange getLocation() {
    LocalRange location = null;
    location =
        (new LocationFactory())
            .createLocalRange(getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
    return location;
  }
}
