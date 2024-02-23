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

import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file REFERENCE lines. */
public class ReferenceReader extends MultiLineBlockReader {

  public ReferenceReader(LineReader lineReader) {
    super(lineReader, ConcatenateType.CONCATENATE_SPACE);
  }

  private static final Pattern PATTERN =
      Pattern.compile(
          "^\\s*"
              + "(\\d+)"
              + // reference number
              "\\s*"
              + "(?:\\(([^\\)]+)\\))?"
              + // reference location
              "$");

  private static final int GROUP_REFERENCE_NUMBER = 1;
  private static final int GROUP_REFERENCE_LOCATION = 2;

  @Override
  public String getTag() {
    return GenbankTag.REFERENCE_TAG;
  }

  @Override
  protected void read(String block) {
    FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
    if (!matcher.match(block)) {
      error("FF.1", getTag());
      return;
    }
    getCache().resetReferenceCache();
    Reference reference = getCache().getReference();
    int referenceNumber = matcher.getInteger(GROUP_REFERENCE_NUMBER);
    if (referenceNumber > 0) {
      reference.setReferenceNumber(referenceNumber);
      reference.setOrigin(getOrigin());
    } else {
      error("RN.1", referenceNumber);
    }

    if (matcher.isValue(GROUP_REFERENCE_LOCATION)) {
      GenbankReferenceLocationMatcher locationMatcher = new GenbankReferenceLocationMatcher(this);
      CompoundLocation<LocalRange> locations = reference.getLocations();
      if (locations.getOrigin() == null) {
        locations.setOrigin(getOrigin());
      }
      for (String value : FlatFileUtils.split(matcher.getString(GROUP_REFERENCE_LOCATION), ";")) {
        if (!locationMatcher.match(value)) {
          // warning("RP.1", value);
        } else {
          reference.getLocations().addLocation(locationMatcher.getLocation());
          reference.setLocationExists(true);
        }
      }
    }
  }
}
