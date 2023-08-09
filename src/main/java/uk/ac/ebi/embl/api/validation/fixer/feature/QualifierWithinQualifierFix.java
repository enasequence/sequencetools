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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.GlobalDataSetFile;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

public class QualifierWithinQualifierFix extends FeatureValidationCheck {

  private static final String QUALIFIER_REGEX =
      "^(.)*(\\s)((/\\w+)(\\s)?(=(\\s)?[^\\s]+)?(=(\\s)?\\'(.+)\\')?)(\\s)?(.)*$";
  private static final String ADD_QUALIFIER_MESSAGE_ID = "QualifierWithinQualifierFix_1";
  private static final String EXCLUDE_QUALIFIER_MESSAGE_ID = "QualifierWithinQualifierFix_2";

  public QualifierWithinQualifierFix() {}

  public ValidationResult check(Feature feature) {
    DataSet qualifierSet = GlobalDataSets.getDataSet(GlobalDataSetFile.FEATURE_QUALIFIER_VALUES);
    result = new ValidationResult();

    if (feature == null) {
      return result;
    }

    ArrayList<String> validQualifiers = new ArrayList<String>();
    ArrayList<Qualifier> newQualifiers = new ArrayList<Qualifier>();
    QualifierFactory qualifierFactory = new QualifierFactory();
    for (DataRow dataRow : qualifierSet.getRows()) {
      validQualifiers.add(dataRow.getString(0));
    }

    for (Qualifier qualifier : feature.getQualifiers()) {
      String qualifierValue = qualifier.getValue();
      if (qualifierValue != null && qualifierValue.contains("/")) {
        Pattern p = Pattern.compile(QUALIFIER_REGEX);
        while (true) {
          Matcher m = p.matcher(qualifierValue);
          if (m.find()) {
            String[] qualifierKeyValues = m.group(3).split("=");
            String key = qualifierKeyValues[0].replaceAll("/", "");
            String newQualifier = containsCaseInsensitive(key, validQualifiers);
            if (newQualifier != null) {
              if (qualifierKeyValues.length == 1)
                newQualifiers.add(qualifierFactory.createQualifier(newQualifier));
              else
                newQualifiers.add(
                    qualifierFactory.createQualifier(
                        newQualifier, qualifierKeyValues[1].replaceAll("'", "").trim()));
              qualifierValue = qualifierValue.replaceAll(m.group(3), "");
              qualifier.setValue(qualifierValue.replaceAll("  ", " ").trim());
              reportMessage(
                  Severity.FIX,
                  feature.getOrigin(),
                  EXCLUDE_QUALIFIER_MESSAGE_ID,
                  newQualifier,
                  qualifier.getName());
            } else break;
          } else break;
        }
      }
    }

    for (Qualifier qualifier : newQualifiers) {
      feature.addQualifier(qualifier);
      reportMessage(
          Severity.FIX,
          feature.getOrigin(),
          ADD_QUALIFIER_MESSAGE_ID,
          qualifier.getName(),
          feature.getName());
    }

    return result;
  }

  public String containsCaseInsensitive(String strToCompare, ArrayList<String> list) {
    for (String str : list) {
      if (str.equalsIgnoreCase(strToCompare)) {
        return str;
      }
    }
    return null;
  }
}
