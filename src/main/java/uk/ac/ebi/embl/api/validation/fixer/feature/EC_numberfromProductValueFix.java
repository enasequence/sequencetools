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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description(
    "Added \"EC_number\" qualifier from \"Product\" Value {0}"
        + "\"product\" qualifier value {0} has been changed to {1}")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class EC_numberfromProductValueFix extends FeatureValidationCheck {

  private static final String PRODUCT_QUALIFIER_VALUE_CHANGED = "EC_numberfromProductValueFix_1";
  private static final String ADDED_ECNUMBER_QUAL_DERIVED_FROM_PRODUCT =
      "EC_numberfromProductValueFix_2";
  private static final String REMOVED_ECNUMBER_QUAL_FROM_FEATURE = "EC_numberfromProductValueFix_3";
  private static final String REMOVED_ECNUMBER_VALUE_FROM_PRODUCT =
      "EC_numberfromProductValueFix_4";

  private static final String ecRegex =
      "[\\d-]{1,3}\\.(?:(\\d{0,3}\\-{0,1}\\.)){2}n{0,1}\\d{0,3}\\-{0,1}";
  private static final Pattern ecPattern = Pattern.compile(ecRegex);
  private static final String productEcNumberSplitRegex =
      "\\[?\\(?(?:EC|ec)?:?=?(?:(?:\\d{0,3}\\-{0,1}\\.){3}n{0,1}\\d{0,}\\-{0,1})(?:\\s*,\\s*"
          + "(?:EC|ec)?:?=?)?(?:(?:\\d{0,3}\\-{0,1}\\.){3}n{0,1}\\d{0,}\\-{0,1}){0,}(?:\\]?\\)?)";
  private static final Pattern productEcNumberSplitPattern =
      Pattern.compile(productEcNumberSplitRegex);

  public ValidationResult check(Feature feature) {
    result = new ValidationResult();

    if (feature == null) {
      return result;
    }
    List<Qualifier> productQualifiers = feature.getQualifiers(Qualifier.PRODUCT_QUALIFIER_NAME);
    for (Qualifier productQualifier : productQualifiers) {
      String productValue = productQualifier.getValue();
      boolean ecNumbersAllowed = true;

      if (productValue.toLowerCase().contains("hypothetical protein")
          || productValue.toLowerCase().contains("unknown")) {
        ecNumbersAllowed = false;
        ArrayList<Qualifier> ecNumberQualifiers =
            (ArrayList<Qualifier>) feature.getQualifiers(Qualifier.EC_NUMBER_QUALIFIER_NAME);
        for (Qualifier ecQualifier : ecNumberQualifiers) {
          feature.removeQualifier(ecQualifier);
          reportMessage(
              Severity.FIX,
              feature.getOrigin(),
              REMOVED_ECNUMBER_QUAL_FROM_FEATURE,
              feature.getName());
        }
      }

      ImmutablePair<String, List<String>> productAndEcNumber = getEcNumberAndProduct(productValue);
      // if derived product and existing product value are same, no ec_numbers found
      if (productAndEcNumber.left.equals(productValue)) {
        continue;
      }

      productQualifier.setValue(productAndEcNumber.left);
      if (!ecNumbersAllowed) {
        reportMessage(Severity.FIX, feature.getOrigin(), REMOVED_ECNUMBER_VALUE_FROM_PRODUCT);
      }
      reportMessage(
          Severity.FIX,
          feature.getOrigin(),
          PRODUCT_QUALIFIER_VALUE_CHANGED,
          productValue,
          productAndEcNumber.left);

      for (String ecNumber : productAndEcNumber.right) {
        feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, ecNumber);
        if (!SequenceEntryUtils.deleteDuplicatedQualfiier(
            feature, Qualifier.EC_NUMBER_QUALIFIER_NAME))
          reportMessage(
              Severity.FIX,
              feature.getOrigin(),
              ADDED_ECNUMBER_QUAL_DERIVED_FROM_PRODUCT,
              ecNumber,
              productValue);
      }
    }
    return result;
  }

  ImmutablePair<String, List<String>> getEcNumberAndProduct(String product) {
    String originalProduct = product;
    List<String> ecNumbersL = new ArrayList<>();
    Matcher matcher = productEcNumberSplitPattern.matcher(product);
    String matched;
    while (matcher.find()) {
      matched = matcher.group();
      product = product.replaceAll(productEcNumberSplitRegex, "").trim();

      if (matched != null) {
        String[] ecs = matched.split(",");
        for (String ec : ecs) {
          Matcher ecMatcher = ecPattern.matcher(ec);

          if (ecMatcher.find()) {
            String grp = ecMatcher.group();
            ecNumbersL.add(grp);
          }
        }
      }
    }

    if (ecNumbersL.isEmpty()) {
      return new ImmutablePair<>(originalProduct, ecNumbersL);
    }
    return new ImmutablePair<>(product, ecNumbersL);
  }
}
