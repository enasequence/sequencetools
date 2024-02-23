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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import java.util.Arrays;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

public class EC_numberValueFix extends FeatureValidationCheck {

  private static final String Ec_numberValueFix_ID = "Ec_numberValueFix";
  private static final String EcNumberEmptyValueFix = "Ec_numberEmptyValueFix";
  private static final String[] INVALID_EC_NUMBER = {"-.-.-.-", "-.-.-", "-.-", "-"};

  public EC_numberValueFix() {}

  public ValidationResult check(Feature feature) {
    result = new ValidationResult();

    if (feature == null) {
      return result;
    }

    for (Qualifier ecqualifier : feature.getQualifiers(Qualifier.EC_NUMBER_QUALIFIER_NAME)) {
      if (ecqualifier.getValue().equalsIgnoreCase("deleted")) {
        feature.removeQualifier(ecqualifier);
        reportMessage(Severity.FIX, ecqualifier.getOrigin(), Ec_numberValueFix_ID);
      }

      if (Arrays.asList(INVALID_EC_NUMBER).contains(ecqualifier.getValue())) {
        feature.removeQualifier(ecqualifier);
        reportMessage(Severity.FIX, ecqualifier.getOrigin(), EcNumberEmptyValueFix);
      }
    }
    return result;
  }
}
