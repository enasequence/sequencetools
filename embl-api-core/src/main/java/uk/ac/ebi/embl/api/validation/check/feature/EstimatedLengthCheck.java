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
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("\"estimated_length\" qualifier with value \"{1}\" must have a length of {1}, not \\\"{2}\\\"\\" +
        "\"estimated_length\" qualifier with value \"unknown\" must have a length of 100, not \\\"{1}\\\"\\" +
        "\"estimated_length qualifier value \"{1}\" is not a number. Must be a whole number or \"unknown\"")
public class EstimatedLengthCheck extends FeatureValidationCheck {

    private final static String WRONG_LENGTH_ID = "EstimatedLengthCheck-1";
    private final static String WRONG_ESTIMATED_LENGTH_ID = "EstimatedLengthCheck-2";
    private final static String WRONG_ESTIMATED_FORMAT_ID = "EstimatedLengthCheck-3";

    /**
     * The length of the feature required if the estimated_length is "unknown"
     */
    private static final int MAX_VALID_VALUES_SIZE = 100;

    public EstimatedLengthCheck() {
    }

    public void setPopulated() {
        super.setPopulated();
    }

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if(!feature.getName().equals(Feature.GAP_FEATURE_NAME)&&!feature.getName().equals(Feature.ASSEMBLY_GAP_FEATURE_NAME)){
            return result;
        }

        long featureLength = feature.getLocations().getLength();

        for (Qualifier estimatedLengthQualifier : feature.getQualifiers(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME)) {

            String value = estimatedLengthQualifier.getValue();

            if (value.equals("unknown")) {
                if (featureLength != MAX_VALID_VALUES_SIZE) {
                    reportError(estimatedLengthQualifier.getOrigin(), WRONG_ESTIMATED_LENGTH_ID, featureLength);
                }
            } else {
                try {
                    Long estimatedLength = new Long(value);
                    if (estimatedLength != featureLength) {
                        reportError(estimatedLengthQualifier.getOrigin(), WRONG_LENGTH_ID, value, featureLength);
                    }
                } catch (NumberFormatException e) {
                    reportError(estimatedLengthQualifier.getOrigin(), WRONG_ESTIMATED_FORMAT_ID, value);
                }
            }
        }

        return result;
    }
}
