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

@Description("contig/scaffold/chromosome keywords are not allowed in master entry description")
@GroupIncludeScope(group={ValidationScope.Group.ASSEMBLY})
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_CONTIG,ValidationScope.ASSEMBLY_SCAFFOLD,ValidationScope.ASSEMBLY_CHROMOSOME})
public class MasterEntryDescriptionCheck extends EntryValidationCheck {

	private final static String MASTER_ENTRY_DESCRIPTION_MESSAGE_ID = "MasterEntryDescriptionCheck_1";
	
	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		
		if (entry.getDescription() != null)
		{
			String description = entry.getDescription().getText();
			String descUppercase=null;
			if(description!=null)
			{
			descUppercase = description.toUpperCase();
			if (descUppercase.contains("SCAFFOLD")|| descUppercase.contains("CONTIG")|| descUppercase.contains("CHROMOSOME")) 
			{
				reportError(entry.getOrigin(),MASTER_ENTRY_DESCRIPTION_MESSAGE_ID);
			}
			}
		}

		return result;
	}

}
