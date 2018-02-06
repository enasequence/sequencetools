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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import java.util.*;

@Description(" feature \\\"{0}\\\" has been replaced with \\\"{1}\\\"")
public class FeatureRenameFix extends FeatureValidationCheck {

	private HashMap<String, String> featureMap = new HashMap<String, String>();

	private final static String FEATURE_RENAME_FIX_ID = "FeatureRenameFix";

	public FeatureRenameFix() {

	}

	private void init() {
		DataSet featureRenameSet = GlobalDataSets.getDataSet(FileName.FEATURE_RENAME);
		if (featureRenameSet != null) {

			for (DataRow frdataRow : featureRenameSet.getRows()) {
				String oldFeatureName = Utils.parseTSVString(frdataRow
						.getString(0));
				String newFeatureName = Utils.parseTSVString(frdataRow
						.getString(1));
				featureMap.put(oldFeatureName, newFeatureName);
			}

		}
	}

	public ValidationResult check(Feature feature) {
		init();
		result = new ValidationResult();
		String newValue = null;
		if (feature == null) {
			return result;
		}

		if (featureMap.containsKey(feature.getName())) {

			newValue = featureMap.get(feature.getName());

            /**
             * special case - we only rename repeat_region if the /mobile_element qualifier also exists (EMD-2607)
             */
            if (feature.getName().equals(Feature.REPEAT_REGION) &&
                    !SequenceEntryUtils.isQualifierAvailable(Qualifier.MOBILE_ELEMENT_NAME, feature)) {
                return result;
            } else {
                renameFeature(feature, newValue);
            }
        }

        return result;
	}

	private void renameFeature(Feature feature, String newName) {
		String oldName = feature.getName();

        feature.setName(newName);

        // if the feature name is "conflict" add the qualifier "note" with value
        // "conflict"
		if (oldName.equals(Feature.CONFLICT_FEATURE_NAME)) {
			feature.addQualifier(Qualifier.NOTE_QUALIFIER_NAME, "conflict");
		}

		reportMessage(Severity.FIX, feature.getOrigin(), FEATURE_RENAME_FIX_ID,
				oldName, newName);
	}

}
