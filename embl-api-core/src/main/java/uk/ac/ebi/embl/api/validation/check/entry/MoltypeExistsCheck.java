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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")


public class MoltypeExistsCheck extends EntryValidationCheck {

	private final static String MOLTYPE_MISSING_MESSAGE_ID = "MoltypeExistsCheck";
		
	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		
		if(entry.getSequence()==null&&entry.getPrimarySourceFeature()==null)
			return result;
		
		String molType= null;
		
		if(entry.getSequence()!=null)
		{
			molType=entry.getSequence().getMoleculeType();
		}
		
		if(entry.getPrimarySourceFeature()!=null)
		{
			Qualifier molTypeQualifier=entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.MOL_TYPE_QUALIFIER_NAME);
			if(molTypeQualifier!=null)
			molType=molTypeQualifier.getValue();
     	}

		if (molType == null)
		{
			reportError(entry.getOrigin(), MOLTYPE_MISSING_MESSAGE_ID,entry.getPrimaryAccession()==null?entry.getSubmitterAccession():entry.getPrimaryAccession());
		} 

		return result;
	}

}
