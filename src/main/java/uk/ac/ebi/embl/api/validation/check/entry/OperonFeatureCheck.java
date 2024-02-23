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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.*;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description(
    "Feature \"{0}\" refers to operon \"{1}\". Please provide an operon feature which spans the entire operon region. Refer to (http://www.ebi.ac.uk/ena/WebFeat/operon_s.html) for details"
        + "\"{0}\" number of features refer to operon \"{1}\". Please provide an operon feature which spans the entire operon region. Refer to (http://www.ebi.ac.uk/ena/WebFeat/operon_s.html) for details")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class OperonFeatureCheck extends EntryValidationCheck {

  protected static final String SINGLE_OPERON_MESSAGE = "OperonFeatureCheck_1";
  protected static final String MULTIPLE_OPERON_MESSAGE = "OperonFeatureCheck_2";

  public OperonFeatureCheck() {}

  public ValidationResult check(Entry entry) {

    result = new ValidationResult();

    if (entry == null) {
      return result;
    }
    if (entry.getFeatures().size() == 0) {
      return result;
    }

    List<Qualifier> operonQualifiers =
        SequenceEntryUtils.getQualifiers(Qualifier.OPERON_QUALIFIER_NAME, entry);

    if (operonQualifiers != null && operonQualifiers.size() == 0) {
      return result;
    }
    List<Feature> operonFeatures =
        SequenceEntryUtils.getFeatures(Feature.OPERON_FEATURE_NAME, entry);

    if (operonFeatures.size() >= 1) {
      return result;
    }

    Collection<Feature> featuresWithOperon =
        SequenceEntryUtils.getFeaturesContainingQualifier(Qualifier.OPERON_QUALIFIER_NAME, entry);

    if (featuresWithOperon != null) {
      if (featuresWithOperon.size() == 1) {
        reportError(
            entry.getOrigin(),
            SINGLE_OPERON_MESSAGE,
            ((Feature) featuresWithOperon.toArray()[0]).getName(),
            ((Feature) featuresWithOperon.toArray()[0])
                .getSingleQualifierValue(Qualifier.OPERON_QUALIFIER_NAME));
      } else {
        HashMap<String, Integer> operonCountMap = new HashMap<String, Integer>();
        for (Feature feature1 : featuresWithOperon) {
          String operonValue = feature1.getSingleQualifierValue(Qualifier.OPERON_QUALIFIER_NAME);
          int operonCount = 0;
          if (operonCountMap.containsKey(operonValue)) continue;
          for (Feature feature2 : featuresWithOperon) {
            if (operonValue != null
                && operonValue.equals(
                    feature2.getSingleQualifierValue(Qualifier.OPERON_QUALIFIER_NAME))) {
              operonCount++;
            }
          }
          if (operonCount == 0) continue;
          if (operonCount == 1) {
            reportError(entry.getOrigin(), SINGLE_OPERON_MESSAGE, feature1.getName(), operonValue);
          } else {
            operonCountMap.put(operonValue, operonCount);
          }
        }

        for (String operonValue : operonCountMap.keySet()) {
          reportError(
              entry.getOrigin(),
              MULTIPLE_OPERON_MESSAGE,
              operonCountMap.get(operonValue),
              operonValue);
        }
      }
    }

    return result;
  }
}
