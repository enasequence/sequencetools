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

import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("The feature has no location.\\Feature location missing.\\The begin and end position of a sequence span are in the wrong order."
		+ " {0} feature  location is overlapped")
public class FeatureLocationCheck extends FeatureValidationCheck {

    private static final String NO_LOCATION_ID = "FeatureLocationCheck-1";
    private static final String LOCATION_MISSING_ID = "FeatureLocationCheck-2";
    private static final String LOCATION_ORDER_ID = "FeatureLocationCheck-3";
    private static final String LOCATION_OVERLAP_ID = "FeatureLocationCheck-4";

    private Entry entry;
    
    public void setEntry(Entry entry) {
        this.entry = entry;
    }
    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
			return result;
		}
        
      

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
            
            }
        if(compoundLocation.hasOverlappingLocation())
        {
        	if(!SequenceEntryUtils.isQualifierAvailable(Qualifier.RIBOSOMAL_SLIPPAGE, feature)&&!SequenceEntryUtils.isQualifierAvailable(Qualifier.TRANS_SPLICING, feature))
        	{
        		if(entry!=null&&entry.getSequence()!=null&&entry.getSequence().getTopology()==Topology.CIRCULAR)
        			return result;
        		reportError(feature.getOrigin(), LOCATION_OVERLAP_ID, feature.getName());
        	}
          		
        }

		return result;
	}

}
