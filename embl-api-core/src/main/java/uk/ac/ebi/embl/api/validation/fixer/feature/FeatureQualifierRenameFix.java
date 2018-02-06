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

@Description(" feature Qualifier\\\"{0}\\\" has been replaced with \\\"{1}\\\"")
public class FeatureQualifierRenameFix extends FeatureValidationCheck {

	private HashMap<String, String> featureQualifierMap = new HashMap<String, String>();

	private final static String FEATURE_QUALIFIER_RENAME_FIX_ID = "FeatureQualifierRenameFix";

	public FeatureQualifierRenameFix() {
	}

	private void init() {
		DataSet featureQualifierRenameSet = GlobalDataSets.getDataSet(FileName.FEATURE_QUALIFIER_RENAME);

		if (featureQualifierRenameSet != null) {

			for (DataRow frdataRow : featureQualifierRenameSet.getRows()) {
				String oldFeatureQualifierName = Utils.parseTSVString(frdataRow
						.getString(0));
				String newFeatureQualifierName = Utils.parseTSVString(frdataRow
						.getString(1));
//				System.out.println("oldFeatureName=" + oldFeatureQualifierName
//						+ "newFeatureName" + newFeatureQualifierName);
				featureQualifierMap.put(oldFeatureQualifierName,
						newFeatureQualifierName);
			}

		}
	}

	public ValidationResult check(Feature feature) {
		init();
		result = new ValidationResult();

		if (feature == null) {
			return result;
		}

		for (Qualifier qualifier : feature.getQualifiers()) {

			String qualifierName = qualifier.getName();

			if (featureQualifierMap.containsKey(qualifierName)) {

				String newqualifiervalue = featureQualifierMap
						.get(qualifierName);
				renameFeatureQualifier(qualifier, newqualifiervalue);
			}

		}

		return result;

	}

	private void renameFeatureQualifier(Qualifier qualifier, String newName) {
		String oldName = qualifier.getName();

		qualifier.setName(newName);

        /**
         * spacial case - label - note needs the value prepending with "label:"
         */
        if(oldName.equals(Qualifier.LABEL_QUALIFIER_NAME)){
            String oldQualifierValue = qualifier.getValue();
            qualifier.setValue("label:" + oldQualifierValue);
//            System.out.println("\'"+qualifier.getName()+"\' value has been changed to: \'"+qualifier.getValue()+"\'");
        }

		reportMessage(Severity.FIX, qualifier.getOrigin(),
				FEATURE_QUALIFIER_RENAME_FIX_ID, oldName, newName);
	}

}
