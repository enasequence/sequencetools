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
package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class FeatureLengthCheck extends FeatureValidationCheck {

  private static final String FEATURE_LENGTH_CHECK_ID = "FeatureLengthCheck-1";
  private static final long INTRON_FETURE_LENGTH = 10;
  private static final long EXON_FETURE_LENGTH = 15;

  @Override
  public ValidationResult check(Feature feature) {
    result = new ValidationResult();
    if (feature == null) return result;
    if (feature.getLocations() == null) return result;
    String featureName = feature.getName();
    Long length = feature.getLength();
    if (length == null) return result;
    if ((Feature.INTRON_FEATURE_NAME.equals(featureName)
        && length.longValue() < INTRON_FETURE_LENGTH)) {
      reportMessage(
          Severity.ERROR,
          feature.getOrigin(),
          FEATURE_LENGTH_CHECK_ID,
          featureName,
          INTRON_FETURE_LENGTH);
    } else if (Feature.EXON_FEATURE_NAME.equals(featureName)
        && length.longValue() < EXON_FETURE_LENGTH) {
      reportWarning(feature.getOrigin(), FEATURE_LENGTH_CHECK_ID, featureName, EXON_FETURE_LENGTH);
    }

    return result;
  }
}
