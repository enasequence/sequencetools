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

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description(
    "\"propeptide\" feature length must be multiple of 3"
        + "\"propeptide\" feature must be located within CDS feature coordinates"
        + "\"propeptide\" feature must not overlap with either the sig_peptide or the mat_peptide")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class PropeptideLocationCheck extends EntryValidationCheck {

  private static final String LENGTH_MULTIPLE_OF_THREE_MESSAGE_ID = "PropeptideLocationCheck1";
  private static final String WITHIN_CDS_MESSAGE_ID = "PropeptideLocationCheck2";
  private static final String WITHIN_PEPTIDE_MESSAGE_ID = "PropeptideLocationCheck3";

  public ValidationResult check(Entry entry) {

    result = new ValidationResult();
    if (entry == null) return result;
    if (entry.getFeatures() == null || entry.getFeatures().size() == 0) return result;

    List<Feature> propetideFeatures =
        SequenceEntryUtils.getFeatures(Feature.PROPETIDE_FEATURE_NAME, entry);

    if (propetideFeatures == null || propetideFeatures.size() == 0) return result;

    List<Feature> cdsFeatures = SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry);
    List<Feature> peptideFeatures = new ArrayList<Feature>();
    peptideFeatures.addAll(SequenceEntryUtils.getFeatures(Feature.SIG_PEPTIDE_FEATURE_NAME, entry));
    peptideFeatures.addAll(SequenceEntryUtils.getFeatures(Feature.MAT_PEPTIDE_FEATURE_NAME, entry));

    for (Feature propetideFeature : propetideFeatures) {
      Long length = propetideFeature.getLength();
      boolean validPropetideFeature = false;

      if (length % 3 != 0) {
        reportError(propetideFeature.getOrigin(), LENGTH_MULTIPLE_OF_THREE_MESSAGE_ID);
      }
      if (cdsFeatures.size() > 0) {
        for (Feature feature : cdsFeatures) {
          if (propetideFeature.getLocations().getMinPosition()
                  >= feature.getLocations().getMinPosition()
              && propetideFeature.getLocations().getMaxPosition()
                  <= feature.getLocations().getMaxPosition()) {
            validPropetideFeature = true;
          }
        }
        if (!validPropetideFeature) {
          reportError(propetideFeature.getOrigin(), WITHIN_CDS_MESSAGE_ID);
        }
      }

      for (Feature feature : peptideFeatures) {
        if ((propetideFeature.getLocations().getMinPosition()
                    > feature.getLocations().getMinPosition()
                && propetideFeature.getLocations().getMinPosition()
                    < feature.getLocations().getMaxPosition())
            || (propetideFeature.getLocations().getMaxPosition()
                    > feature.getLocations().getMinPosition()
                && propetideFeature.getLocations().getMaxPosition()
                    < feature.getLocations().getMaxPosition())) {
          reportError(propetideFeature.getOrigin(), WITHIN_PEPTIDE_MESSAGE_ID);
        }
      }
    }

    return result;
  }
}
