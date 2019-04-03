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
import java.util.List;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;

@Description("")
@RemoteExclude
public class AssemblyInfoSamplewithDifferentProjectCheck extends GenomeAssemblyValidationCheck<AssemblyInfoEntry>
{
   
   private final String MESSAGE_KEY_DIFFERENT_PROJECT_ASEMBLY_ERROR = "AssemblyInfoDifferentProject";
	
	
    public AssemblyInfoSamplewithDifferentProjectCheck()
	{

	}
   
    @Override
	public ValidationResult check(AssemblyInfoEntry entry) throws ValidationEngineException
	{
		if(entry==null)
			return result;
		if(getEraproDAOUtils()==null)
			return result;
		try
		{
		ValidationResult result = new ValidationResult();
		List<String> analysisIdList=getEraproDAOUtils().isSampleHasDifferentProjects(entry.getAnalysisId());
		if(analysisIdList.size()>0)
		 reportError(entry.getOrigin(), MESSAGE_KEY_DIFFERENT_PROJECT_ASEMBLY_ERROR, String.join(",",analysisIdList));
		return result;
		}catch(SQLException e)
		{
			throw new ValidationEngineException(e.getMessage(), e);
		}
	}

	
}
