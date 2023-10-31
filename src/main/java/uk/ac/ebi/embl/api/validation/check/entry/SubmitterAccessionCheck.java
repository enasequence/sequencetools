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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;

public class SubmitterAccessionCheck extends EntryValidationCheck {
  private static final String SUBMITTER_ACCESSION_MISSING_MESSAGE_ID = "SubmitterAccessionCheck_1";
  private static final String SUBMITTER_ACCESSION_TOO_LONG_MESSAGE_ID = "SubmitterAccessionCheck_2";
  private static final int SUBMITTER_ACCESSION_MAX_LENGTH = 50;

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();
    if (entry == null) {
      return result;
    }
    ValidationScope validationScope = getEmblEntryValidationPlanProperty().validationScope.get();
    if (validationScope == ValidationScope.ASSEMBLY_CHROMOSOME
        || validationScope == ValidationScope.ASSEMBLY_SCAFFOLD
        || validationScope == ValidationScope.ASSEMBLY_CONTIG
        || validationScope == ValidationScope.ASSEMBLY_TRANSCRIPTOME) {

      String submitterAccession = entry.getSubmitterAccession();

      if (submitterAccession == null || submitterAccession.isEmpty()) {
        reportError(entry.getOrigin(), SUBMITTER_ACCESSION_MISSING_MESSAGE_ID);
      } else if (submitterAccession.length() > SUBMITTER_ACCESSION_MAX_LENGTH
          && getEmblEntryValidationPlanProperty().options.ignoreError.isPresent()
          && !getEmblEntryValidationPlanProperty().options.ignoreError.get()) {
        reportError(
            entry.getOrigin(),
            SUBMITTER_ACCESSION_TOO_LONG_MESSAGE_ID,
            entry.getSubmitterAccession());
      }
    }
    return result;
  }
}
