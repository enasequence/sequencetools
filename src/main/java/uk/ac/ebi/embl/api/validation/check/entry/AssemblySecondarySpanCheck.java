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

import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.EntryValidations;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Assembly begin position and assembly end position do not exceed the sequence length")
public class AssemblySecondarySpanCheck extends EntryValidationCheck {

	private final static String MESSAGE_ID = "AssembleSecondarySpanCheck-1";
	
	public ValidationResult check(Entry entry) {
		result = new ValidationResult();
		if (entry == null) {
			return result;
		}
		if (entry.getSequence() == null) {
			return result;
		}
		if (entry.getAssemblies() == null) {
			return result;
		}

		for (Assembly assembly : entry.getAssemblies()) {
			Long beginPosition = assembly.getSecondarySpan().getBeginPosition();
			Long endPosition = assembly.getSecondarySpan().getEndPosition();
			if (beginPosition == null) {
				return result;
			}
			if (endPosition == null) {
				return result;
			}
			if (beginPosition < 0
					|| beginPosition > entry.getSequence().getLength()) {
				result.append(EntryValidations.createMessage(entry.getOrigin(),
						Severity.ERROR, MESSAGE_ID));
			}
			if (endPosition < 0
					|| endPosition > entry.getSequence().getLength()) {
				result.append(EntryValidations.createMessage(entry.getOrigin(),
						Severity.ERROR, MESSAGE_ID));
			}
		}
		return result;
	}

}
