/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
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
import java.util.function.Predicate;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class SequenceCoverageCheck extends EntryValidationCheck {

  private static final String MESSAGE_ID_INVALID_START_POSITION = "SequenceCoverageCheck-2";
  private static final String MESSAGE_ID_INVALID_END_POSITION = "SequenceCoverageCheck-1";
  private static final String MESSAGE_ID_TRANSGENIC_SINGLE_SOURCE = "SequenceCoverageCheck-4";
  private static final String MESSAGE_ID_GAP_BETWEEN_LOCATIONS = "SequenceCoverageCheck-5";
  private static final String MESSAGE_ID_INVALID_CONTIG_LOCATIONS = "SequenceCoverageCheck-6";
  private static final String MESSAGE_ID_TRANSGENIC_SEQUENCE_COVERAGE = "SequenceCoverageCheck-7";

  private static final String MESSAGE_ID_SEQUENCE_COVERAGE = "SequenceCoverageCheck-3";
  private static final String MESSAGE_ID_TRANSGENIC_FOCUS_OCCURRENCE = "SequenceCoverageCheck-8";
  private static final String MESSAGE_ID_LOCATION_OVERLAP = "SequenceCoverageCheck-9";

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

    validateContigsFullCoverage(entry);

    List<SourceFeature> sources = new ArrayList<>();
    for (Feature feature : entry.getFeatures()) {
      if (feature instanceof SourceFeature) {
        SourceFeature source = (SourceFeature) feature;
        sources.add(source);
      }
    }
    if (sources.isEmpty()) {
      return result;
    }

    Origin firstSourceOrigin = sources.get(0).getOrigin();

    if (sources.stream().filter(source -> source.isTransgenic() || source.isFocus()).count() > 1) {
      // Only one /focus or /transgenic qualifier is allowed.
      reportError(firstSourceOrigin, MESSAGE_ID_TRANSGENIC_FOCUS_OCCURRENCE);
      return result;
    }

    long sequenceLength = entry.getSequence().getLength();

    if (sources.stream().anyMatch(SourceFeature::isTransgenic)) {
      // Validate sources when transgenic qualifier is present.
      //

      // At least one other source is required with /transgenic.
      if (sources.size() == 1) {
        reportError(firstSourceOrigin, MESSAGE_ID_TRANSGENIC_SINGLE_SOURCE);
        return result;
      }

      // Transgenic source must cover the whole sequence.
      List<Location> transgenicLocations = sortedLocations(sources, SourceFeature::isTransgenic);
      if (!validateFullCoverageNoGapsOrOverlaps(
          transgenicLocations, sequenceLength, firstSourceOrigin)) {
        reportError(firstSourceOrigin, MESSAGE_ID_TRANSGENIC_SEQUENCE_COVERAGE);
        return result;
      }

      // Other sources must not overlap.
      List<Location> nonTransgenicLocations = sortedLocations(sources, s -> !s.isTransgenic());
      validateWithinSequenceNoOverlaps(nonTransgenicLocations, sequenceLength, firstSourceOrigin);
    } else {
      // Validate sources when transgenic qualifier is not present.
      //

      // Sources must cover the whole sequence and must not overlap.
      List<Location> allLocations = sortedLocations(sources);
      if (!validateFullCoverageNoGapsOrOverlaps(allLocations, sequenceLength, firstSourceOrigin)) {
        reportError(firstSourceOrigin, MESSAGE_ID_SEQUENCE_COVERAGE);
      }
    }

    return result;
  }

  /** Validate that contigs cover the whole sequence. */
  private void validateContigsFullCoverage(Entry entry) {
    if (entry.getSequence().getContigs() != null && entry.getSequence().getContigs().size() != 0) {
      List<Location> locations = entry.getSequence().getContigs();
      long contigCoverageLength = 0L;
      for (Location location : locations) {
        contigCoverageLength += location.getLength();
      }
      long sequenceLength = entry.getSequence().getLength();
      if (sequenceLength != contigCoverageLength) {
        Origin firstContigOrigin = entry.getSequence().getContigs().get(0).getOrigin();
        reportError(
            firstContigOrigin,
            MESSAGE_ID_INVALID_CONTIG_LOCATIONS,
            contigCoverageLength,
            sequenceLength);
      }
    }
  }

  private static List<Location> sortedLocations(List<SourceFeature> sources) {
    return sortedLocations(sources, e -> true);
  }

  private static List<Location> sortedLocations(
      List<SourceFeature> sources, Predicate<SourceFeature> add) {
    List<Location> locations = new ArrayList<>();
    for (SourceFeature source : sources) {
      if (add.test(source)) {
        locations.addAll(source.getLocations().getLocations());
      }
    }
    locations.sort(Comparator.comparing(location -> minPosition(location)));
    return locations;
  }

  private void validateWithinSequenceNoOverlaps(
      List<Location> locations, long sequenceLength, Origin firstSourceOrigin) {
    Iterator<Location> locationIter = locations.iterator();
    Location location = locationIter.next();
    if (!validateStartPositionWithinSequence(location, sequenceLength, firstSourceOrigin)) {
      return;
    }
    while (locationIter.hasNext()) {
      Location prevLocation = location;
      location = locationIter.next();
      if (!validateNoOverlapBetweenLocations(prevLocation, location, firstSourceOrigin)) {
        return;
      }
    }
    validateEndPositionWithinSequence(location, sequenceLength, firstSourceOrigin);
  }

  /** Returns false if the locations do not cover the whole sequence. */
  private boolean validateFullCoverageNoGapsOrOverlaps(
      List<Location> locations, long sequenceLength, Origin firstSourceOrigin) {
    Iterator<Location> itr = locations.iterator();
    Location location = itr.next();
    if (!validateStartPositionIsOne(location, firstSourceOrigin)) {
      return false;
    }
    while (itr.hasNext()) {
      Location prevLocation = location;
      location = itr.next();
      if (!validateNoGapBetweenLocations(prevLocation, location, firstSourceOrigin)) {
        return false;
      }
      validateNoOverlapBetweenLocations(prevLocation, location, firstSourceOrigin);
    }
      return validateEndPositionIsSequenceLength(location, sequenceLength, firstSourceOrigin);
  }

  private boolean validateStartPositionIsOne(Location location, Origin firstSourceOrigin) {
    if (minPosition(location) != 1) {
      reportError(firstSourceOrigin, MESSAGE_ID_INVALID_START_POSITION, minPosition(location));
      return false;
    }
    return true;
  }

  private boolean validateStartPositionWithinSequence(
      Location location, long sequenceLength, Origin firstSourceOrigin) {
    if (minPosition(location) < 1 || minPosition(location) > sequenceLength) {
      reportError(firstSourceOrigin, MESSAGE_ID_INVALID_START_POSITION, minPosition(location));
      return false;
    }
    return true;
  }

  private boolean validateEndPositionIsSequenceLength(
      Location location, long sequenceLength, Origin firstSourceOrigin) {
    if (maxPosition(location) != sequenceLength) {
      reportError(
          firstSourceOrigin,
          MESSAGE_ID_INVALID_END_POSITION,
          maxPosition(location),
          sequenceLength);
      return false;
    }
    return true;
  }

  private boolean validateEndPositionWithinSequence(
      Location location, long sequenceLength, Origin firstSourceOrigin) {
    if (maxPosition(location) < 1 || maxPosition(location) > sequenceLength) {
      reportError(
          firstSourceOrigin,
          MESSAGE_ID_INVALID_END_POSITION,
          maxPosition(location),
          sequenceLength);
      return false;
    }
    return true;
  }

  private boolean validateNoGapBetweenLocations(
      Location prevLocation, Location location, Origin firstSourceOrigin) {
    if ((maxPosition(prevLocation) + 1) < minPosition(location)) {
      reportError(firstSourceOrigin, MESSAGE_ID_GAP_BETWEEN_LOCATIONS);
      return false;
    }
    return true;
  }

  private boolean validateNoOverlapBetweenLocations(
      Location prevLocation, Location location, Origin firstSourceOrigin) {
    if ((maxPosition(prevLocation) + 1) > minPosition(location)) {
      reportError(firstSourceOrigin, MESSAGE_ID_LOCATION_OVERLAP);
      return false;
    }
    return true;
  }

  private static long minPosition(Location location) {
    return location.getBeginPosition() <= location.getEndPosition()
        ? location.getBeginPosition()
        : location.getEndPosition();
  }

  private static long maxPosition(Location location) {
    return location.getBeginPosition() >= location.getEndPosition()
        ? location.getBeginPosition()
        : location.getEndPosition();
  }
}
