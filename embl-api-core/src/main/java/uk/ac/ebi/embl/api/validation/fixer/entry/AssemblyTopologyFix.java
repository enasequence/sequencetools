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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("contig/scaffold sequence topology set to \"linear\"")
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
public class AssemblyTopologyFix extends EntryValidationCheck
{
	private final String MESSAGE_ID = "AssemblyTopologyFix_1";


	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null||entry.getSequence()==null)
		{
			return result;
		}
		
        if((ValidationScope.ASSEMBLY_CONTIG==getEmblEntryValidationPlanProperty().validationScope.get()||ValidationScope.ASSEMBLY_SCAFFOLD==getEmblEntryValidationPlanProperty().validationScope.get())&&Topology.LINEAR!=entry.getSequence().getTopology())
        {
        	entry.getSequence().setTopology(Topology.LINEAR);
	   	    reportMessage(Severity.FIX, entry.getOrigin(), MESSAGE_ID);
        }
		
		return result;
	}

}
