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
package uk.ac.ebi.embl.api.validation.check.sequence;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.EntryValidations;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("The entry has no sequence.")
@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class SequenceExistsCheck extends EntryValidationCheck {

  private static final String MESSAGE_ID = "SequenceExistsCheck";
  private static final String MESSAGE_ID_1 = "SequenceExistsCheck_1";

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    if (entry.getDataClass() != null && entry.getDataClass().equals(Entry.CON_DATACLASS)) {
      return result; // as cons do not have sequences ,if there is no database connection given
    }

    if (entry.getSequence() == null
        || entry.getSequence().getSequenceByte() == null
        || entry.getSequence().getLength() == 0) {
      result.append(EntryValidations.createMessage(entry.getOrigin(), Severity.ERROR, MESSAGE_ID));
    }
    return result;
  }
}
