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

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;

@Description("Entry_names cannot be updated.Entry_name \"{0}\" is not matching with the registered \"{1}\" entry_names of the assembly \"{2}\"")
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI})
public class AssemblyLevelEntryNameCheck extends EntryValidationCheck {

	private final static String MESSAGE_ID = "AssemblyLevelEntryNameCheck_1";
	
	public ValidationResult check(Entry entry) throws ValidationEngineException {
		result = new ValidationResult();

		if (entry == null||entry.getSubmitterAccession()==null||entry.getSubmitterAccession().isEmpty())
		{
			return result;
		}
		
		if(getEmblEntryValidationPlanProperty()==null||getEmblEntryValidationPlanProperty().analysis_id.get()==null||getEntryDAOUtils()==null)
			return result;

	    Integer assemblyLevel = ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 0 : ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 1 : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 2 :-1;
	    
	    String entryName=ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? "contig":  ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? "scaffold" : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? "chromosome" : null;
	
	   if(assemblyLevel==-1)
		   return result;
	   String analysisId=getEmblEntryValidationPlanProperty().analysis_id.get();
	   try
	   {
	     if(getEntryDAOUtils().isAssemblyLevelExists(analysisId, assemblyLevel))
	    {
	      if(!getEntryDAOUtils().isAssemblyLevelObjectNameExists(analysisId,entry.getSubmitterAccession(), assemblyLevel))   
		   reportError(entry.getOrigin(), MESSAGE_ID,entry.getSubmitterAccession(),entryName,analysisId);
	    }
	   }catch(SQLException e)
	   {
		   throw new ValidationEngineException(e.getMessage());
	   }
		
		return result;
	}

}
