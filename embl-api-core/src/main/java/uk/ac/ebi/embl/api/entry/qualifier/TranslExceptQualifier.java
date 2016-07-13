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

public class TranslExceptQualifier extends Qualifier implements Serializable,
		LocationQualifier {

	private static final long serialVersionUID = -4130304812449439118L;

	private static final Pattern PATTERN = Pattern
			.compile("^\\s*\\(\\s*pos\\s*:\\s*(?:(\\d+)(?:\\s*\\.\\.\\s*(\\d+)){0,1})\\s*,\\s*aa\\s*:\\s*([^)\\s]+)\\s*\\)\\s*$");
	public static final Pattern COMPATTERN = Pattern
			.compile("^\\s*\\(\\s*pos\\s*:\\s*(?:complement\\s*\\(\\s*(?:(\\d+)(?:\\s*\\.\\.\\s*(\\d+)){0,1})\\s*\\))\\s*,\\s*aa\\s*:\\s*([^)\\s]+)\\s*\\)\\s*$");

	public TranslExceptQualifier(String value) {
		super(TRANSL_EXCEPT_QUALIFIER_NAME, value);
	}

	public Location getLocation() throws ValidationException {
		if (getValue() == null) {
			return null;
		}
		Matcher matcher = PATTERN.matcher(getValue());
		if (!matcher.matches())
		{
			matcher = COMPATTERN.matcher(getValue());
			if (!matcher.matches())
			{
				throwValueException();
			}
		}
		LocationFactory factory = new LocationFactory();
		String beginPositionStr = matcher.group(1);
		long beginPosition = Long.parseLong(beginPositionStr);
		if (matcher.group(2) != null) {
			String endPositionStr = matcher.group(2);

			long endPosition = Long.parseLong(endPositionStr);
			return factory.createLocalRange(beginPosition, endPosition);
		} else {
			return factory.createLocalBase(beginPosition);
		}
	}

	public AminoAcid getAminoAcid() throws ValidationException {
		if (getValue() == null) {
			return null;
		}
		Matcher matcher = PATTERN.matcher(getValue());
		if (!matcher.matches())
		{
			matcher = COMPATTERN.matcher(getValue());
			if (!matcher.matches())
			{
				throwValueException();
			}
		}
		AminoAcidFactory factory = new AminoAcidFactory();
		return factory.createAminoAcid(matcher.group(3));
	}

	public boolean setLocation(Location location) throws ValidationException {
		if (getValue() == null) {
			return false;
		}
		
		Matcher matcher = PATTERN.matcher(getValue());
		String trans_value=getValue();
		
		if (!matcher.matches())
		{
			matcher = COMPATTERN.matcher(getValue());
			if (!matcher.matches())
			{
				throwValueException();
			}
		}
		trans_value.replace(matcher.group(1),location.getBeginPosition().toString());
		if(matcher.group(2)!=null)
		{
			trans_value.replace(matcher.group(2),location.getEndPosition().toString());
		}
		setValue(trans_value);
		return true;
	}
}
