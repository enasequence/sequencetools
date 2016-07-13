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

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EC_numberandProductValueCheck extends FeatureValidationCheck
{
	private final static String EC_NUMBER_PATTERN = "((\\()?(\\[)?(\\s*)?(EC||ec)?(\\s*)?([=:])?(\\s*)?([0-9\\-]+)\\.([0-9\\-]+)(\\.([0-9\\-]+))(\\.([0-9\\-]+))(\\s*)?(\\))?(\\])?)";
	private static final String EC_numberandProductValueCheck_ID = "EC_numberandProductValueCheck";
	private Pattern pattern = Pattern.compile(EC_NUMBER_PATTERN);
		
	public EC_numberandProductValueCheck()
	{
	}
	
	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();
		
		if (feature == null)
		{
			return result;
		}
		
		String productValue = SequenceEntryUtils.getQualifierValue(Qualifier.PRODUCT_QUALIFIER_NAME, feature);
		String ec_numberValue = SequenceEntryUtils.getQualifierValue(Qualifier.EC_NUMBER_QUALIFIER_NAME, feature);
		if (productValue == null)
		{
			return result;
		}
		Matcher matcher = pattern.matcher(productValue);
		if ((productValue.toLowerCase().contains("hypothetical protein") || productValue.toLowerCase().contains("unknown")))
		{
			if (ec_numberValue != null || matcher.find())
				reportError(feature.getOrigin(), EC_numberandProductValueCheck_ID, productValue);
		}
		
		return result;
	}
}
