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
package uk.ac.ebi.embl.api.entry.qualifier;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.helper.location.LocationStringParser;

public class AnticodonQualifier extends Qualifier
    implements Serializable, CompoundLocationQualifier {

  private static final long serialVersionUID = -4130304812449439118L;

  private static final Pattern PATTERN =
      Pattern.compile(
          "^(\\s*\\((pos:)\\s*(.*)\\s*(,\\s*aa\\s*:\\s*)([^\\s\\,]+)(,\\s*seq\\s*:\\s*)?([^\\s]+)?\\s*\\))$");

  public AnticodonQualifier(String value) {
    super(ANTICODON_QUALIFIER_NAME, value);
  }

  public CompoundLocation<Location> getLocations() throws ValidationException {
    if (getValue() == null) {
      return null;
    }
    Matcher matcher = PATTERN.matcher(getValue());
    if (!matcher.matches()) {
      throwValueException();
    }
    String locationStr = matcher.group(3);

    LocationStringParser locationParser = new LocationStringParser(this);

    CompoundLocation<Location> location = locationParser.getCompoundLocation(locationStr, false);

    if (location == null) {
      throwValueException();
    }

    return location;
  }

  public AminoAcid getAminoAcid() throws ValidationException {
    if (getValue() == null) {
      return null;
    }
    Matcher matcher = PATTERN.matcher(getValue());
    if (!matcher.matches()) {
      throwValueException();
    }

    AminoAcidFactory factory = new AminoAcidFactory();

    String aa = null;

    if (matcher.group(5) != null) {
      aa = factory.getAbbreviation(matcher.group(5));
    }
    return factory.createAminoAcid(aa);
  }

  public String getSequence() throws ValidationException {
    if (getValue() == null) {
      return null;
    }
    Matcher matcher = PATTERN.matcher(getValue());
    if (!matcher.matches()) {
      throwValueException();
    }

    return matcher.group(7);
  }

  public String getAminoAcidString() throws ValidationException {
    if (getValue() == null) {
      return null;
    }
    Matcher matcher = PATTERN.matcher(getValue());
    if (!matcher.matches()) {
      throwValueException();
    }

    return matcher.group(5);
  }

  public String getValue(String pos, String aminoAcid, String seq) {
    return "(pos:" + pos + ",aa:" + aminoAcid + ",seq:" + seq + ")";
  }

  @Override
  public boolean setLocations(CompoundLocation<Location> location) throws ValidationException {
    // TODO Auto-generated method stub
    return false;
  }
}
