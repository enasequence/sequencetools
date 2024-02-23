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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("scaffolded TSA : mol_type has been changed from \"mRNA\" to \"transcribed RNA\"")
public class Mol_typeFix extends EntryValidationCheck {

  private static final String FIX_ID = "Mol_typeFix_1";

  public Mol_typeFix() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();
    if (entry == null) {
      return result;
    }

    Sequence sequence = entry.getSequence();
    if (sequence == null) {
      return result;
    }

    if (sequence.getMoleculeType() == null) {
      return result;
    }
    if (entry.getFeatures().size() == 0) {
      return result;
    }

    if (SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry).size() == 0
        || entry.getDataClass() == null) {
      return result;
    } else if (entry.getDataClass().equals(Entry.TSA_DATACLASS)
        && sequence.getMoleculeType().equals("mRNA")) {
      entry.getSequence().setMoleculeType("transcribed RNA");
      reportMessage(Severity.FIX, entry.getOrigin(), FIX_ID);
    }
    return result;
  }
}
