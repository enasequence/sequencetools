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

import java.util.Collection;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataRow;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@CheckDataSet("feature-source-qualifier.tsv")
@Description("Source qualifier {0} is required when feature {1} exists.")
public class FeatureAndSourceQualifierCheck extends EntryValidationCheck {

	@CheckDataRow
	private DataRow dataRow;
	
	private final static String MESSAGE_ID = "FeatureAndSourceQualifierCheck-1";
	
	public FeatureAndSourceQualifierCheck() {
	}
	
	FeatureAndSourceQualifierCheck(DataRow dataRow) {
		this.dataRow = dataRow;
	}
	
	public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null) {
			return result;
		}
        String expectedQualifierName = dataRow.getString(0);
		String featureName = dataRow.getString(1);
		if (featureName == null || expectedQualifierName == null) {
			return result;
		}
		
		if (!SequenceEntryUtils.isFeatureAvailable(featureName, entry)) {
			return result;
		}
		
		Collection<Feature> sources = SequenceEntryUtils.getFeatures(
				Feature.SOURCE_FEATURE_NAME, entry);
		if (sources == null || sources.isEmpty()) {
			reportError(entry.getOrigin(), MESSAGE_ID, featureName, expectedQualifierName);
			return result;
		}
		boolean isQualifierAvailable = false;
		for (Feature source : sources) {
			isQualifierAvailable = SequenceEntryUtils.isQualifierAvailable(expectedQualifierName, source);
			if (isQualifierAvailable) {
				break;
			}
		}
		if (!isQualifierAvailable) {
			reportError(entry.getOrigin(), MESSAGE_ID, expectedQualifierName, featureName);
		}
		
		return result;
	}

}
