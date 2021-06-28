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
package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.GlobalDataSetFile;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

import java.util.ArrayList;
import java.util.List;

@Description("The feature name \\\"{0}\\\" must have at least one qualifier.\\\\")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class FeatureQualifiersRequiredCheck extends FeatureValidationCheck {

	private List<String> featuresList = new ArrayList<>();

	private static final String QUALIFIERS_REQUIRED_ID_1 = "FeatureQualifiersRequiredCheck";

	public FeatureQualifiersRequiredCheck() {
	}

	private void init() {
		DataSet keySet = GlobalDataSets.getDataSet(GlobalDataSetFile.FEATURE_REQUIRE_QUALIFIERS);
		if (keySet != null) {
			for (DataRow dataRow : keySet.getRows()) {
				String key = dataRow.getString(0);
				featuresList.add(key);
			}
		}
	}

	public ValidationResult check(Feature feature) {
		init();
		result = new ValidationResult();

		if (feature == null) {
			return result;
		}

		if (featuresList.contains(feature.getName()) && feature.getQualifiers().isEmpty()) {
            reportError(feature.getOrigin(), QUALIFIERS_REQUIRED_ID_1, feature.getName());
		}

		return result;
	}
}
