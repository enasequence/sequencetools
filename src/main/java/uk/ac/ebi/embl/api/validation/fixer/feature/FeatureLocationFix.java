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
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

import java.util.List;

@Description(
        "Locations swapped from - ({0},{1}) to ({1},{0}) and made complement")
public class FeatureLocationFix extends FeatureValidationCheck {

    private static final String LOCATION_ORDER_ID = "FeatureLocationFix";
    private static final String USER_CONFUSED_ID = "FeatureLocationFix-2";

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if(feature.getSingleQualifier(Qualifier.TRANS_SPLICING) != null) {
            return result;
        }

        CompoundLocation<Location> compoundLocation = feature.getLocations();

        if (compoundLocation == null
                || compoundLocation.getLocations() == null
                || compoundLocation.getLocations().size() == 0) {
            return result;
        }
        

        List<Location> locations = compoundLocation.getLocations();//TODO

        if (locations.size() > 1 && !compoundLocation.isComplement()) {
            if (locations.stream().allMatch(Location::isComplement)) { //if all splits are complement,convert it to global complement
                compoundLocation.setGlobalComplement();
            }
        }

        for (Location location : locations) {

            Long beginPosition = location.getBeginPosition();
            Long endPosition = location.getEndPosition();

            if (location.getBeginPosition() == null || endPosition == null) {
                return result;//dont make a fuss - no errors in fixer, just bail out and leave to validator
            }

            if (beginPosition > endPosition){
                if (!location.isComplement()) {
                    location.setBeginPosition(endPosition);
                    location.setEndPosition(beginPosition);
                    location.setComplement(true);
                    reportMessage(Severity.FIX, feature.getOrigin(), LOCATION_ORDER_ID, beginPosition.toString(), endPosition.toString());
                } else {
                    reportError(feature.getOrigin(), USER_CONFUSED_ID, endPosition.toString(), beginPosition.toString());
                }
            }
        }
       feature.setLocations(compoundLocation.getSortedLocations());
        return result;
    }

}
