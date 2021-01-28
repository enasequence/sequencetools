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

import java.util.List;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Invalid EC-number format: {0}")
public class EC_numberFormatCheck extends FeatureValidationCheck
{
	private static final String EC_numberFormatCheck_Error_ID = "EC_numberFormatCheck";
	private static final Pattern PATTERN = Pattern.compile("^\\d+\\.(((\\d+|-)\\.(\\d+|-)\\.(\\d+|-))|((\\d+)\\.(\\d+)\\.(\\d+|n([0-9]+)?)))$");

	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();
		if(feature==null)
			return result;
		List<Qualifier> ecQualifiers=feature.getQualifiers(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		if(ecQualifiers.size()==0)
			return result;
		for(Qualifier ecNumber :ecQualifiers)
		{
			String value= ecNumber.getValue();
			if(!PATTERN.matcher(value).matches())
				reportError(feature.getOrigin(),EC_numberFormatCheck_Error_ID , value);
		}

		return result;
	}
}
