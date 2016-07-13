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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("Dataclass has been changed from \"{0}\" to \"{1}\"")
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
public class AssemblyLevelDataClassFix extends EntryValidationCheck
{
	private final String DATACLASSFIX_ID = "assemblyLevelDataClassFix_1";


	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

	   Integer assemblyLevel = ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 0 : ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 1 : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 2:-1 ;

		if (assemblyLevel == -1 && ValidationScope.ASSEMBLY_MASTER.equals(getEmblEntryValidationPlanProperty().validationScope.get()))
		{
			if (entry.getDataClass() == null || !Entry.SET_DATACLASS.equals(entry.getDataClass()))
			{
				reportMessage(Severity.FIX, entry.getOrigin(), DATACLASSFIX_ID,entry.getDataClass(), Entry.SET_DATACLASS);
				entry.setDataClass(Entry.SET_DATACLASS);
			}
		} else if (assemblyLevel == 0)
		{
			if (entry.getDataClass() == null || !Entry.WGS_DATACLASS.equals(entry.getDataClass()))
			{
				reportMessage(Severity.FIX, entry.getOrigin(), DATACLASSFIX_ID, entry.getDataClass(),Entry.WGS_DATACLASS);
				entry.setDataClass(Entry.WGS_DATACLASS);
			    entry.addKeyword(new Text(Entry.WGS_DATACLASS));
			}
		} else if (assemblyLevel == 1 || assemblyLevel == 2)
		{
				if(entry.getSubmitterAccession()==null||getEmblEntryValidationPlanProperty().analysis_id.get()==null)
				  return result;
				String dataClass = null;
				try
				{
					if(getEntryDAOUtils()!=null)
					dataClass = getEntryDAOUtils().getDataclass(getEmblEntryValidationPlanProperty().analysis_id.get(), entry.getSubmitterAccession(), assemblyLevel);
				} catch (SQLException e)
				{
					throw new ValidationEngineException(e);
				}
				if (dataClass != null&& !dataClass.equals(entry.getDataClass()))
				{
					reportMessage(Severity.FIX, entry.getOrigin(), DATACLASSFIX_ID,entry.getDataClass(), Entry.STD_DATACLASS);
					entry.setDataClass(dataClass);
				}
			}
		
		return result;
	}

}
