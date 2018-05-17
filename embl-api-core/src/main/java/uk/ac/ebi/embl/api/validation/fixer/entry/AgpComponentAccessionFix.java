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

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.sql.SQLException;

@Description("")
public class AgpComponentAccessionFix extends EntryValidationCheck
{
	
	private final static String ACCESSION_FIX_ID = "AgpComponentAccessionFix-1";

	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null||getEntryDAOUtils()==null||(entry.getSequence()!=null&&entry.getSequence().getAgpRows().size()==0)||!FileType.AGP.equals(getEmblEntryValidationPlanProperty().fileType.get()))
		{
			return result;
		}

		for(AgpRow agpRow:entry.getSequence().getAgpRows())
		{
		  if(agpRow.isGap())
			  continue;
		  String componentID=agpRow.getComponent_id();
		  String analysis_id=getEmblEntryValidationPlanProperty().analysis_id.get();
		  int assembly_level=getEmblEntryValidationPlanProperty().validationScope.get().getAssemblyLevel();
		  if(analysis_id==null||assembly_level==-1)
			  return result;
		  ContigSequenceInfo sequenceInfo=null;
		  try
		  {
		     sequenceInfo=getEntryDAOUtils().getSequenceInfoBasedOnEntryName(componentID, analysis_id, assembly_level);
		  }catch(SQLException e)
		  {
			  e.printStackTrace();
			  throw new ValidationEngineException(e);
		  }
		  if(sequenceInfo==null)
			 continue;
		  if(sequenceInfo.getPrimaryAccession()!=null&& sequenceInfo.getSequenceVersion()!=0)
		  {
			  agpRow.setComponent_acc(sequenceInfo.getPrimaryAccession()+"."+sequenceInfo.getSequenceVersion());
    		  reportMessage(Severity.FIX, entry.getOrigin(), ACCESSION_FIX_ID, agpRow.getComponent_acc(),componentID,agpRow.getObject());
		  }
		  		
		}

		return result;
	}

}
