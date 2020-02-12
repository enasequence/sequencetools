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
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;

@Description("\"CIRCULAR\" topology  is not allowed for assembly contigs")
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
@ExcludeScope(validationScope={ValidationScope.NCBI})
public class AssemblyTopologyCheck extends EntryValidationCheck {

	private final static String MESSAGE_ID = "assemblyTopologyCheck_1";
	
	public ValidationResult check(Entry entry) throws ValidationEngineException {
		result = new ValidationResult();

    if (entry == null || entry.getSequence() == null) {
      return result;
    }

    if ((ValidationScope.ASSEMBLY_CONTIG
                == getEmblEntryValidationPlanProperty().validationScope.get()
            || ValidationScope.ASSEMBLY_SCAFFOLD
                == getEmblEntryValidationPlanProperty().validationScope.get())
        && Topology.CIRCULAR == entry.getSequence().getTopology()) {
      reportError(entry.getOrigin(), MESSAGE_ID);
    }

		return result;
	}

}
