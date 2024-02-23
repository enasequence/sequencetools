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

import java.util.Collection;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description(
    "Qualifier \"{0}\" must have value which matches the pattern \"{1}\" when qualifier \"{2}\" has value \"{3}\".")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class NcRNAQualifierValueAndQualifierPatternCheck extends FeatureValidationCheck {

  private static final String MESSAGE_ID = "NcRNAQualifierValueAndQualifierPatternCheck";

  public NcRNAQualifierValueAndQualifierPatternCheck() {}

  public ValidationResult check(Feature feature) {
    DataSet dataSet =
        GlobalDataSets.getDataSet(GlobalDataSetFile.NCRNA_QUALIFIER_VAL_QUALIFIER_PATTERN);
    result = new ValidationResult();

    if (feature == null) {
      return result;
    }

    if (!"ncRNA".equals(feature.getName())) {
      return result;
    }

    for (DataRow dataRow : dataSet.getRows()) {
      String qualifierName1 = dataRow.getString(0);
      String qualifierPattern1 = dataRow.getString(1);
      String qualifierName2 = dataRow.getString(2);
      String qualifierValue2 = dataRow.getString(3);

      if (qualifierName2 == null || qualifierName1 == null || qualifierPattern1 == null) {
        return result;
      }

      if (!SequenceEntryUtils.isQualifierWithValueAvailable(
          qualifierName2, qualifierValue2, feature)) {
        return result;
      }

      Collection<Qualifier> qualifiers2 = feature.getQualifiers(qualifierName1);
      if (qualifiers2.isEmpty()) {
        return result;
      }

      Pattern pattern = Pattern.compile(qualifierPattern1);
      for (Qualifier qualifier2 : qualifiers2) {
        String value2 = qualifier2.getValue();
        if (value2 == null || !pattern.matcher(value2).matches()) {
          reportError(
              feature.getOrigin(),
              MESSAGE_ID,
              qualifierName1,
              qualifierPattern1,
              qualifierName2,
              qualifierValue2);
        }
      }
    }

    return result;
  }
}
