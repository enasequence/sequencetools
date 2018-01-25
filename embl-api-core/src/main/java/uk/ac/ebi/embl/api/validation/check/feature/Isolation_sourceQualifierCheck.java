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
package uk.ac.ebi.embl.api.validation.check.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@CheckDataSet( dataSetNames = {FileName.FEATURE_REGEX_GROUPS, FileName.FEATURE_QUALIFIER_VALUES} )
@Description("isolation_source is a country \"{0}\",change isolation_source qualifier to country qualifier " + "isolation_source is a lat_lon \"{0}\", change isolation_source qualifier to lat_lon qualifier ")
public class Isolation_sourceQualifierCheck extends FeatureValidationCheck
{
	private final static String ISOLATION_SOURCE_COUNTRY_CHECK_ID = "Isolation_sourceQualifierCheck_1";
	private final static String ISOLATION_SOURCE_LAT_LON_CHECK_ID = "Isolation_sourceQualifierCheck_2";

	public Isolation_sourceQualifierCheck()
	{
	}

	public ValidationResult check(Feature feature)
	{
		DataSet qualifierValueSet = GlobalDataSets.getDataSet(FileName.FEATURE_QUALIFIER_VALUES);
		DataSet qualifierRegexSet = GlobalDataSets.getDataSet(FileName.FEATURE_REGEX_GROUPS);
		result = new ValidationResult();

		if (feature == null)
		{
			return result;
		}
		if (feature.getQualifiers().size() == 0)
		{
			return result;
		}

		List<Qualifier> isolation_sourceQualifiers = feature.getQualifiers(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME);

		if (isolation_sourceQualifiers.size() == 0)
		{
			return result;
		}

		List<String> countryList = new ArrayList<String>();
		String latLonRegex = null;
		String countryRegex = null;

		// country values
		for (DataRow dataRow : qualifierValueSet.getRows())
		{
			if (dataRow.getString(0).equals(Qualifier.COUNTRY_QUALIFIER_NAME))
			{
				countryList = Arrays.asList(dataRow.getStringArray(3));
			}
		}

		// lat_lon regex
		for (DataRow dataRow : qualifierRegexSet.getRows())
		{
			if (dataRow.getString(0).equals(Qualifier.LAT_LON_QUALIFIER_NAME))
			{
				latLonRegex = dataRow.getString(4);
			} else if (dataRow.getString(0).equals(Qualifier.COUNTRY_QUALIFIER_NAME))
			{
				countryRegex = dataRow.getString(4);
			}
		}

		Pattern lat_lonPattern = null;
		Pattern countryPattern = null;
		if (latLonRegex != null)
		{
			lat_lonPattern = Pattern.compile(latLonRegex);
		}

		if (countryRegex != null)
		{
			countryPattern = Pattern.compile(countryRegex);
		}
		for (Qualifier isolation_sourceQualifier : isolation_sourceQualifiers)
		{
			String isolationQualifierValue = isolation_sourceQualifier.getValue();
			if (isolationQualifierValue != null)
			{
				Matcher matcher;

				if ((matcher = countryPattern.matcher(isolationQualifierValue)).matches() && countryList.contains(matcher.group(1)))
				{
					reportError(feature.getOrigin(), ISOLATION_SOURCE_COUNTRY_CHECK_ID, isolationQualifierValue);
				} else if (lat_lonPattern != null)
				{
					if (lat_lonPattern.matcher(isolationQualifierValue).matches())
					{
						reportError(feature.getOrigin(), ISOLATION_SOURCE_LAT_LON_CHECK_ID, isolationQualifierValue);
					}
				}
			}
		}

		return result;
	}
}
