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

import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;

public class FeatureLocationMatcher extends FlatFileMatcher {

  public FeatureLocationMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  private static final Pattern PATTERN =
      Pattern.compile(
          "(\\s*complement\\s*\\()?\\s*(?:(\\w+)\\s*(?:\\.(\\d+))?\\s*\\:\\s*)?\\s*(<)?(?:(\\d+)?\\s*(?:((?:\\.\\.)?|(?:\\^))\\s*(>)?\\s*(\\d+))?)\\)?\\s*\\)?");

  private static final int GROUP_COMPLEMENT = 1;
  private static final int GROUP_ACCESSION = 2;
  private static final int GROUP_VERSION = 3;
  private static final int GROUP_LEFT_PARTIAL = 4;
  private static final int GROUP_BEGIN_POSITION = 5;
  private static final int GROUP_OPERATOR = 6;
  private static final int GROUP_RIGHT_PARTIAL = 7;
  private static final int GROUP_END_POSITION = 8;

  boolean fivePrime;
  boolean threePrime;

  public Location getLocation() {
    Location location = null;
    boolean isComplement = isValue(GROUP_COMPLEMENT);
    fivePrime = isValue(GROUP_LEFT_PARTIAL);
    threePrime = isValue(GROUP_RIGHT_PARTIAL);
    String accession = getString(GROUP_ACCESSION);
    Integer version = getInteger(GROUP_VERSION);
    LocationFactory locationFactory = new LocationFactory();
    String operator = getString(GROUP_OPERATOR);
    if (operator == null) {
      if (accession != null) {
        if (threePrime)
          location =
              locationFactory.createRemoteBase(accession, version, getLong(GROUP_END_POSITION));
        else
          location =
              locationFactory.createRemoteBase(accession, version, getLong(GROUP_BEGIN_POSITION));
      } else {
        if (threePrime) location = locationFactory.createLocalBase(getLong(GROUP_END_POSITION));
        else location = locationFactory.createLocalBase(getLong(GROUP_BEGIN_POSITION));
      }
    } else if (operator.equals("..")) {
      if (accession != null) {
        location =
            locationFactory.createRemoteRange(
                accession, version, getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
      } else {
        location =
            locationFactory.createLocalRange(
                getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
      }
    } else {
      if (accession != null) {
        location =
            locationFactory.createRemoteBetween(
                accession, version, getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
      } else {
        location =
            locationFactory.createLocalBetween(
                getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
      }
    }
    location.setComplement(isComplement);
    if (isComplement) {
      if (fivePrime) {
        location.setFivePrime(false);
        location.setThreePrime(true);
      }
      if (threePrime) {
        location.setFivePrime(true);
        location.setThreePrime(false);
      }
    } else {
      location.setFivePrime(fivePrime);
      location.setThreePrime(threePrime);
    }

    return location;
  }

  public boolean isFivePrime() {
    return fivePrime;
  }

  public boolean isThreePrime() {
    return threePrime;
  }
}
