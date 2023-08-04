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

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.GlobalDataSetFile;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description(
    "Qualifier \"{0}\" Value has been changed from \"{1}\" to \"{2}\""
        + "Deleted Qualifiers from feature \"{0}\" having value \"DELETED\"")
public class QualifierValueFix extends FeatureValidationCheck {

  private static final String QualifierValueFix_ID_1 = "QualifierValueFix_1";
  private static final String QualifierValueFix_ID_3 = "QualifierValueFix_3";

  public QualifierValueFix() {}

  public ValidationResult check(Feature feature) {
    DataSet qualifierValuetoFixValue =
        GlobalDataSets.getDataSet(GlobalDataSetFile.QUALIFIER_VALUE_TO_FIX_VALUE);
    try {
      result = new ValidationResult();
      if (feature == null) {
        return result;
      }
      for (Qualifier qual : feature.getQualifiers()) {
        String qName = qual.getName();
        String qValue = qual.getValue();
        if (qValue != null && qValue.contains("\"")) {
          qValue = qValue.replaceAll("\"", "'");
          qual.setValue(qValue);
          reportMessage(Severity.FIX, feature.getOrigin(), QualifierValueFix_ID_3, qName);
        }
        if (qName.equals(Qualifier.ALTITUDE_QUALIFIER_NAME) && qValue != null) {
          if (qValue.endsWith("m.")) {
            qual.setValue(qValue.substring(0, qValue.length() - 1));
          }
          if (qValue.contains(",")) {
            qual.setValue(qual.getValue().replaceAll(",", ""));
          }

          if (!qValue.equals(qual.getValue())) {
            reportMessage(
                Severity.FIX,
                feature.getOrigin(),
                QualifierValueFix_ID_1,
                qName,
                qValue,
                qual.getValue());
          }
        }

        for (DataRow dataRow : qualifierValuetoFixValue.getRows()) {
          String qualifier = dataRow.getString(0);
          String regex = dataRow.getString(1);
          String fixValue = dataRow.getString(2);

          if (qualifier.equalsIgnoreCase(qName) && regex.equalsIgnoreCase(qValue)) {
            qual.setValue(fixValue);
            reportMessage(
                Severity.FIX, qual.getOrigin(), QualifierValueFix_ID_1, qName, qValue, fixValue);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }
}
