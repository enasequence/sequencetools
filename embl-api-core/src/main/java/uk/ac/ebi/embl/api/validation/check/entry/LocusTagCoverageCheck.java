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
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.*;

import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.LOCUS_TAG_QUALIFIER_NAME;

/**
 * Checks whether features sharing the same locus_tag are overlapping with other locus_tag feature locations
 */
@Description("Features sharing the locus_tag \\\"{0}\\\" have locations overlapping with locus_tag \\\"{1}\\\".")
@Deprecated //Locus tags can overlap after all
public class LocusTagCoverageCheck extends EntryValidationCheck {

    protected final static String MESSAGE_ID_OVERLAP_IN_LOCATIONS = "LocusTagCoverageCheck";

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

        if (entry.getSequence() == null) {
            return result;
        }

        //collect all locus features
        Collection<Feature> locusFeatures =
                SequenceEntryUtils.getFeaturesContainingQualifier(LOCUS_TAG_QUALIFIER_NAME, entry);
        Sequence sequence = entry.getSequence();

        if (locusFeatures.isEmpty()) {
            return result;
        }

        if (sequence == null || sequence.getTopology() == null) {
            return result;
        }

        /**
         * collect all the features that share the same locus tag
         */
        Map<String, List<Feature>> locusTagMap = new HashMap<String, List<Feature>>();
        for (Feature locus_feature : locusFeatures) {
            String locusTagValue = locus_feature.getQualifiers(LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();
            if (locusTagMap.containsKey(locusTagValue)) {
                locusTagMap.get(locusTagValue).add(locus_feature);
            } else {
                locusTagMap.put(locusTagValue, new ArrayList<Feature>(Arrays.asList(locus_feature)));
            }
        }

        List<LocusFeatures> compoundLocusFeatures = new ArrayList<LocusFeatures>();
        for (String locusTag : locusTagMap.keySet()) {
            compoundLocusFeatures.add(new LocusFeatures(locusTagMap.get(locusTag), locusTag));
        }

        for (int i = 0; i < compoundLocusFeatures.size(); i++) {
            LocusFeatures currentLocusTag = compoundLocusFeatures.get(i);

            for (int i2 = i + 1; i2 < compoundLocusFeatures.size(); i2++) {
                LocusFeatures nextLocusTag = compoundLocusFeatures.get(i2);

                if (isLocusFeatureLocationsOverlap(sequence, currentLocusTag.getMergedLocations(),
                        nextLocusTag.getMergedLocations())) {
                    reportCoverageWarning(result, entry.getOrigin(),
                            MESSAGE_ID_OVERLAP_IN_LOCATIONS,
                            currentLocusTag.getLocusTag(),
                            nextLocusTag.getLocusTag());
                }
            }
        }

        return result;
    }

    private boolean isLocusFeatureLocationsOverlap(Sequence sequence,
                                                   CompoundLocation<Location> currentMergedLocations,
                                                   CompoundLocation<Location> nextMergedLocations) {
        if (SequenceEntryUtils
                .doLocationsOverlap(currentMergedLocations, nextMergedLocations)) {

            /**
             * if the genome is not circular and/or neither of the locations cross the origin - report warning
             */
            if (!(sequence.getTopology().equals(Sequence.Topology.CIRCULAR) &&
                    (SequenceEntryUtils.isCircularBoundary(currentMergedLocations, sequence.getLength())) ||
                    SequenceEntryUtils.isCircularBoundary(nextMergedLocations, sequence.getLength()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds error to the result.
     *
     * @param result a reference to validation result
     * @param origin the origin
     * @param tag1
     * @param tag2
     */
    private void reportCoverageWarning(ValidationResult result, Origin origin, String messageId, String tag1,
                                       String tag2) {
        ValidationMessage<Origin> message =
                EntryValidations.createMessage(origin, Severity.WARNING, messageId, tag1, tag2);
        result.append(message);
    }

    public class LocusFeatures {
        List<Feature> features;
        String locusTag;
        CompoundLocation<Location> mergedLocations;

        public LocusFeatures(List<Feature> features, String locusTag) {
            this.features = features;
            this.locusTag = locusTag;

            mergedLocations = new Order<Location>();
            for (Feature feature : features) {
                CompoundLocation<Location> locationCompoundLocation = feature.getLocations();

                LocationFactory locationFactory = new LocationFactory();
                for (Location location : locationCompoundLocation.getLocations()) {
                    //make a copy of the locations - if we need to reverse the complement we don't what the original
                    // object to change (as checks should not change the data objects)
                    Location copyLocation = locationFactory.createLocalRange(location);//local range will do - don't care what the type is
                    if(locationCompoundLocation.isComplement()){
                        //reverse the complement if the compound location is complement
                        copyLocation.setComplement(!copyLocation.isComplement());
                    }
                    mergedLocations.addLocation(copyLocation);
                }
            }
        }

        public CompoundLocation<Location> getMergedLocations() {
            return mergedLocations;
        }

        public List<Feature> getFeatures() {
            return features;
        }

        public String getLocusTag() {
            return locusTag;
        }
    }
}
