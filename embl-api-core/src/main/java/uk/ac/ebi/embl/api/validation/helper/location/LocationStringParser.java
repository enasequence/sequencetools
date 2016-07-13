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
package uk.ac.ebi.embl.api.validation.helper.location;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.helper.Utils;

public class LocationStringParser
{
	private static final Pattern PATTERN = Pattern.compile("(?:(\\s*complement\\s*\\()?\\s*((?:join)|(?:order)))?\\s*\\(?(.*)");
	private static final Pattern simpleLocationPattern = Pattern.compile("(\\d+)(..)(\\d+)");
	private Matcher matcher;
	private static final int GROUP_COMPLEMENT = 1;
	private static final int GROUP_OPERATOR = 2;
	private static final int GROUP_ELEMENTS = 3;
	boolean leftPartial;
	boolean rightPartial;
	Object object;

	public LocationStringParser()
	{
	}

	public LocationStringParser(Object obj)
	{
		this.object = obj;
	}
	
	public CompoundLocation<Location> getCompoundLocation(String locationString,boolean ignoreError) throws ValidationException
	{
		CompoundLocation<Location> compoundLocation = new Join<Location>();
		
		if(matcher!=null)
        {
       	 matcher=null;
        }

		if (!match(locationString, PATTERN))
		{
			throwValueException(object);
		}

		boolean isComplement = isValue(GROUP_COMPLEMENT);

		if (isValue(GROUP_OPERATOR))
		{
			if (getString(GROUP_OPERATOR).equals("order"))
			{
				compoundLocation = new Order<Location>();
			}
		}
		if (isComplement)
		{
			compoundLocation.setComplement(true);
		}

		Vector<String> element = Utils.split(getString(GROUP_ELEMENTS), ",");
		int elementCount = element.size();
		if (elementCount == 0)
		{
			// Invalid location.
			return null;
		}
		if (elementCount == 1 && simpleLocationPattern.matcher(element.get(0)).matches())
		{
			compoundLocation.setSimpleLocation(true);
		}
		for (int i = 0; i < elementCount; ++i)
		{

			if (!match(element.get(i), PATTERN))
			{
				// Invalid location.
				return null;
			}
			Location location = getLocation(element.get(i));
			if (isLeftPartial())
			{
				if (!location.isComplement() && i == 0)
				{
					compoundLocation.setLeftPartial(true);
				} else if (location.isComplement() && i == elementCount - 1)
				{
					compoundLocation.setRightPartial(true);
				} else
				{
					// Invalid location.
					if(!ignoreError)
					return null;
				}
			}
			if (isRightPartial())
			{
				if (location.isComplement() && i == 0)
				{
					compoundLocation.setLeftPartial(true);
				} else if (!location.isComplement() && i == elementCount - 1)
				{
					compoundLocation.setRightPartial(true);
				} else
				{
					// Invalid location.
					if(!ignoreError)
					return null;
				}
			}
			compoundLocation.addLocation(location);
		}

		return compoundLocation;
	}

	public boolean isValue(int group)
	{
		return matcher.group(group) != null;
	}

	public boolean match(String string, Pattern pattern)
	{
		matcher = pattern.matcher(string);
		return matcher.matches();
	}

	public String getString(int group)
	{
		String value = matcher.group(group);
		if (value == null)
		{
			return null;
		}
		value = value.trim();
		if (value.length() == 0)
		{
			return null;
		}
		return value;
	}

	public Location getLocation(String locationString) throws ValidationException
	{

		final Pattern PATTERN = Pattern
				.compile("(\\s*complement\\s*\\()?\\s*(?:(\\w+)\\s*(?:\\.(\\d+))?\\s*\\:\\s*)?\\s*(<)?(?:(\\d+)\\s*(?:((?:\\.\\.)|(?:\\^))\\s*(>)?\\s*(\\d+))?)\\)?\\s*\\)?");

		final int GROUP_COMPLEMENT = 1;
		final int GROUP_ACCESSION = 2;
		final int GROUP_VERSION = 3;
		final int GROUP_LEFT_PARTIAL = 4;
		final int GROUP_BEGIN_POSITION = 5;
		final int GROUP_OPERATOR = 6;
		final int GROUP_RIGHT_PARTIAL = 7;
		final int GROUP_END_POSITION = 8;
		Location location = null;
         if(matcher!=null)
         {
        	 matcher=null;
         }
		if (!match(locationString, PATTERN))
		{
			throwValueException(object);
		}

		boolean isComplement = isValue(GROUP_COMPLEMENT);
		leftPartial = isValue(GROUP_LEFT_PARTIAL);
		rightPartial = isValue(GROUP_RIGHT_PARTIAL);
		String accession = getString(GROUP_ACCESSION);
		Integer version = getInteger(GROUP_VERSION);
		LocationFactory locationFactory = new LocationFactory();
		String operator = getString(GROUP_OPERATOR);
		if (operator == null)
		{
			if (accession != null)
			{
				location = locationFactory.createRemoteBase(accession, version, getLong(GROUP_BEGIN_POSITION));
			} else
			{
				location = locationFactory.createLocalBase(getLong(GROUP_BEGIN_POSITION));
			}
		} else if (operator.equals(".."))
		{
			if (accession != null)
			{
				location = locationFactory.createRemoteRange(accession, version, getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
			} else
			{
				location = locationFactory.createLocalRange(getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
			}
		} else
		{
			if (accession != null)
			{
				location = locationFactory.createRemoteBetween(accession, version, getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
			} else
			{
				location = locationFactory.createLocalBetween(getLong(GROUP_BEGIN_POSITION), getLong(GROUP_END_POSITION));
			}
		}
		location.setComplement(isComplement);
		return location;
	}

	public boolean isLeftPartial()
	{
		return leftPartial;
	}

	public boolean isRightPartial()
	{
		return rightPartial;
	}

	public Integer getInteger(int group) throws ValidationException
	{
		String value = matcher.group(group);
		if (value == null)
		{
			return null;
		}
		value = value.trim();
		if (value.length() == 0)
		{
			return null;
		}
		Integer number = null;
		try
		{
			number = Integer.parseInt(matcher.group(group));
		} catch (NumberFormatException ex)
		{
			throwValueException(object);
			// invalid location
		}
		return number;
	}

	public Long getLong(int group) throws ValidationException
	{
		String value = matcher.group(group);
		if (value == null)
		{
			return null;
		}
		value = value.trim();
		if (value.length() == 0)
		{
			return null;
		}
		Long number = null;
		try
		{
			number = Long.parseLong(matcher.group(group));
		} catch (NumberFormatException ex)
		{
			throwValueException(object);
			// invalid location
		}
		return number;
	}

	protected void throwValueException(Object obj) throws ValidationException
	{
		if (obj instanceof Qualifier) {
			throw new ValidationException(ValidationMessage.error("Qualifier", ((Qualifier)obj).getName(), ((Qualifier)obj).getValue()).append(((Qualifier)obj).getOrigin()));
		} else if (obj instanceof Feature) {
			throw new ValidationException(ValidationMessage.error("Feature", ((Feature)obj).getName(), ((Feature)obj).getOrigin()));
		}
	}
}
