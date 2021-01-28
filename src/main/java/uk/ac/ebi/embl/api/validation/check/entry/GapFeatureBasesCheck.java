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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.Collection;

/**
 * Checks that gap feature locations correspond to n's in the sequence
 */
@Description("\"gap\" features must span a set of bases that are only 'n'.\n" +
        "The subsequence matched by locations {0}-{1} is \\\"{2}\\\".")
public class GapFeatureBasesCheck extends EntryValidationCheck {

    protected final static String MESSAGE_ID = "GapFeatureBasesCheck-1";
    protected final static String FAULTY_SEQUENCE_MESSAGE = "GapFeatureBasesCheck-2";

    /**
     * Checks the coverage of sequence by source features' locations.
     *
     * @param entry an entry to be checked (expected type is Entry)
     * @return a validation result
     */
    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null) {
            return result;
        }

        //collect all gene features
        Collection<Feature> gapFeatures =
                SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry);
      
        gapFeatures.addAll(SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry));
     	
		if (gapFeatures.isEmpty())
		{
			return result;
		}

        if (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null) {
            return result;
        }

        for (Feature gapFeature : gapFeatures) {

            CompoundLocation<Location> compoundLocation = gapFeature.getLocations();
            if (compoundLocation == null || compoundLocation.getLocations() == null || compoundLocation.getLocations().size() != 1) {
                //if there is more than 1 location, just bail, there are other checks to complain if the location is
                //not a single location
                return result;
            }

            Location location = compoundLocation.getLocations().get(0);
            Long start = location.getBeginPosition();
            Long end = location.getEndPosition();
            
           
            byte[] sequenceByte=entry.getSequence().getSequenceByte();
            if(sequenceByte==null||start==null||end==null)
            	return result;
            if(start<0||end>sequenceByte.length)
            {
            	return result;
            }
           int beginPosition=start.intValue();
           int endPosition=end.intValue();
           
			for (int i=beginPosition;i<endPosition;i++)
			{
				if ('n' != (char) sequenceByte[i])
				{
					ValidationMessage<Origin> message = reportError(gapFeature.getOrigin(), MESSAGE_ID);
					String report = ValidationMessageManager.getString(FAULTY_SEQUENCE_MESSAGE, start, end);
					message.setReportMessage(report);
					return result;
				}
			}

		}

        return result;
    }
}
