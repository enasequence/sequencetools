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
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;

@Description("Assembly entries must have an entry_name")
@GroupIncludeScope(group={ValidationScope.Group.ASSEMBLY})
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI})
public class EntryNameExistsCheck extends EntryValidationCheck {

	private final static String ENTRY_NAME_MISSING_MESSAGE_ID = "EntryNameExistsCheck_1";
	private final static String ENTRY_NAME_LENGTH_MESSAGE_ID = "EntryNameExistsCheck_2";
	private final static String MASTER_ENTRY_NAME_EXISTS_MESSAGE_ID = "EntryNameExistsCheck_3";


	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		
		if(Entry.SET_DATACLASS.equals(entry.getDataClass()))
		{
			if((entry.getSubmitterAccession()!=null))
			reportError(entry.getOrigin(),MASTER_ENTRY_NAME_EXISTS_MESSAGE_ID);
		}
		else if (entry.getSubmitterAccession()==null||entry.getSubmitterAccession().isEmpty())
		{
			reportError(entry.getOrigin(), ENTRY_NAME_MISSING_MESSAGE_ID);
		} 
		else if(entry.getSubmitterAccession().length()>100)
		{
			reportError(entry.getOrigin(),ENTRY_NAME_LENGTH_MESSAGE_ID );
		}

		return result;
	}

}
