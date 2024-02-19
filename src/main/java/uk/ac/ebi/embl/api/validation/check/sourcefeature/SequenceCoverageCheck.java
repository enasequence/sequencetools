/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import java.util.*;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

/**
 * @author dlorenc
 * <p>It is an implementation of validation check. It checks the coverage of sequence by source
 * features' locations.
 */
@Description(
        "The sequence is not fully covered by source features. The source features exceed the total sequence length.")
@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class SequenceCoverageCheck extends EntryValidationCheck {

    private static final String MESSAGE_ID_SHORT = "SequenceCoverageCheck-1";
    private static final String MESSAGE_ID_LONG = "SequenceCoverageCheck-2";
    private static final String MESSAGE_ID_FIRST_BASE_ERROR = "SequenceCoverageCheck-3";
    private static final String MESSAGE_ID_TRANSGENIC_SINGLE_SOURCE = "SequenceCoverageCheck-4";
    private static final String MESSAGE_ID_GAPS_IN_LOCATIONS = "SequenceCoverageCheck-5";
    private static final String MESSAGE_ID_INVALID_CONTIG_LOCATIONS = "SequenceCoverageCheck-6";
    private static final String MESSAGE_ID_TRANSGENIC_SEQUENCE_COVERAGE = "SequenceCoverageCheck-7";
    private static final String MESSAGE_ID_TRANSGENIC_FOCUS_OCCURRENCE = "SequenceCoverageCheck-8";
    private static final String MESSAGE_ID_LOCATIONS_OVERLAP = "SequenceCoverageCheck-9";

    private static final int BEGIN_POS = 0;
    private static final int END_POS = 1;

    /**
     * Checks the coverage of sequence by source features' locations.
     *
     * @param entry an entry to be checked (expected type is Entry)
     * @return a validation result
     */
    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null || entry.getSequence() == null) {
            return result;
        }
        // checks the CONTIG/CO line locations cover the sequence length
        if (entry.getSequence().getContigs() != null && entry.getSequence().getContigs().size() != 0) {
            result = checkContigLocation(entry);
        }

        // collect all sources
        List<SourceFeature> sources = new ArrayList<>();
        for (Feature feature : entry.getFeatures()) {
            if (feature instanceof SourceFeature) {
                sources.add((SourceFeature) feature);
            }
        }

        if (sources.isEmpty()) {
            return result;
        }

        // checks sequence
        Sequence sequence = entry.getSequence();
        if (sequence == null) {
            return result;
        }

        long sequenceLength = sequence.getLength();


    /* Transgenic sequences are validated differently. They have at least two source feature keys and
       the source feature key having the /transgenic qualifier must span the whole sequence. The /focus and
       /transgenic qualifiers are mutually exclusive.
     */

        boolean hasTransgenic = false;
        boolean hasFocus = false;
        Origin firstSourceOrigin = sources.get(0).getOrigin();

        for (SourceFeature source : sources) {
            if (source.isTransgenic() || source.isFocus()) {
                if (hasTransgenic || hasFocus) {
                    reportError(source.getOrigin(), MESSAGE_ID_TRANSGENIC_FOCUS_OCCURRENCE);
                }

                if (source.isTransgenic()) {
                    hasTransgenic = true;
                    if (sequenceLength > source.getLength())
                        reportError(source.getOrigin(), MESSAGE_ID_TRANSGENIC_SEQUENCE_COVERAGE);
                }

                if (source.isFocus()) {
                    hasFocus = true;
                }
            }
        }

        if (hasTransgenic && sources.size() == 1) {
            reportError(firstSourceOrigin, MESSAGE_ID_TRANSGENIC_SINGLE_SOURCE);
        }

        if (!hasTransgenic) {
            // Sort locations by begin position.
            List<Location> locations = new ArrayList<>();
            for (SourceFeature source : sources) {
                locations.addAll(source.getLocations().getLocations());
            }
            Collections.sort(locations, Comparator.comparing(Location::getBeginPosition));

            Iterator<Location> locationIter = locations.iterator();
            Long[] position = getPosition(locationIter.next());

            // checks first location
            if (position[BEGIN_POS] != 1) {
                result.append(
                        EntryValidations.createMessage(
                                firstSourceOrigin, Severity.ERROR, MESSAGE_ID_FIRST_BASE_ERROR, position[BEGIN_POS]));
                return result;
            }

            while (locationIter.hasNext()) {
                Long[] prevPosition = position;
                position = getPosition(locationIter.next());
                if ((prevPosition[END_POS] + 1) != position[BEGIN_POS]) { // not contiguous
                    if ((prevPosition[END_POS] + 1) > position[BEGIN_POS]) {
                        reportCoverageError(
                                result, firstSourceOrigin, MESSAGE_ID_LOCATIONS_OVERLAP, 0, sequenceLength);
                    } else {
                        reportCoverageError(
                                result, firstSourceOrigin, MESSAGE_ID_GAPS_IN_LOCATIONS, 0, sequenceLength);
                    }
                    return result;
                }
            }

            if (position[END_POS] > sequenceLength) {
                // Last location end position is after sequence length.
                reportCoverageError(
                        result, firstSourceOrigin, MESSAGE_ID_LONG, position[END_POS], sequenceLength);
            } else if (position[END_POS] < sequenceLength) {
                // Last location end position is before sequence length.
                reportCoverageError(
                        result, firstSourceOrigin, MESSAGE_ID_SHORT, position[END_POS], sequenceLength);
            }
        }

        return result;
    }

    /**
     * Returns begin and end position in correct order.
     */
    private Long[] getPosition(Location location) {
        Long[] positions = new Long[2];
        if (location.isComplement()) {
            positions[BEGIN_POS] = location.getEndPosition();
            positions[END_POS] = location.getBeginPosition();
        } else {
            positions[BEGIN_POS] = location.getBeginPosition();
            positions[END_POS] = location.getEndPosition();
        }
        return positions;
    }

    /**
     * Adds error to the result.
     *
     * @param result a reference to validation result
     * @param origin the origin
     */
    private void reportCoverageError(
            ValidationResult result, Origin origin, String messageId, long coverage, long actualLength) {
        result.append(
                EntryValidations.createMessage(origin, Severity.ERROR, messageId, coverage, actualLength));
    }

    /*
     * checks the CONTIG/CO line locations cover the sequence length
     */

    private ValidationResult checkContigLocation(Entry entry) {
        List<Location> locations = entry.getSequence().getContigs();
        Long contigSequenceCoverLength = 0L;
        Long sequenceLength = entry.getSequence().getLength();
        for (Location location : locations) {
            contigSequenceCoverLength += location.getLength();
        }

        if (!sequenceLength.equals(contigSequenceCoverLength)) {

            result.append(
                    EntryValidations.createMessage(
                            entry.getSequence().getContigs().get(0).getOrigin(),
                            Severity.ERROR,
                            MESSAGE_ID_INVALID_CONTIG_LOCATIONS,
                            contigSequenceCoverLength,
                            sequenceLength));
        }

        return result;
    }
}
