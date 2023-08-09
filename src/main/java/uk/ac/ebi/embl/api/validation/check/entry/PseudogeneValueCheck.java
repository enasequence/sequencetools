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

import java.util.*;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

/** Checks pseudogene qualifier has valid allowed value */
@Description("pseudogene qualifier value \"{0}\" is invalid. Allowed values are: {1}")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class PseudogeneValueCheck extends EntryValidationCheck {

  protected static final String PSEUDOGENE_INVALID_VALUE_CHECK = "PseudogeneInvalidValueCheck";

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    Set<String> psedudogeneValues =
        new HashSet<>(Arrays.asList("processed", "unprocessed", "unitary", "allelic", "unknown"));

    Collection<Feature> pseudoGeneFeatures =
        SequenceEntryUtils.getFeaturesContainingQualifier(
            Qualifier.PSEUDOGENE_QUALIFIER_NAME, entry);

    if (pseudoGeneFeatures == null || pseudoGeneFeatures.isEmpty()) {
      return result;
    }

    for (Feature feat : pseudoGeneFeatures) {
      Qualifier psedogeneQual = feat.getSingleQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME);
      if (psedogeneQual != null && !psedudogeneValues.contains(psedogeneQual.getValue())) {
        reportError(
            feat.getOrigin(),
            PSEUDOGENE_INVALID_VALUE_CHECK,
            psedogeneQual.getValue(),
            psedudogeneValues);
      }
    }

    return result;
  }
}
