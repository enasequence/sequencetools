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
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

import java.nio.ByteBuffer;

@Description("Entry {0} with ObjectName{1} has already been stored in database and using stored sequence for validatation")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})
public class AnnotationOnlySequenceFix extends EntryValidationCheck
{
	private final static String SEQUENCE_FIX_ID = "AnnotationOnlySequenceFix";

	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		try
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
		 if(entry.getSequence()!=null&&entry.getSequence().getSequenceByte()!=null&&entry.getSequence().getLength()!=0)
			{
			   	return result;
			}
		   
		   if(entry.getSequence()!=null&&((entry.getSequence().getContigs()!=null&&entry.getSequence().getContigs().size()!=0)||entry.getSequence().getAgpRows().size()!=0))//CO line exists
		   {
			   return result;
		   }
		   byte[] sequence =null;
		   String primaryAcc=entry.getPrimaryAccession();
		   
		if(primaryAcc!=null)
		{
			sequence=entryDAOUtils.getSequence(entry.getPrimaryAccession());
		}
		else  //check whether it is an assembly
		{
		
		if(getEmblEntryValidationPlanProperty().analysis_id.get()==null||entry.getSecondaryAccessions()==null)
		{
			return result;
		}
		
	   Integer assemblyLevel = getEmblEntryValidationPlanProperty().validationScope.get().getAssemblyLevel();
	   
       if(assemblyLevel==-1)
    	   return result;
	   
		
			  if(!entryDAOUtils.isAssemblyLevelExists(getEmblEntryValidationPlanProperty().analysis_id.get(), assemblyLevel))
			  {
				  return result;
			  }
			 
			 primaryAcc = entryDAOUtils.getPrimaryAcc(getEmblEntryValidationPlanProperty().analysis_id.get(), entry.getSubmitterAccession(), assemblyLevel);
			if(primaryAcc==null)
				return result;
			   
			sequence = entryDAOUtils.getSequence(primaryAcc);
		}
		
		if (sequence != null)
			{
				if(entry.getSequence()==null)
				{
					entry.setSequence(new SequenceFactory().createSequence());
				}
				entry.getSequence().setSequence(ByteBuffer.wrap(sequence));
				reportMessage(Severity.FIX, entry.getOrigin(), SEQUENCE_FIX_ID,primaryAcc,entry.getSubmitterAccession());
			}
		
		
		}catch(Exception e)
		{
			throw new ValidationEngineException(e.getMessage());
		}
		return result;
	}
}
