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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.genomeassembly.UnlocalisedEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;

@Description("")
@RemoteExclude
public class UnlocalisedListObjectNameValidationCheck extends GenomeAssemblyValidationCheck<UnlocalisedEntry>
{
	public static final String MESSAGE_KEY_UNLOCALISED_VALID_ERROR = "UnlocalisedListUnlocalisedValidCheck";		

	public ValidationResult check(UnlocalisedEntry entry) throws ValidationEngineException
	{

		if (entry == null)
		return result;
		
		if(getEntryDAOUtils()==null)
		return result;
	   try
		{
		String accession = getEntryDAOUtils().associate_unlocalised_list_acc(entry.getObjectName(),2/* chromosome */,entry.getAnalysisId());
		
		if(null == accession) 
			reportError(entry.getOrigin(), MESSAGE_KEY_UNLOCALISED_VALID_ERROR, entry.getObjectName()); 
		else 
			entry.setAcc(accession);

		}catch(SQLException e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
		return result;
	}	
	
}
