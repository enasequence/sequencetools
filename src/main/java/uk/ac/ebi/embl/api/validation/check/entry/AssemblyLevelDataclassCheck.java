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
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Invalid ID Line dataclass {0}"
		+ "{0} dataclass allowed only for Master entries")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI , ValidationScope.NCBI_MASTER})
public class AssemblyLevelDataclassCheck extends EntryValidationCheck {

	private final static String MESSAGE_ID = "assemblyLevelDataclassCheck_1";
	private final static String MESSAGE_ID_CHROMOSOME ="assemblyLevelDataclassCheck_2";
	
	public ValidationResult check(Entry entry) throws ValidationEngineException {
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

	    int assemblyLevel = ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 0 : ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 1 : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 2 :-1;

		if (assemblyLevel == -1 && ValidationScope.ASSEMBLY_MASTER.equals(getEmblEntryValidationPlanProperty().validationScope.get()))
		{
			if (entry.getDataClass() == null || !Entry.SET_DATACLASS.equals(entry.getDataClass()))
			{
			reportError(entry.getOrigin(), MESSAGE_ID, Entry.SET_DATACLASS,"master entries");
			}
		
		} else if (assemblyLevel == 0)
		{
			if (entry.getDataClass() == null || !Entry.WGS_DATACLASS.equals(entry.getDataClass()))
			{
				reportError(entry.getOrigin(), MESSAGE_ID, Entry.WGS_DATACLASS,"contig entries");
			}
		} else if (assemblyLevel == 2)
		{
			if(entry.getDataClass()!=null&& (Entry.WGS_DATACLASS.equals(entry.getDataClass())||Entry.SET_DATACLASS.equals(entry.getDataClass())))

			{
				reportError(entry.getOrigin(), MESSAGE_ID_CHROMOSOME);
			}
		}
		return result;
	}

}
