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
package uk.ac.ebi.embl.api.validation.check.feature;

import java.util.Collection;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@ExcludeScope(
    validationScope = {
      ValidationScope.EMBL_TEMPLATE,
      ValidationScope.NCBI,
      ValidationScope.NCBI_MASTER
    }) // do not run in template mode - no control over what they type so could run into problems
@Description("Qualifier {0} must exist when qualifier {1} value matches the pattern {2}.")
public class QualifierPatternAndQualifierCheck extends FeatureValidationCheck {

  private static final String MESSAGE_ID = "QualifierPatternAndQualifierCheck";

  public QualifierPatternAndQualifierCheck() {}

  public ValidationResult check(Feature feature) {
    DataSet dataSet = GlobalDataSets.getDataSet(GlobalDataSetFile.QUALIFIER_PATTERN_QUALIFIER);
    result = new ValidationResult();

    if (feature == null) {
      return result;
    }

    for (DataRow dataRow : dataSet.getRows()) {
      String requiredQualifierName = dataRow.getString(0);
      String qualifierName = dataRow.getString(1);
      String qualifierValuPattern = dataRow.getString(2);

      if (qualifierName == null || qualifierValuPattern == null || requiredQualifierName == null) {
        return result;
      }

      Collection<Qualifier> qualifiers = feature.getQualifiers(qualifierName);
      Pattern pattern = Pattern.compile(qualifierValuPattern);
      for (Qualifier qualifier : qualifiers) {
        String value = qualifier.getValue();
        if (value == null) {
          continue;
        }

        if (pattern.matcher(value).matches()
            && !SequenceEntryUtils.isQualifierAvailable(requiredQualifierName, feature)) {
          reportError(
              feature.getOrigin(),
              MESSAGE_ID,
              requiredQualifierName,
              qualifierName,
              qualifierValuPattern);
        }
      }
    }

    return result;
  }
}
