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
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.List;

// TODO Add support for CV_FLOCATION.
@Description("If source feature, must have single location (not a span)")
public class FeatureLocationTypeCheck extends FeatureValidationCheck {

	private final static String MESSAGE_ID = "FeatureLocationTypeCheck";
	
	public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
			return result;
		}

        if (feature.getLocations() == null) {
			return result;
		}

        List<Location> locations = feature.getLocations().getLocations();
		if (locations == null) {
			return result;
		}
		
		if (Feature.SOURCE_FEATURE_NAME.equals(feature.getName()) && locations.size() > 1) {
			reportError(feature.getOrigin(), MESSAGE_ID, Feature.SOURCE_FEATURE_NAME);
		}
		return result;
	}
}
