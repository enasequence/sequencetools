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

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class FeatureLocationParser {

  private final FlatFileLineReader reader;
  private boolean isIgnoreLocationParseError = false;

  private static final Pattern BASE_PATTERN = Pattern.compile("(\\d+)");
  private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)(..)(\\d+)");

  Pattern COMPOUND_LOCATION_PATTERN =
      Pattern.compile("(?:(\\s*complement\\s*\\()?\\s*((?:join)|(?:order))?)?\\s*\\(?(.*)");

  Pattern INDIVIDUAL_LOCATION_PATTERN =
      Pattern.compile(
          "(\\s*complement\\s*\\()?\\s*(?:(\\w+)\\s*(?:\\.(\\d+))?\\s*\\:\\s*)?\\s*(<)?(?:(\\d+)?\\s*(?:((?:\\.\\.)?|(?:\\^))\\s*(>)?\\s*(\\d+))?)\\)?\\s*\\)?");

  public FeatureLocationParser(FlatFileLineReader reader, boolean ignoreParseError) {
    this.reader = reader;
    this.isIgnoreLocationParseError = ignoreParseError;
  }

  public CompoundLocation<Location> getCompoundLocation(String locationString) {

    Matcher compoundLocationMatcher = COMPOUND_LOCATION_PATTERN.matcher(locationString);

    if (!compoundLocationMatcher.matches()) {
      error("FT.4"); // Invalid feature location.
      return null;
    } else {
      String complement = compoundLocationMatcher.group(1); // Group 1: (\s*complement\s*\()?
      String operator = compoundLocationMatcher.group(2); // Group 2: (\s*((?:join)|(?:order)))?
      Vector<String> regions =
          FlatFileUtils.split(compoundLocationMatcher.group(3), ","); // Group 3: (.*)

      CompoundLocation<Location> compoundLocation = new Join<>();
      if (isValue(operator)) {
        if (operator.equals("order")) {
          compoundLocation = new Order<>();
        }
      }

      if (regions.size() == 1
          && (RANGE_PATTERN.matcher(regions.get(0)).matches()
              || BASE_PATTERN.matcher(regions.get(0)).matches())) {
        compoundLocation.setSimpleLocation(true);
      }

      if (isValue(complement)) {
        compoundLocation.setComplement(true);
      }

      for (String region : regions) {
        if (null != getLocation(region)) {
          compoundLocation.addLocation(getLocation(region));
        }
      }

      List<Location> locations = compoundLocation.getLocations();

      // Location partiality should be in first and last location only.
      IntStream.range(0, locations.size())
          .forEach(
              index -> {
                if (index != 0 && index != locations.size() - 1) {
                  if (locations.get(index).isThreePrime() || locations.get(index).isFivePrime()) {
                    if (!isIgnoreLocationParseError) {
                      error("FT.8", locations.get(index));
                    }
                  }
                }
              });

      if (locations.size() > 0) {
        // Set partiality using first location
        setCompoundLocationPartiality(locations.get(0), compoundLocation);
      }
      if (locations.size() > 1) {
        // Set partiality using last location
        setCompoundLocationPartiality(locations.get(locations.size() - 1), compoundLocation);
      }

      return compoundLocation;
    }
  }

  public Location getLocation(String locationRange) {

    Matcher individualLocationMatcher = INDIVIDUAL_LOCATION_PATTERN.matcher(locationRange);
    LocationFactory locationFactory = new LocationFactory();
    Location location = null;

    if (individualLocationMatcher.matches()) {
      boolean isComplement =
          isValue(individualLocationMatcher.group(1)); // Group 1: (\s*complement\s*\()?
      String accession = individualLocationMatcher.group(2); // Group 2: (\w+)
      String version = individualLocationMatcher.group(3); // Group 3: (\d+)
      boolean isFivePrime = isValue(individualLocationMatcher.group(4)); // Group 4: (<)?
      String beginingPosition = individualLocationMatcher.group(5); // Group 5: (\d+)?
      String operator = individualLocationMatcher.group(6); // Group 6: ((?:\.\.)?|(?:\^))
      boolean isThreePrime = isValue(individualLocationMatcher.group(7)); // Group 7: (>)?
      String endPosition = individualLocationMatcher.group(8); // Group 8: (\d+)?

      if (!isValue(operator)) {
        if (isValue(accession)) {
          if (isThreePrime)
            location =
                locationFactory.createRemoteBase(
                    accession, getInteger(version), getLong(endPosition));
          else
            location =
                locationFactory.createRemoteBase(
                    accession, getInteger(version), getLong(beginingPosition));
        } else {
          if (isThreePrime) location = locationFactory.createLocalBase(getLong(endPosition));
          else location = locationFactory.createLocalBase(getLong(beginingPosition));
        }
      } else if (operator.equals("..")) {
        if (isValue(accession)) {
          location =
              locationFactory.createRemoteRange(
                  accession, getInteger(version), getLong(beginingPosition), getLong(endPosition));
        } else {
          location =
              locationFactory.createLocalRange(getLong(beginingPosition), getLong(endPosition));
        }
      } else {
        if (isValue(accession)) {
          location =
              locationFactory.createRemoteBetween(
                  accession, getInteger(version), getLong(beginingPosition), getLong(endPosition));
        } else {
          location =
              locationFactory.createLocalBetween(getLong(beginingPosition), getLong(endPosition));
        }
      }

      location.setComplement(isComplement);
      location.setFivePrime(isFivePrime);
      location.setThreePrime(isThreePrime);
      setLocationPartiality(location);
    }

    return location;
  }

  private void setLocationPartiality(Location location) {
    if (location.isComplement() && (location.isFivePrime() || location.isThreePrime())) {
      // Swap 3 prime and 5 prime in case of complement
      boolean tempFivePrime = location.isFivePrime();
      location.setFivePrime(location.isThreePrime());
      location.setThreePrime(tempFivePrime);
    }
  }

  private void setCompoundLocationPartiality(Location location, CompoundLocation compoundLocation) {
    if (compoundLocation.isComplement()) {
      if (location.isThreePrime()) {
        compoundLocation.setFivePrime(true);
      }
      if (location.isFivePrime()) {
        compoundLocation.setThreePrime(true);
      }
    } else {
      if (location.isFivePrime()) {
        compoundLocation.setFivePrime(true);
      }
      if (location.isThreePrime()) {
        compoundLocation.setThreePrime(true);
      }
    }
  }

  public boolean isValue(String value) {
    return StringUtils.isNotEmpty(value);
  }

  public Integer getInteger(String value) {
    if (value == null) {
      return null;
    }
    value = value.trim();
    if (value.length() == 0) {
      return null;
    }
    Integer number = null;
    try {
      number = Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      error("FF.3");
    }
    return number;
  }

  public Long getLong(String value) {
    if (value == null) {
      return null;
    }
    value = value.trim();
    if (value.length() == 0) {
      return null;
    }
    Long number = null;
    try {
      number = Long.parseLong(value);
    } catch (NumberFormatException ex) {
      error("FF.3");
    }
    return number;
  }

  protected void error(String messageKey, Object... params) {
    reader.error(messageKey, params);
  }
}
