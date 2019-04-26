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

@Description("Calls through to SequencesBasesCheck in the sequence package, but adds an entry Origin")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_CONTIG,ValidationScope.ASSEMBLY_SCAFFOLD,ValidationScope.ASSEMBLY_CHROMOSOME,ValidationScope.ASSEMBLY_TRANSCRIPTOME})
public class SequenceBasesCheck extends EntryValidationCheck {

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();
        
        uk.ac.ebi.embl.api.validation.check.sequence.SequenceBasesCheck check =
			new uk.ac.ebi.embl.api.validation.check.sequence.SequenceBasesCheck();
		check.setOrigin(entry.getOrigin());
		return check.check(entry.getSequence());
	}
}
