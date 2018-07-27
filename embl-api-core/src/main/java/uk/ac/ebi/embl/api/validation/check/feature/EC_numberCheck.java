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
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Invalid EC number : \"{0}\" "
		+ " Non existent EC number : \"{0}\"  ")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class EC_numberCheck extends FeatureValidationCheck
{
	private static final String EC_numberCheck_Warning_ID = "EC_numberCheck_1";
	private static final String EC_numberCheck_Error_ID = "EC_numberCheck_2";

		
	public EC_numberCheck()
	{
	}
	public ValidationResult check(Feature feature) throws ValidationEngineException
	{
		result = new ValidationResult();
		
		if (feature == null||getEntryDAOUtils()==null)
		{
			return result;
		}
		try
		{
		 if(!SequenceEntryUtils.isQualifierAvailable(Qualifier.EC_NUMBER_QUALIFIER_NAME, feature))
			 return result; 
				
		String ecNumberValue = SequenceEntryUtils.getQualifierValue(Qualifier.EC_NUMBER_QUALIFIER_NAME, feature);
		String result=getEntryDAOUtils().isEcnumberValid(ecNumberValue);
		
		if(result==null)
			reportError(feature.getOrigin(),EC_numberCheck_Error_ID , ecNumberValue);
		if("N".equals(result))
		    reportWarning(feature.getOrigin(), EC_numberCheck_Warning_ID,ecNumberValue);
		
		} catch(Exception e)
		{
			throw new ValidationEngineException(e);
		}
		
		return result;
	}
}
