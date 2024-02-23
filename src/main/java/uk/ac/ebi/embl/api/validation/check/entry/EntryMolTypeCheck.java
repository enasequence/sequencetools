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

import java.util.Arrays;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("The mol_type value \\\"{0}\\\" is not permitted.")
public class EntryMolTypeCheck extends EntryValidationCheck {

  private static final String WRONG_MOLTYPE_ID = "EntryMolTypeCheck";

  private List<String> molTypeValues;

  public EntryMolTypeCheck() {}

  public void init() {
    DataSet valuesSet = GlobalDataSets.getDataSet(GlobalDataSetFile.FEATURE_REGEX_GROUPS);

    if (valuesSet != null) {
      for (DataRow regexpRow : valuesSet.getRows()) {
        if (regexpRow.getString(0).equals("mol_type")) {
          molTypeValues = Arrays.asList(regexpRow.getStringArray(3));
        }
      }
    } else {
      throw new IllegalArgumentException("Failed to set qualifier values in EntryMolTypeCheck!");
    }
  }

  public ValidationResult check(Entry entry) {

    init();
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    Sequence sequence = entry.getSequence();
    if (sequence == null || entry.getSequence().getLength() == 0) {
      return result; // dont make a fuss - other checks for this
    }

    if (sequence.getMoleculeType() != null && !molTypeValues.contains(sequence.getMoleculeType())) {
      ValidationMessage<Origin> message =
          reportError(entry.getOrigin(), WRONG_MOLTYPE_ID, sequence.getMoleculeType());

      String permittedValues = Utils.paramArrayToCuratorTipString(molTypeValues.toArray());
      message.setCuratorMessage("Permitted values are : " + permittedValues);
    }

    return result;
  }
}
