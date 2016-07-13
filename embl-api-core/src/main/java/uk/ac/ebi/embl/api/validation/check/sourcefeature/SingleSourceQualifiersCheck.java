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

import java.util.Collection;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataRow;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@CheckDataSet("single-source-qualifiers.tsv")
@Description("Qualifier {0} cannot exist in more than one source feature.")
public class SingleSourceQualifiersCheck extends EntryValidationCheck {

	@CheckDataRow
	private DataRow dataRow;

	private final static String MESSAGE_ID = "SingleSourceQualifiersCheck";

	public SingleSourceQualifiersCheck() {
	}

	SingleSourceQualifiersCheck(DataRow dataSet) {
		this.dataRow = dataSet;
	}

	public ValidationResult check(Entry entry) {
        result = new ValidationResult();
        
        if (entry == null) {
			return result;
		}

		String qualifierName = dataRow.getString(0);

		Collection<Feature> sources = SequenceEntryUtils.getFeatures(
				Feature.SOURCE_FEATURE_NAME, entry);
		boolean alreadyAvailable = false;
		for (Feature source : sources) {
			Collection<Qualifier> qualifiers = source
					.getQualifiers(qualifierName);
			if (qualifiers.size() > 1) {
				reportError(entry.getOrigin(), MESSAGE_ID, qualifierName);
			}
			if (alreadyAvailable && !qualifiers.isEmpty()) {
				reportError(entry.getOrigin(), MESSAGE_ID, qualifierName);
			}
			alreadyAvailable = !qualifiers.isEmpty();
		}
		return result;
	}

}
