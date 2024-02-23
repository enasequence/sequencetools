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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description(
    "Qualifier circularRNA is not allowed in feature {0},  only CDS,mRNA or tRNA feature can contain circularRNA qualifier.")
public class CircularRNAQualifierCheck extends FeatureValidationCheck {

  private static final String CIRCULAR_RNA_NOT_ALLOWED = "QualifierNotAllowedInFeatureCheck";

  public CircularRNAQualifierCheck() {}

  public ValidationResult check(Feature feature) {

    result = new ValidationResult();

    if (feature == null) {
      return result;
    }

    Qualifier cirularRNAQual = feature.getSingleQualifier(Qualifier.CIRCULAR_RNA_QUALIFIER_NAME);
    if (cirularRNAQual != null
        && !feature.getName().equals(Feature.CDS_FEATURE_NAME)
        && !feature.getName().equals(Feature.mRNA_FEATURE_NAME)
        && !feature.getName().equals(Feature.tRNA_FEATURE_NAME)) {
      reportError(feature.getOrigin(), CIRCULAR_RNA_NOT_ALLOWED, feature.getName());
    }

    return result;
  }
}
