/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.entry.qualifier;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class Rpt_Unit_RangeQualifier extends Qualifier implements Serializable,
		LocationQualifier {

	// private static final long serialVersionUID = -4130304812449439118L;

	private static final Pattern PATTERN = Pattern
			.compile("^\\s*(\\d+)\\s*(\\.\\.)\\s*(\\d+)\\s*$");

	public Rpt_Unit_RangeQualifier(String value) {
		super(RPT_UNIT_RANGE_QUALIFIER_NAME, value);
	}

	public Location getLocation() throws ValidationException {
		if (getValue() == null) {
			return null;
		}
		Matcher matcher = PATTERN.matcher(getValue());
		if (!matcher.matches()) {
			throwValueException();
		}
		LocationFactory factory = new LocationFactory();
		String beginPositionStr = matcher.group(1);
		long beginPosition = Long.parseLong(beginPositionStr);

		String endPositionStr = matcher.group(3);

		long endPosition = Long.parseLong(endPositionStr);
		return factory.createLocalRange(beginPosition, endPosition);

	}

	public boolean setLocation(Location location) throws ValidationException {
		if (getValue() == null) {
			return false;
		}
		Matcher matcher = PATTERN.matcher(getValue());
		if (!matcher.matches()) {
			throwValueException();
		}
		String newRptUnitRangeValue = matcher.replaceFirst(location
				.getBeginPosition().toString()
				+ "$2"
				+ location.getEndPosition().toString());
		setValue(newRptUnitRangeValue);

		return true;
	}
}
