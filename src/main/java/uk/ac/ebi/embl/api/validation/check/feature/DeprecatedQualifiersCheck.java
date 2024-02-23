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
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description(
    "The {0} qualifier is deprecated. The {0} qualifier is deprecated, please replace with {1}."
        + "The {0} qualifier is deprecated and is replaced by {1} - this change will be made automatically during curation of your entry.")
public class DeprecatedQualifiersCheck extends FeatureValidationCheck {

  private static final String DEPRECATED_ID = "DeprecatedQualifiersCheck";
  private static final String REPLACEMENT_ID = "DeprecatedQualifiersCheck-2";
  private static final String AUTOFIX_ID = "DeprecatedQualifiersCheck-3";

  public DeprecatedQualifiersCheck() {}

  public ValidationResult check(Feature feature) {
    result = new ValidationResult();
    DataSet dataSet = GlobalDataSets.getDataSet(GlobalDataSetFile.DEPRECATED_QUALIFIERS);

    if (feature == null) {
      return result;
    }

    for (DataRow dataRow : dataSet.getRows()) {
      String qualifierName = Utils.parseTSVString(dataRow.getString(0));
      String replacementName = Utils.parseTSVString(dataRow.getString(1));
      boolean autoReplace = dataRow.getString(2).equals("Y");

      if (qualifierName == null) {
        return result;
      }

      if (SequenceEntryUtils.isQualifierAvailable(qualifierName, feature)) {
        if (replacementName == null) {
          reportError(feature.getOrigin(), DEPRECATED_ID, qualifierName);
        } else if (autoReplace) {
          reportWarning(feature.getOrigin(), AUTOFIX_ID, qualifierName, replacementName);
        } else {
          reportError(feature.getOrigin(), REPLACEMENT_ID, qualifierName, replacementName);
        }
      }
    }
    return result;
  }
}
