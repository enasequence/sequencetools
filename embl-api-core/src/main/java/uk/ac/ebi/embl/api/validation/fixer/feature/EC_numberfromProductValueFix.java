/*
 * # Copyright 2012-2012 EMBL-EBI, Hinxton outstation
 *
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 *
# http://www.apache.org/licenses/LICENSE-2.0
 *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.fixer.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("Added \"EC_number\" qualifier from \"Product\" Value {0}" + "\"product\" qualifier value {0} has been changed to {1}")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class EC_numberfromProductValueFix extends FeatureValidationCheck
{
	
	private static final String EC_numberfromProductValueFix_ID_1 = "EC_numberfromProductValueFix_1";
	
	private static final String EC_numberfromProductValueFix_ID_2 = "EC_numberfromProductValueFix_2";
	private static final String EC_numberfromProductValueFix_ID_3 = "EC_numberfromProductValueFix_3";
	private static final String EC_numberfromProductValueFix_ID_4 = "EC_numberfromProductValueFix_4";
	
	private final static String EC_NUMBER_PATTERN = "((\\()?(\\[)?(\\s*)?(EC||ec)?(\\s*)?([=:])?(\\s*)?([0-9\\-]+)\\.([0-9\\-]+)(\\.([0-9\\-]+))(\\.([0-9\\-]+))(\\s*)?(\\))?(\\])?)";
	private final static Pattern PATTERN = Pattern.compile(EC_NUMBER_PATTERN, Pattern.CASE_INSENSITIVE);
	
	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();
		
		if (feature == null)
		{
			return result;
		}
		List<Qualifier> productQualifiers = feature.getQualifiers(Qualifier.PRODUCT_QUALIFIER_NAME);
		for (Qualifier productQualifier : productQualifiers)
		{
			String productValue = productQualifier.getValue();
			String non_EC_Value = productValue.replaceAll(EC_NUMBER_PATTERN, "");
			if (non_EC_Value!=null&&non_EC_Value.equals(productValue))
			{
				if (productValue.toLowerCase().contains("hypothetical protein") || productValue.toLowerCase().contains("unknown"))
				{
					ArrayList<Qualifier> ec_numberQualifiers = (ArrayList<Qualifier>) feature.getQualifiers(Qualifier.EC_NUMBER_QUALIFIER_NAME);
					for (Qualifier ec_qualifier : ec_numberQualifiers)
					{
						feature.removeQualifier(ec_qualifier);
						reportMessage(Severity.FIX, feature.getOrigin(), EC_numberfromProductValueFix_ID_3, feature.getName());
					}
				}
				continue;
			}
			String[] productValues = productValue.split(EC_NUMBER_PATTERN);
			non_EC_Value.replaceAll("  ", " ");
			String ec_numberValue = null;
			String tempValue = productValue;
			if (productValues.length == 0)
			{
				return result;
			}
			if (productValues.length == 1)
			{
				ec_numberValue = StringUtils.remove(tempValue, productValues[0]);
				
			}
			else
			{
				for (String productvalue : productValues)
				{
					ec_numberValue = StringUtils.remove(tempValue, productvalue);
					tempValue = ec_numberValue;
				}
			}
			if (ec_numberValue != null)
			{
				Matcher matcher = PATTERN.matcher(ec_numberValue);
				if (matcher.find())
				{
					ec_numberValue = (matcher.group(9) == null ? "-" : matcher.group(9)) + "."
							+ (matcher.group(10) == null ? "-" : matcher.group(10)) + "." + (matcher.group(12) == null ? "-" : matcher.group(12))
							+ "." + (matcher.group(14) == null ? "-" : matcher.group(14));
					
				}
				
				productQualifier.setValue(StringUtils.trim((non_EC_Value)));
				reportMessage(Severity.FIX, feature.getOrigin(), EC_numberfromProductValueFix_ID_1, productValue, productQualifier.getValue());
				
				if (!non_EC_Value.toLowerCase().contains("hypothetical protein") && !non_EC_Value.toLowerCase().contains("unknown"))
				{
					feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, ec_numberValue);
					if (!SequenceEntryUtils.deleteDuplicatedQualfiier(feature, Qualifier.EC_NUMBER_QUALIFIER_NAME))
						reportMessage(Severity.FIX, feature.getOrigin(), EC_numberfromProductValueFix_ID_2, productValue);
				}
				else
				{
					reportMessage(Severity.FIX, feature.getOrigin(), EC_numberfromProductValueFix_ID_4, productValue, productQualifier.getValue());
       				ArrayList<Qualifier> ec_numberQualifiers = (ArrayList<Qualifier>) feature.getQualifiers(Qualifier.EC_NUMBER_QUALIFIER_NAME);
					for (Qualifier ec_qualifier : ec_numberQualifiers)
					{
						feature.removeQualifier(ec_qualifier);
						reportMessage(Severity.FIX, feature.getOrigin(), EC_numberfromProductValueFix_ID_3, feature.getName());
					}
				}
				
			}
			
		}
		return result;
	}
	
		
}
