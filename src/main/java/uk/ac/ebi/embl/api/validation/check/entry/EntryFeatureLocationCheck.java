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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("No feature end position exceeds the entry sequence length")
@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class EntryFeatureLocationCheck extends EntryValidationCheck {

  private static final String MESSAGE_ID = "EntryFeatureLocationCheck";

  public EntryFeatureLocationCheck() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    List<Feature> features = entry.getFeatures();
    if (features == null || features.isEmpty()) {
      return result;
    }

    Sequence sequence = entry.getSequence();
    if (sequence == null) {
      return result;
    }

    Long sequenceLength = sequence.getLength();

    for (Feature feature : features) {
      CompoundLocation<Location> compoundLocation = feature.getLocations();
      for (Location location : compoundLocation.getLocations()) {
        if (location instanceof RemoteLocation) continue;
        Long endPosition = location.getEndPosition();
        if (endPosition != null && endPosition > sequenceLength) {
          reportError(
              feature.getOrigin(), MESSAGE_ID, endPosition.toString(), sequenceLength.toString());
        }
      }
    }

    return result;
  }
}
