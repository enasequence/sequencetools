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
package uk.ac.ebi.embl.api.validation.check.sequence;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Checks that extended stretches of n characters in the sequence are matched by a gap feature
 */

@Description("Sequence contains a stretch of 'n' characters between base {0} and {1} that is not " +
        "represented with a \"gap\" feature (stretches of n greater than {2} gives a warning, greater than {3} gives an error).")
@ExcludeScope(validationScope={ValidationScope.EMBL_TEMPLATE})//do not run in template mode - no ability to add gaps
public class SequenceToGapFeatureBasesCheck extends EntryValidationCheck {

    protected final static String MESSAGE_ID = "SequenceToGapFeatureBasesCheck-1";
    public static int ERROR_THRESHOLD = Entry.DEFAULT_MIN_GAP_LENGTH;
    public static int WARNING_THRESHOLD = 0;
    public static int GAP_ESTIMATED_LENGTH = 100;
    public static String GAP_ESTIMATED_LENGTH_STRING = Integer.toString(GAP_ESTIMATED_LENGTH);
  
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

        String dataClass = entry.getDataClass();
        if (dataClass != null && dataClass.equals(Entry.PRT_DATACLASS)) {
            return result;
        }
        //collect all gap features
        Collection<Feature> gapFeatures =
                SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry);
        Collection<Feature> assembly_gapFeatures =
            SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
        gapFeatures.addAll(assembly_gapFeatures); //assembly_gaps and gaps are mutually exclusive,there is other check to complain
        if (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null||(Entry.CON_DATACLASS).equals(entry.getDataClass())) {
            return result;
        }

        int baseCount=0;
        List<NRegion> nRegions = new ArrayList<NRegion>();
        boolean lastBaseN = false;
        int matchStart = 0;
        int matchEnd = 0;
        byte[] sequenceByte=entry.getSequence().getSequenceByte();
        for(byte base:sequenceByte)
        {
        	
        	if('n'==(char)base)
        	{
        		if (!lastBaseN) 
        		{
                    matchStart = baseCount + 1;//as arrays are from 0 and sequence starts from 1
                    lastBaseN = true;
                }
        		
        	}
        	else 
        	{
                if (lastBaseN) 
                {
                    matchEnd = baseCount;
                    lastBaseN = false;
//                    System.out.println("matchStart = " + matchStart);
//                    System.out.println("matchEnd = " + matchEnd);
//                  System.out.println("matchedString = " + matchedString);
                    NRegion regionMatch = new NRegion(matchStart, matchEnd);
                    nRegions.add(regionMatch);
                }
        	
            }
        	baseCount++;
        }
    
        for (NRegion nRegion : nRegions) {
        	 
         	
         	boolean matchedRegion = false;
        	//whether we have found a gap feature that corresponds to this nRegion
            for (Feature gapFeature : gapFeatures) {
            	CompoundLocation<Location> compoundLocation = gapFeature.getLocations();
                if (compoundLocation == null || compoundLocation.getLocations() == null ||
                        compoundLocation.getLocations().size() != 1) {
                    //if there is more than 1 location, just bail, there are other checks to complain if the location is
                    //not a single location
                    break;
                }

                Location location = compoundLocation.getLocations().get(0);
                Long featureStart = location.getBeginPosition();
                Long featureEnd = location.getEndPosition();

                if (nRegion.getStart() == featureStart && nRegion.getEnd() == featureEnd) {
                    matchedRegion = true;
                    break;
                }
            }

              if (!matchedRegion) {
                  processMissingGapFeature(entry, nRegion);
              }
          
        }
        
      
        finish(entry);

        return result;
    }

    protected void finish(Entry entry) {
        //do nothing here - used in SequenceToGapFeatureBasesFix 
    }

    protected void processMissingGapFeature(Entry entry, NRegion nRegion) {
    	
    	if(getEmblEntryValidationPlanProperty().minGapLength.get()!=0)
    	{
    		ERROR_THRESHOLD=getEmblEntryValidationPlanProperty().minGapLength.get();
    	}
    	else
    	{
    		ERROR_THRESHOLD=Entry.DEFAULT_MIN_GAP_LENGTH;
    	}
    	
			if (nRegion.getLength() > ERROR_THRESHOLD)
			{
				reportError(entry.getSequence().getOrigin(), MESSAGE_ID, nRegion.getStart(), nRegion.getEnd(), WARNING_THRESHOLD,
						ERROR_THRESHOLD);
			} else if (nRegion.getLength() > WARNING_THRESHOLD)
			{
				reportWarning(entry.getSequence().getOrigin(), MESSAGE_ID, nRegion.getStart(), nRegion.getEnd(), WARNING_THRESHOLD,
						ERROR_THRESHOLD);
			}
		}

   public  class NRegion {
        int start;
        int end;

        public NRegion(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getLength() {
            return (end - start) + 1;
        }
    }
}
