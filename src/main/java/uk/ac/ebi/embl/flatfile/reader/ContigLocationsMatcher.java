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
package uk.ac.ebi.embl.flatfile.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class ContigLocationsMatcher extends FlatFileMatcher {

  public ContigLocationsMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
    this.reader = reader;
  }

  private final FlatFileLineReader reader;

  private static final Pattern PATTERN = Pattern.compile("\\s*join\\s*\\(?(.*)");

  private static final int GROUP_ELEMENTS = 1;

  public List<Location> getLocations() {
    List<Location> locations = new ArrayList<Location>();
    Vector<String> element = FlatFileUtils.split(getString(GROUP_ELEMENTS), ",");
    int elementCount = element.size();
    if (elementCount == 0) {
      error("FF.1", EmblTag.CO_TAG); // Invalid line.
      return null;
    }
    for (int i = 0; i < elementCount; ++i) {
      ContigLocationMatcher contigLocationMatcher = new ContigLocationMatcher(reader);
      if (!contigLocationMatcher.match(element.get(i))) {
        error("CO.1", element.get(i)); // Invalid contig location.
        return null;
      }
      Location location = contigLocationMatcher.getLocation();
      locations.add(location);
    }

    return locations;
  }
}
