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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

/**
 * @author dlorenc
 *
 * It is an implementation of validation check. It checks the coverage of 
 * sequence by source features' locations.
 */
@Description("The sequence is not fully covered by source features. The source features exceed the total sequence length.")

@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})

public class SequenceCoverageCheck extends EntryValidationCheck {
	//todo no check for this class
	private final static String MESSAGE_ID_SHORT = "SequenceCoverageCheck-1";
	private final static String MESSAGE_ID_LONG = "SequenceCoverageCheck-2";
	private final static String MESSAGE_ID_FIRST_BASE_ERROR = "SequenceCoverageCheck-3";
	private final static String MESSAGE_ID_NO_LOCATIONS = "SequenceCoverageCheck-4";
	private final static String MESSAGE_ID_GAPS_IN_LOCATIONS = "SequenceCoverageCheck-5";
	private final static String MESSAGE_ID_INVALID_CONTIG_LOCATIONS = "SequenceCoverageCheck-6";
	private final static String MESSAGE_ID_TRANSGENIC_SEQUENCE_COVERAGE = "SequenceCoverageCheck-7";


	/**
	 * Checks the coverage of sequence by source features' locations.
	 * 
	 * @param entry an entry to be checked (expected type is Entry)
	 * @return a validation result
	 */
	public ValidationResult check(Entry entry) {
        result = new ValidationResult();
        
		// checks the CONTIG/CO line locations cover the sequence length
		if (entry.getSequence().getContigs().size() != 0) {
			result = checkContigLocation(entry);
		}

		//collect all sources
		List<SourceFeature> sources = new ArrayList<SourceFeature>();
		for (Feature feature : entry.getFeatures()) {
			if (feature instanceof SourceFeature) {
				sources.add((SourceFeature) feature);
			}
		}

        if (sources.isEmpty()) {
			return result;
		}
		
		//checks sequence
		Sequence sequence = entry.getSequence();
		if (sequence == null) {
			return result;
		}
		long sequenceSize = sequence.getLength();
		
		//collection sources' locations
		List<Location> sourceLocations = new ArrayList<Location>();
		Map<Location, Feature> locationMap = new HashMap<Location, Feature>();
		boolean isSourceFocuswithFullSequenceCoverage=false;
		
		for (SourceFeature source : sources) {
			List<Location> locations = source.getLocations().getLocations();
//            Origin sourceOrigin = source.getOrigin();
            if (!locations.isEmpty()) {
				Location singleLocation = locations.iterator().next();
				sourceLocations.add(singleLocation);
				locationMap.put(singleLocation, source);
			}
    		//sourcefeature with transgenic has to cover whole sequence
            if(source.isTransgenic()&&sequenceSize>source.getLength())
            {
            	reportError(source.getOrigin(),MESSAGE_ID_TRANSGENIC_SEQUENCE_COVERAGE );
            }
            
            if(source.isFocus()&&sequenceSize==source.getLength())
            {
            	isSourceFocuswithFullSequenceCoverage=true;
            }
		}

        /**
         * get the origin of the first source feature - all errors will point to this
         */
        Origin firstSourceOrigin = sources.get(0).getOrigin();

		if (sourceLocations.isEmpty()) {
			reportCoverageError(result, entry.getOrigin(), MESSAGE_ID_NO_LOCATIONS, 0, sequenceSize);
			return result;
		}

		//sorts location so they will be checked in proper order
		Collections.sort(sourceLocations, (o1,o2)->o1.getBeginPosition().compareTo(o2.getBeginPosition()));


        Iterator<Location> locationIter = sourceLocations.iterator();
		Location location = (Location) locationIter.next();
		Long[] firstLocation = checkLocation(location);

		//check if lastLocationPositions exist
        //todo I dont understand why I am doing this. I assign the start location to the end location if it is null, but then exit - so why bother? It is a local copy of the locations so the assignation will not be kept
		if (firstLocation[0] == null || firstLocation[1] == null) {
			if (firstLocation[1] == null && firstLocation[0] != null) {
				firstLocation[1] = firstLocation[0];
			}
			return result;
		}
		
		//checks first location
		if (firstLocation[0] != 1) {
            result.append(EntryValidations.createMessage(firstSourceOrigin, Severity.ERROR, MESSAGE_ID_FIRST_BASE_ERROR, firstLocation[0]));
			return result;
		}

        int basePositionComparison = firstLocation[1].compareTo(sequenceSize);
        if (sourceLocations.size() == 1 && basePositionComparison < 0) {//shorter than
			reportCoverageError(result, firstSourceOrigin, MESSAGE_ID_SHORT, firstLocation[1], sequenceSize);
			return result;		
		}else if(sourceLocations.size() == 1 && basePositionComparison > 0){//longer than
            reportCoverageError(result, firstSourceOrigin, MESSAGE_ID_LONG, firstLocation[1], sequenceSize);
            return result;
        }
		
		if (sourceLocations.size() == 1) {
			return result;
		}
			
		
		//check further locations
		Long[] lastLocationPositions = null;
		for (; locationIter.hasNext();) {
			location = (Location) locationIter.next();
						lastLocationPositions = checkLocation(location);

			if ((firstLocation[1].longValue() + 1) != lastLocationPositions[0]&&!isSourceFocuswithFullSequenceCoverage){
				reportCoverageError(result, firstSourceOrigin, MESSAGE_ID_GAPS_IN_LOCATIONS, 0, sequenceSize);
				return result;
            }
			
			firstLocation[1] = lastLocationPositions[1];
		}

        //checks the last location
        if (lastLocationPositions != null && lastLocationPositions[1] != null) {
            if (lastLocationPositions[1].compareTo(sequenceSize) > 0) {//longer
                reportCoverageError(result, firstSourceOrigin, MESSAGE_ID_LONG, lastLocationPositions[1], sequenceSize);
                return result;
            }else if (lastLocationPositions[1].compareTo(sequenceSize) < 0) {//shorter
                reportCoverageError(result, firstSourceOrigin, MESSAGE_ID_SHORT, lastLocationPositions[1], sequenceSize);
                return result;
            }
        }
        return result;
    }

