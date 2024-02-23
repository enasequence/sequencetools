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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description(
    "Any of Qualifiers \"{0}\" must exist in Source feature if Molecule Type matches the Value \"{1}\""
        + "mol_type must have value \"{0}\" when qualifier \"{1}\" exists")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class MoleculeTypeAndSourceQualifierCheck extends EntryValidationCheck {

  private static final String MESSAGE_ID = "MoleculeTypeAndSourceQualifierCheck";
  private static final String MESSAGE_ID_1 = "MoleculeTypeAndSourceQualifierCheck_1";

  public MoleculeTypeAndSourceQualifierCheck() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();
    if (entry == null) {
      return result;
    }

    DataSet dataSet = GlobalDataSets.getDataSet(GlobalDataSetFile.SOURCE_QUALIFIERS_MOLTYPE_VALUES);
    for (DataRow dataRow :
        GlobalDataSets.getDataSet(GlobalDataSetFile.MOLTYPE_SOURCE_QUALIFIERS).getRows()) {

      boolean status = false;

      String[] requiredQualifiers = dataRow.getStringArray(0);
      String moleculeType = dataRow.getString(1);

      if (moleculeType == null || ArrayUtils.isEmpty(requiredQualifiers)) {
        continue;
      }
      String molType = SequenceEntryUtils.getMoleculeType(entry);
      if (moleculeType.equals(molType)) {
        Collection<Feature> sources =
            SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
        if (sources.isEmpty()) {
          continue; // just get out - other checks for source features
        }
        for (Feature source : sources) {
          Collection<Qualifier> sourceQualifiers = source.getQualifiers();
          if (sourceQualifiers.isEmpty()) {
            reportError(entry, requiredQualifiers, moleculeType);
          }
          for (String requiredQualifier : requiredQualifiers) {
            if (SequenceEntryUtils.isQualifierAvailable(requiredQualifier, source)) {
              status = true;
              break;
            }
          }
          if (!status) {
            reportError(entry, requiredQualifiers, moleculeType);
          }
        }
      }

      // "mol_type must have value \"{0}\" when qualifier \"{1}\" exists"
      List<Qualifier> sourceQualifiers = SequenceEntryUtils.getSourceQualifiers(entry);
      List<String> sourceQualifierNames = new ArrayList<String>();
      if (sourceQualifiers == null || sourceQualifiers.size() == 0) {
        continue;
      }

      for (Qualifier sourceQualifier : sourceQualifiers) {
        sourceQualifierNames.add(sourceQualifier.getName());
      }

      for (DataRow row : dataSet.getRows()) {
        String requiredmolType_value = row.getString(0);
        String sourceQualifier = row.getString(1);
        if (sourceQualifierNames.contains(sourceQualifier)) {
          if (requiredmolType_value != null && !requiredmolType_value.equals(molType)) {
            reportError(entry.getOrigin(), MESSAGE_ID_1, requiredmolType_value, sourceQualifier);
          }
        }
      }
    }
    return result;
  }

  private void reportError(Entry entry, String[] requiredQualifiers, String moleculeType) {
    reportError(
        entry.getOrigin(), MESSAGE_ID, Utils.paramArrayToString(requiredQualifiers), moleculeType);
  }
}
