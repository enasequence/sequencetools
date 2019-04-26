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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

import java.util.Collection;

@Description("Qualifier {0} must not exist when qualifier {1} has value {2}.")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class QualifierValueNotQualifierCheck extends FeatureValidationCheck {

    private final static String MESSAGE_ID = "QualifierValueNotQualifierCheck";

    public QualifierValueNotQualifierCheck() {
    }


    public ValidationResult check(Feature feature) {
        DataSet dataSet = GlobalDataSets.getDataSet(FileName.QUALIFIER_VALUE_NOT_QUALIFIER);
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        for (DataRow dataRow : dataSet.getRows()) {
            String notPermittedQualifierName = dataRow.getString(0);
            String qualifierName = dataRow.getString(1);
            String qualifierValue = dataRow.getString(2);

            if (qualifierName == null || notPermittedQualifierName == null) {
                return result;
            }

            if (!SequenceEntryUtils.isQualifierAvailable(notPermittedQualifierName, feature)) {
                continue;
            }

            Collection<Qualifier> qualifiers = feature.getQualifiers(qualifierName);
            for (Qualifier qualifier : qualifiers) {
                if (qualifier != null && qualifier.getValue() != null && qualifier.getValue().equals(qualifierValue)) {
                    reportError(feature.getOrigin(), MESSAGE_ID, notPermittedQualifierName, qualifierName, qualifierValue);
                }
            }
        }

        return result;
    }

}
