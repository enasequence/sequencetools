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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.embl.api.AccessionMatcher;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("Entryname has been fixed from \"{0}\" to \"{1}\"")
public class AccessionFix extends EntryValidationCheck {

  private static final String REMOVE_MASTER_ACCESSION_FIX = "MaterAccessionRemovalFix";

  public AccessionFix() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();
    if (entry == null) return result;
    if (entry.getSecondaryAccessions() != null) {
      List<Text> masterAccnsToRemove = new ArrayList<>();
      for (Text accn : entry.getSecondaryAccessions()) {
        if (AccessionMatcher.isMasterAccession(accn.getText())) {
          masterAccnsToRemove.add(accn);
        }
      }
      if (!masterAccnsToRemove.isEmpty()) {
        entry.getSecondaryAccessions().removeAll(masterAccnsToRemove);
        reportMessage(
            Severity.FIX,
            entry.getOrigin(),
            REMOVE_MASTER_ACCESSION_FIX,
            masterAccnsToRemove,
            entry.getSubmitterAccession());
      }
    }

    return result;
  }
}
