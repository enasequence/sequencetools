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
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataRow;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@CheckDataSet("feature-moltype.tsv")
@Description("Molecule type must have value {0} when feature {1} exists")
public class FeatureAndMoleculeTypeCheck extends EntryValidationCheck {

	@CheckDataRow
	private DataRow dataRow;

	private final static String MESSAGE_ID = "FeatureAndMoleculeTypeCheck-1";

	public FeatureAndMoleculeTypeCheck() {
	}

	FeatureAndMoleculeTypeCheck(DataRow dataRow) {
		this.dataRow = dataRow;
	}

	public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null) {
			return result;
		}
        String expectedMoleculeType = this.dataRow.getString(0);
		String featureName = this.dataRow.getString(1);
		if (featureName == null || expectedMoleculeType == null) {
			return result;
		}

		if (!SequenceEntryUtils.isFeatureAvailable(featureName, entry)) {
			return result;
		}

		String moleculeType = SequenceEntryUtils.getMoleculeType(entry);
		if (!expectedMoleculeType.equals(moleculeType)) {
			reportError(entry.getOrigin(), MESSAGE_ID, expectedMoleculeType, featureName);
		}
		return result;
	}

}
