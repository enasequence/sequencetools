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

import java.util.List;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description(
    "Feature \\\"{0}\\\" is required when qualifier \\\"{1}\\\" matches pattern \\\"{2}\\\"")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class QualifierPatternAndFeatureCheck extends EntryValidationCheck {

  private static final String MESSAGE_ID = "QualifierPatternAndFeatureCheck_1";

  public QualifierPatternAndFeatureCheck() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();
    DataSet dataSet = GlobalDataSets.getDataSet(GlobalDataSetFile.QUALIFIER_PATTERN_FEATURE);
    if (entry == null) return result;

    List<Feature> features = entry.getFeatures();

    if (features != null && features.size() == 0) {
      return result;
    }

    for (DataRow dataRow : dataSet.getRows()) {

      String featureName = dataRow.getString(0);
      String qualifierName = dataRow.getString(1);
      String valuePattern = dataRow.getString(2);

      if (StringUtils.isEmpty(qualifierName)
          || StringUtils.isEmpty(valuePattern)
          || StringUtils.isEmpty(featureName)) {
        continue;
      }

      if (SequenceEntryUtils.isQualifierwithPatternAvailable(qualifierName, valuePattern, entry)
          && !SequenceEntryUtils.isFeatureAvailable(featureName, entry)) {
        reportError(entry.getOrigin(), MESSAGE_ID, featureName, qualifierName, valuePattern);
      }
    }

    return result;
  }
}
