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
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Molecule type for {0} entries must be {1}")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class MoleculeTypeAndDataclassCheck extends EntryValidationCheck {

	/**
	 * whether this is a 'new' entry in the database - default to true until we
	 * have a system for checking this
	 * 
	 */

	private final static String MESSAGE_ID = "MoleculeTypeAndDataclassCheck-1";
	private final static String GSS_MESSAGE_ID = "MoleculeTypeAndDataclassCheck-2";

	public MoleculeTypeAndDataclassCheck() {
	}

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();
		if (entry == null) {
			return result;
		}
		String moleculeType = SequenceEntryUtils.getMoleculeType(entry);
		String dataclass = entry.getDataClass();

		if (dataclass == null || moleculeType == null) {
			return result;
		}
		if (dataclass.equals(Entry.EST_DATACLASS)
				&& !moleculeType.contains("RNA")) {
			reportError(entry.getOrigin(), MESSAGE_ID, dataclass, "RNA");
		}
		if (dataclass.equals(Entry.GSS_DATACLASS)
				&& !moleculeType.contains("genomic")) {
			reportWarning(entry.getOrigin(), GSS_MESSAGE_ID, dataclass,
					"genomic RNA/DNA");
		}
		if (dataclass.equals(Entry.HTG_DATACLASS)
				&& !moleculeType.contains("DNA")) {
			reportError(entry.getOrigin(), MESSAGE_ID, dataclass, "DNA");
		}

		return result;
	}

}
