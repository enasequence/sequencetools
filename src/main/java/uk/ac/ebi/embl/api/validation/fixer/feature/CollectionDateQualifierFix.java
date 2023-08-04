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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("Collection_date qualifier value changed from \"{0}\" to \"{1}\"")
public class CollectionDateQualifierFix extends FeatureValidationCheck {

  private static final String CollectionDateQualifierFix_ID_1 = "CollectionDateQualifierFix_1";
  private static final Pattern INSDC_DATE_FORMAT_PATTERN_1 =
      Pattern.compile(
          "^(\\d{1})-((Jan)|(Feb)|(Mar)|(Apr)|(May)|(Jun)|(Jul)|(Aug)|(Sep)|(Oct)|(Nov)|(Dec))-(\\d{4})$"); // "DD-Mmm-YYYY"
  private static final Pattern NCBI_DATE_FORMAT_PATTERN =
      Pattern.compile(
          "^(\\d{1,2})-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-(\\d{2})$"); // "DD-Mmm-YY"

  public ValidationResult check(Feature feature) {
    result = new ValidationResult();

    if (feature == null) {
      return result;
    }

    List<Qualifier> collectionDateQualifiers =
        feature.getQualifiers(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);

    if (collectionDateQualifiers.isEmpty()) {
      return result;
    }
    for (Qualifier collectionDateQualifier : collectionDateQualifiers) {
      String collectionDateValue = collectionDateQualifier.getValue();
      if (INSDC_DATE_FORMAT_PATTERN_1.matcher(collectionDateValue).matches()) {
        collectionDateQualifier.setValue(
            "0" + collectionDateValue); // convert date format "D-MON-YYYY" to "DD-MON-YYYY"
        reportMessage(
            Severity.FIX,
            collectionDateQualifier.getOrigin(),
            CollectionDateQualifierFix_ID_1,
            collectionDateValue,
            collectionDateQualifier.getValue());
      }

      if (getEmblEntryValidationPlanProperty().validationScope.get() == ValidationScope.NCBI) {
        Matcher matcher = NCBI_DATE_FORMAT_PATTERN.matcher(collectionDateValue);
        if (matcher.matches()) {
          String monthYear = "-" + matcher.group(2) + "-20" + matcher.group(3);
          collectionDateQualifier.setValue(
              matcher.group(1).length() == 1
                  ? "0" + matcher.group(1) + monthYear
                  : matcher.group(1)
                      + monthYear); // convert date format "D-MON-YY" or "DD-MON-YY" to
          // "DD-MON-YYYY"
          reportMessage(
              Severity.FIX,
              collectionDateQualifier.getOrigin(),
              CollectionDateQualifierFix_ID_1,
              collectionDateValue,
              collectionDateQualifier.getValue());
        }
      }
    }
    return result;
  }
}
