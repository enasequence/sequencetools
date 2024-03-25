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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class FeatureLocationsMatcher extends FlatFileMatcher {

  private boolean isIgnoreLocationParseError = false;

  public FeatureLocationsMatcher(FlatFileLineReader reader, boolean ignoreParseError) {
    super(reader, PATTERN);
    this.reader = reader;
    this.isIgnoreLocationParseError = ignoreParseError;
  }

  private final FlatFileLineReader reader;
  private static final Pattern BASE_PATTERN = Pattern.compile("(\\d+)");
  private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)(..)(\\d+)");

  /*private static final Pattern PATTERN =
  Pattern.compile("(\\s*complement\\s*\\()?\\s*((?:join)|(?:order))?\\s*\\(?(.*)");*/
  private static final Pattern PATTERN =
      Pattern.compile("(?:(\\s*complement\\s*\\()?\\s*((?:join)|(?:order)))?\\s*\\(?(.*)");

  private static final int GROUP_COMPLEMENT = 1;
  private static final int GROUP_OPERATOR = 2;
  private static final int GROUP_ELEMENTS = 3;

  public CompoundLocation<Location> getCompoundLocation() {
    CompoundLocation<Location> compoundLocation = new Join<>();
    if (isValue(GROUP_OPERATOR)) {
      if (getString(GROUP_OPERATOR).equals("order")) {
        compoundLocation = new Order<>();
      }
    }
    if (isValue(GROUP_COMPLEMENT)) {
      compoundLocation.setComplement(true);
    }

    Vector<String> element = FlatFileUtils.split(getString(GROUP_ELEMENTS), ",");
    int elementCount = element.size();
    if (elementCount == 0) {
      error("FT.4"); // Invalid feature location.
      return null;
    }

    if (elementCount == 1
        && (RANGE_PATTERN.matcher(element.get(0)).matches()
            || BASE_PATTERN.matcher(element.get(0)).matches())) {
      compoundLocation.setSimpleLocation(true);
    }

    for (int i = 0; i < elementCount; ++i) {
      FeatureLocationMatcher featureLocationMatcher = new FeatureLocationMatcher(reader);
      if (!featureLocationMatcher.match(element.get(i))) {
        error("FT.8", element.get(i)); // Invalid feature location.
        return null;
      }
      Location location = featureLocationMatcher.getLocation();
      boolean isComplement = location.isComplement();

      /* if (featureLocationMatcher.isFivePrime()) {
        location.setFivePrime(true);
        if (isComplement) {
          if (i != 0 && !isIgnoreLocationParseError) {
            error("FT.8", element.get(i));
            return null;
          } else if (i == 0) {
            location.setThreePrime(true);
            location.setFivePrime(false);
          }
        }
      } if (featureLocationMatcher.isThreePrime()) {
        location.setThreePrime(true);
        if (isComplement) {
          if (i != elementCount - 1 && !isIgnoreLocationParseError) {
            error("FT.8", element.get(i));
            return null;
          } else if (i == elementCount - 1) {
            location.setFivePrime(true);
            location.setThreePrime(false);
          }
        }
      }*/

      compoundLocation.addLocation(location);
    }

    Location firstLocation = compoundLocation.getLocations().get(0);
    Location lastLocation =
        compoundLocation.getLocations().get(compoundLocation.getLocations().size() - 1);

    // setCompoundLocationPartiality(compoundLocation,firstLocation);
    // setCompoundLocationPartiality(compoundLocation,lastLocation);

    /*    boolean fivePrime = false;
        boolean threePrime = false;
        if (firstLocation.isFivePrime()) {
          fivePrime = true;
          if (compoundLocation.isComplement()) {
            fivePrime = false;
            threePrime = true;
          }
        } else if (firstLocation.isThreePrime()) {
          threePrime = true;
          if (compoundLocation.isComplement()) {
            threePrime = false;
            fivePrime = true;
          }
        }


        if (lastLocation.isFivePrime()) {
          fivePrime = true;
          if (compoundLocation.isComplement()) {
            fivePrime = false;
            threePrime = true;
          }
        } else if (lastLocation.isThreePrime()) {
          threePrime = true;
          if (compoundLocation.isComplement()) {
            threePrime = false;
            fivePrime = true;
          }
        }

        compoundLocation.setRightPartial(threePrime);
        compoundLocation.setLeftPartial(fivePrime);
    */

    return compoundLocation;
  }

  private void setLocationPartiality(Location location) {
    if (location.isComplement() && (location.isFivePrime() || location.isThreePrime())) {
      // Swap 3 prime and 5 prime
      boolean temp = location.isFivePrime();
      location.setFivePrime(location.isThreePrime());
      location.setThreePrime(temp);
    }
  }

  private void setCompoundLocationPartiality(Location location, CompoundLocation compoundLocation) {
    if (compoundLocation.isComplement() && (location.isFivePrime() || location.isThreePrime())) {
      // Swap 3 prime and 5 prime
      boolean temp = location.isFivePrime();
      compoundLocation.setFivePrime(location.isThreePrime());
      compoundLocation.setThreePrime(temp);
    } else {
      if (location.isFivePrime()) {
        compoundLocation.setFivePrime(location.isFivePrime());
      }
      if (location.isThreePrime()) {
        compoundLocation.setThreePrime(location.isThreePrime());
      }
    }
  }

  public CompoundLocation<Location> getCompoundLocation(String locationString) {
    Pattern compoundLocationPattern =
        Pattern.compile("(?:(\\s*complement\\s*\\()?\\s*((?:join)|(?:order))?)?\\s*\\(?(.*)");
    Matcher matcher = compoundLocationPattern.matcher(locationString);

    if (matcher.matches()) {
      String complement = matcher.group(1); // Group 1: (\s*complement\s*\()?
      String operator = matcher.group(2); // Group 2: (\s*((?:join)|(?:order)))?
      String[] regions = matcher.group(3).split(","); // Group 3: (.*)

      CompoundLocation<Location> compoundLocation = new Join<>();
      if (isValue(operator)) {
        if (operator.equals("order")) {
          compoundLocation = new Order<>();
        }
      }

      if (regions.length == 1
          && (RANGE_PATTERN.matcher(regions[0]).matches()
              || BASE_PATTERN.matcher(regions[0]).matches())) {
        compoundLocation.setSimpleLocation(true);
      }

      if (isValue(complement)) {
        compoundLocation.setComplement(true);
      }

      for (String region : regions) {
        Location location = getLocation(region);
        compoundLocation.addLocation(location);
      }

      setCompoundLocationPartiality(compoundLocation.getLocations().get(0), compoundLocation);
      if (compoundLocation.getLocations().size() > 0) {
        setCompoundLocationPartiality(
            compoundLocation.getLocations().get(compoundLocation.getLocations().size() - 1),
            compoundLocation);
      }

      return compoundLocation;
    }

    return null;
  }

  public Location getLocation(String locationRange) {
    Pattern individualLocationPattern =
        Pattern.compile(
            "(\\s*complement\\s*\\()?\\s*(?:(\\w+)\\s*(?:\\.(\\d+))?\\s*\\:\\s*)?\\s*(<)?(?:(\\d+)?\\s*(?:((?:\\.\\.)?|(?:\\^))\\s*(>)?\\s*(\\d+))?)\\)?\\s*\\)?");

    Matcher matcher = individualLocationPattern.matcher(locationRange);
    LocationFactory locationFactory = new LocationFactory();
    Location location = null;

    if (matcher.matches()) {
      boolean isComplement = isValue(matcher.group(1)); // Group 1: (\s*complement\s*\()?
      String accession = matcher.group(2); // Group 2: (\w+)
      String version = matcher.group(3); // Group 3: (\d+)
      boolean isFivePrime = isValue(matcher.group(4)); // Group 4: (<)?
      String beginingPosition = matcher.group(5); // Group 5: (\d+)?
      String operator = matcher.group(6); // Group 6: ((?:\.\.)?|(?:\^))
      boolean isThreePrime = isValue(matcher.group(7)); // Group 7: (>)?
      String endPosition = matcher.group(8); // Group 8: (\d+)?

      if (!isValue(operator)) {
        if (accession != null) {
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
        if (accession != null) {
          location =
              locationFactory.createRemoteRange(
                  accession, getInteger(version), getLong(beginingPosition), getLong(endPosition));
        } else {
          location =
              locationFactory.createLocalRange(getLong(beginingPosition), getLong(endPosition));
        }
      } else {
        if (accession != null) {
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

  public boolean isValue(String value) {
    return StringUtils.isNotEmpty(value);
  }
}
