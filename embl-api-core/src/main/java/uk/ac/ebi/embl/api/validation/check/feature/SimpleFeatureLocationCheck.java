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
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.Arrays;
import java.util.List;

@Description(
        "The \"{0}\" feature must be a simple location x..y where start location x is less than y. No complex locations or complement is permitted.")
public class SimpleFeatureLocationCheck extends FeatureValidationCheck {

    private static final String COMPLEX_LOCATION_ID = "SimpleFeatureLocationCheck-1";

    private static final List<String> simpleFeatures;

    static {
        simpleFeatures = Arrays.asList(Feature.GAP_FEATURE_NAME);
    }

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if (simpleFeatures.contains(feature.getName())) {

            CompoundLocation<Location> compoundLocation = feature.getLocations();

            if (compoundLocation == null
                    || compoundLocation.getLocations() == null
                    || compoundLocation.getLocations().size() == 0) {
                return result;//just leave, other checks for this
            }

            /**
             * take off any global complement so that we just need to examine the underlying locations for complementarity
             */
            compoundLocation.removeGlobalComplement();

            List<Location> locations = compoundLocation.getLocations();
            if (locations == null) {
                return result;
            }

            if(locations.size() > 1){
                reportError(feature.getOrigin(), COMPLEX_LOCATION_ID, feature.getName());
            }

            for (Location location : locations) {

                if (location.getBeginPosition() == null || location.getEndPosition() == null) {
                    return result;
                }

                if (location.getBeginPosition() > location.getEndPosition()) {
                    reportError(feature.getOrigin(), COMPLEX_LOCATION_ID, feature.getName());
                    return result;
                }

                if (location.isComplement()) {
                    reportError(feature.getOrigin(), COMPLEX_LOCATION_ID, feature.getName());
                    return result;
                }

                if (!(location instanceof LocalRange)) {
                    reportError(feature.getOrigin(), COMPLEX_LOCATION_ID, feature.getName());
                    return result;
                }

            }
        }

        return result;
    }

}
