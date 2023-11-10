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

import java.util.List;
import uk.ac.ebi.embl.api.contant.Constant;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("Journal has been modified from:{0} to:{1}")
@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_CHROMOSOME,
      ValidationScope.ASSEMBLY_CONTIG,
      ValidationScope.ASSEMBLY_MASTER,
      ValidationScope.ASSEMBLY_SCAFFOLD,
      ValidationScope.ASSEMBLY_TRANSCRIPTOME,
      ValidationScope.EMBL,
      ValidationScope.EMBL,
      ValidationScope.EMBL_TEMPLATE,
      ValidationScope.EPO,
      ValidationScope.EPO_PEPTIDE,
      ValidationScope.NCBI_MASTER
    })
public class JournalFix extends EntryValidationCheck {

  private static final String FIX_ID = "JournalFix_1";

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    List<Reference> referenceList = entry.getReferences();
    if (referenceList != null && !referenceList.isEmpty()) {
      referenceList.forEach(
          ref -> {
            Publication publication = ref.getPublication();
            if (publication != null && publication instanceof Unpublished) {
              String jb = ((Unpublished) publication).getJournalBlock();
              if (jb != null && !jb.equalsIgnoreCase(Constant.JOURNAL_UNPUBLISHED_VALUE)) {
                reportMessage(
                    Severity.FIX,
                    entry.getOrigin(),
                    FIX_ID,
                    jb,
                    Constant.JOURNAL_UNPUBLISHED_VALUE);
              }
            }
          });
    }

    return result;
  }
}
