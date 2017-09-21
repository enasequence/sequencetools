/*******************************************************************************
 * Copyright 2012-2013 EMBL-EBI, Hinxton outstation
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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import java.nio.ByteBuffer;

@Description("{0} Sequence has already been loaded for the given assembly {1} with entry_name {2} and using the loaded sequence")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})
@GroupIncludeScope(group={ValidationScope.Group.ASSEMBLY})
public class AssemblyLevelSequenceFix extends EntryValidationCheck
{
	private final static String SEQUENCE_FIX_ID = "AssemblyLevelSequenceFix_1";

	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		
		EntryDAOUtils entryDAOUtils=getEntryDAOUtils();
		
		if(entryDAOUtils==null)
		{
			return result;
		}
		
		if(getEmblEntryValidationPlanProperty().analysis_id.get()==null||entry.getSecondaryAccessions()==null)
		{
			return result;
		}
		
		 if(entry.getSequence()!=null&&entry.getSequence().getSequenceByte()!=null&&entry.getSequence().getLength()!=0)
		{
		   	return result;
		}
	   
	   if(entry.getSequence()!=null&&entry.getSequence().getContigs()!=null&&entry.getSequence().getContigs().size()!=0||entry.getAgpRows().size()!=0)//CO line exists
	   {
		   return result;
	   }
	   
	   Integer assemblyLevel = getEmblEntryValidationPlanProperty().validationScope.get().getAssemblyLevel();
	   String sequence_type= assemblyLevel==0?"contig":assemblyLevel==1?"scaffold":assemblyLevel==2?"chromosome":null;

	   
       if(assemblyLevel==-1)
    	   return result;
	   
		try
		{
			  if(!entryDAOUtils.isAssemblyLevelExists(getEmblEntryValidationPlanProperty().analysis_id.get(), assemblyLevel))
			  {
				  return result;
			  }
			 
			byte[] sequence = entryDAOUtils.getSequence(entry.getSubmitterAccession(),getEmblEntryValidationPlanProperty().analysis_id.get(), assemblyLevel);
			if (sequence != null)
			{
				if(entry.getSequence()==null)
				{
					entry.setSequence(new SequenceFactory().createSequence());
				}
				entry.getSequence().setSequence(ByteBuffer.wrap(sequence));
				reportMessage(Severity.FIX, entry.getOrigin(), SEQUENCE_FIX_ID,sequence_type,getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession());
			}
		
		} catch (Exception e)
		{
			throw new ValidationEngineException(e);
		}

		return result;
	}
}
