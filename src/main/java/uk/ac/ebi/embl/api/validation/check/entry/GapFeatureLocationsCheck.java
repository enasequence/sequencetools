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

import java.util.Collection;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

/**
 * Checks that features sharing the same locus tag are associated with the same gene and a stable
 * list of gene_synonyms
 */
@Description("bases immediately adjacent to gap location should not be 'n'")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class GapFeatureLocationsCheck extends EntryValidationCheck {

  protected static final String MESSAGE_ID = "GapFeatureLocationsCheck";

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    // collect all gene features
    Collection<Feature> gapFeatures =
        SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry);

    gapFeatures.addAll(SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry));

    if (gapFeatures.isEmpty()) {
      gapFeatures = SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
    }
    if (gapFeatures.isEmpty()) {
      return result;
    }

    if (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null) {
      return result;
    }

    for (Feature gapFeature : gapFeatures) {

      CompoundLocation<Location> compoundLocation = gapFeature.getLocations();
      if (compoundLocation == null
          || compoundLocation.getLocations() == null
          || compoundLocation.getLocations().size() != 1) {

        return result;
      }

      Location location = compoundLocation.getLocations().get(0);
      int start = location.getBeginPosition().intValue();
      int end = location.getEndPosition().intValue();

      byte[] sequenceByte = entry.getSequence().getSequenceByte();

      if (sequenceByte == null) return result;

      if (start <= 1 || sequenceByte.length <= end) {
        return result;
      }

      if ('n' == (char) sequenceByte[start - 2] || 'n' == (char) sequenceByte[end]) {
        reportError(gapFeature.getOrigin(), MESSAGE_ID);
      }

      /*	String sequenceString = entry.getSequence().getSequence();
      if ((sequenceString.length() > end)) {
                   /**
                    * to get the base after, we actually just use the feature end, as feature locations start from
                    * 1 and char arrays start from 0, so getting the end of feature is equivalent to getting the next
                    * character in the sequence
                    */
      /*	char next_base = sequenceString.charAt(end.intValue());


      	if (next_base == 'n') {
      		ValidationMessage<Origin> message = reportError(gapFeature.getOrigin(), MESSAGE_ID);
      	}
      }

      if (!(start <=0)) {*/

      /**
       * to get the base before, we take 2 from the feature start, as feature locations start from 1
       * and char arrays start from 0, so getting the start of feature is equivalent to getting the
       * next character from the start in the char array, so take 2 to get the previous base
       */

      /*if (start.intValue() >= 2)
      	{
      		char previous_base = sequenceString.charAt(start.intValue() - 2);

      		if (previous_base == 'n')
      		{
      			ValidationMessage<Origin> message = reportError(gapFeature.getOrigin(), MESSAGE_ID);

      		}
      	}
      }*/

    }

    return result;
  }
}
