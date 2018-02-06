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
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.ArrayList;
import java.util.List;

@Description("The feature name \\\"{0}\\\" must have at least one qualifier.\\\\")
public class FeatureQualifiersRequiredCheck extends FeatureValidationCheck {

	private List<String> featuresList = new ArrayList<String>();
	private List<String> featuresNoQualList = new ArrayList<String>();

	private static final String QUALIFIERS_REQUIRED_ID_1 = "FeatureQualifiersRequiredCheck";
	private static final String QUALIFIERS_REQUIRED_ID_2 = "FeatureQualifiersRequiredCheck";

	public FeatureQualifiersRequiredCheck() {
	}

	private void init() {
		DataSet keySet = GlobalDataSets.getDataSet(FileName.FEATURE_REQUIRE_QUALIFIERS);
		DataSet noKeySet = GlobalDataSets.getDataSet(FileName.FEATURE_NOT_REQUIRE_QUALIFIERS);
		if (keySet != null) {
			for (DataRow dataRow : keySet.getRows()) {
				String key = dataRow.getString(0);
				featuresList.add(key);
			}
		}
		if (noKeySet != null) {
			for (DataRow dataRow : noKeySet.getRows()) {
				String nkey = dataRow.getString(0);
				featuresNoQualList.add(nkey);
			}
		}
	}

	public ValidationResult check(Feature feature) {
		init();
		result = new ValidationResult();

		if (feature == null) {
			return result;
		}

		if (featuresList.contains(feature.getName())) {// check the key is in
														// the CV
			if (feature.getQualifiers().size() == 0) {
				reportError(feature.getOrigin(), QUALIFIERS_REQUIRED_ID_1,
						feature.getName());
			}
//		} else if (!featuresList.contains(feature.getName())
//				&& !featuresNoQualList.contains(feature.getName())) {
//
//			if (feature.getQualifiers().size() == 0) {
//				reportWarning(feature.getOrigin(), QUALIFIERS_REQUIRED_ID_2,
//						feature.getName());
//			}

		}

		return result;
	}
}
