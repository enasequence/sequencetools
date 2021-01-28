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

import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.sql.SQLException;
import java.util.List;

@Description("The feature has no location.\\Feature location missing.\\The begin and end position of a sequence span are in the wrong order."
		+ " {0} feature  location is overlapped")
public class FeatureLocationCheck extends FeatureValidationCheck {

    private static final String NO_LOCATION_ID = "FeatureLocationCheck-1";
    private static final String LOCATION_MISSING_ID = "FeatureLocationCheck-2";
    private static final String LOCATION_ORDER_ID = "FeatureLocationCheck-3";
    private static final String INVALID_REMOTELOCATION_ID = "FeatureLocationCheck-5";

    public ValidationResult check(Feature feature) throws ValidationEngineException {
        result = new ValidationResult();

        if (feature == null) {
			return result;
		}
        
      try
      {

        CompoundLocation<Location> compoundLocation = feature.getLocations();

        if (compoundLocation == null
				|| compoundLocation.getLocations() == null 
				|| compoundLocation.getLocations().size() == 0) {
			reportError(feature.getOrigin(), NO_LOCATION_ID);
			return result;
		}
        
        List<Location> locations = compoundLocation.getLocations();
        if(locations == null){
            return result;
        }

        for (Location location : locations) {

            if (location.getBeginPosition() == null ) {
				reportError(feature.getOrigin(), LOCATION_MISSING_ID);
			}

            if (location.getEndPosition() == null || (
					location.getBeginPosition() != null &&
					location.getBeginPosition() > location.getEndPosition())) {
				reportError(feature.getOrigin(), LOCATION_ORDER_ID);
			}
            if(location instanceof RemoteLocation)
            {
            	if(getEntryDAOUtils()!=null&&!getEntryDAOUtils().isEntryExists(((RemoteLocation) location).getAccession()))
            	{
            		reportError(feature.getOrigin(), INVALID_REMOTELOCATION_ID,feature.getName());	
            	}
            }
            }
        if(feature instanceof CdsFeature && ((CdsFeature)feature).isPseudo())
        	return result;

      }catch(SQLException e)
      {
    	  throw new ValidationEngineException(e.getMessage(), e);
      }
      return result;
	}

}