	/**
	 * Checks and swaps positions if in wrong order.
	 * 
	 * @param location location to be checked
	 * @return an array of positions (in proper order)
	 */
	private Long[] checkLocation(Location location) {
		Long[] positions = new Long[2];
		if (location.isComplement()) {
			positions[0] = location.getEndPosition();
			positions[1] = location.getBeginPosition();
		} else {
			positions[0] = location.getBeginPosition();
			positions[1] = location.getEndPosition();
		}
		return positions;
	}
	
	/**
	 * Adds error to the result.
	 * 
	 * @param result a reference to validation result  
	 * @param origin the origin
	 */
	private void reportCoverageError(ValidationResult result, Origin origin, String messageId, long coverage, long actualLength) {
		result.append(EntryValidations.createMessage(origin, Severity.ERROR, messageId, coverage, actualLength));
	}
	
	/*
	 * checks the CONTIG/CO line locations cover the sequence length
	 */

	private ValidationResult checkContigLocation(Entry entry) {
		List<Location> locations = entry.getSequence().getContigs();
		Long contigSequenceCoverLength = new Long(0);
		Long sequenceLength = entry.getSequence().getLength();
		for (Location location : locations) {
			contigSequenceCoverLength += location.getLength();

		}

		if (!sequenceLength.equals(contigSequenceCoverLength)) {

			result.append(EntryValidations.createMessage(entry.getSequence().getContigs()
					.get(0).getOrigin(), Severity.ERROR,
					MESSAGE_ID_INVALID_CONTIG_LOCATIONS,
					contigSequenceCoverLength, sequenceLength));
		}

		return result;
	}

}
