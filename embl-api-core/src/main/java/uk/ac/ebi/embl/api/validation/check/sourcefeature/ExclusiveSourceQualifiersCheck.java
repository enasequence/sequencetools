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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.Collection;

@Description("Qualifiers {0} and {1} cannot exist together within the same entry")
public class ExclusiveSourceQualifiersCheck extends EntryValidationCheck {

	private final static String MESSAGE_ID = "ExclusiveSourceQualifiersCheck-1";

	public ExclusiveSourceQualifiersCheck() {
	}

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();
		if (entry == null) {
			return result;
		}

		for(DataRow dataRow : GlobalDataSets.getDataSet(FileName.SOURCE_EXCLUSIVE_QUALIFIERS).getRows()) {

			String qualifierName1 = dataRow.getString(0);
			String qualifierName2 = dataRow.getString(1);

			Collection<Feature> sources = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
			if (sources == null || sources.isEmpty()) {
				continue;
			}

			boolean isQualifier1 = false, isQualifier2 = false;

			for (Feature source : sources) {
				isQualifier1 |= SequenceEntryUtils.isQualifierAvailable(qualifierName1, source);
				isQualifier2 |= SequenceEntryUtils.isQualifierAvailable(qualifierName2, source);
				if (isQualifier1 && isQualifier2) {
					reportError(entry.getOrigin(), MESSAGE_ID, qualifierName1, qualifierName2);
					break;
				}
			}
		}
		return result;
	}

}
