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

import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.Utils;


@Description("Qualifier \\\"{0}\\\" must have one of values {1} when qualifier \\\"{2}\\\" has value \\\"{3}\\\".")
public class QualifierValueRequiredQualifierValueCheck extends FeatureValidationCheck {
    
   	private final static String MESSAGE_ID = "QualifierValueRequiredQualifierValueCheck";

	public QualifierValueRequiredQualifierValueCheck() {
	}

    public ValidationResult check(Feature feature) {
	    DataSet dataSet = GlobalDataSets.getDataSet(FileName.QUALIFIER_VALUE_REQ_QUALIFIER_VALUE);
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        for (DataRow row : dataSet.getRows()) {
            String requiredQualifierName = row.getString(0);
            String[] requiredQualifierValues = row.getStringArray(1);
            String qualifierName = row.getString(2);
            String qualifierValue = row.getString(3);

            if (qualifierName == null || requiredQualifierName == null
                    || ArrayUtils.isEmpty(requiredQualifierValues)) {
                continue;
            }

            if (!SequenceEntryUtils.isQualifierWithValueAvailable(qualifierName, qualifierValue, feature)) {
                continue;
            }

            Collection<Qualifier> qualifiers = feature.getQualifiers(qualifierName);
            Collection<Qualifier> requiredQualifiers = feature.getQualifiers(requiredQualifierName);

            if (requiredQualifiers.isEmpty()) {
                continue;
            }

            String reqQualifierValuesStr = Utils.paramArrayToString(requiredQualifierValues);

            for (Qualifier qualifier : qualifiers) {
                if (qualifier != null && qualifier.getValue() != null && qualifier.getValue().equals(qualifierValue)) {
                    for (Qualifier requiredQualifier : requiredQualifiers) {
                        if (requiredQualifier == null) {
                            continue;
                        }
                        if (!ArrayUtils.contains(requiredQualifierValues, requiredQualifier.getValue())) {
                            reportError(feature, requiredQualifierName, reqQualifierValuesStr, qualifierName, qualifierValue);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void reportError(Feature feature, String qualifierName,
			String qualifierValue, String requiredQualifierName,
			String reqQualifierValuesStr) {
		reportError(feature.getOrigin(), MESSAGE_ID, qualifierName,
				qualifierValue, requiredQualifierName,
				reqQualifierValuesStr);
	}
}
