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

import java.util.concurrent.atomic.AtomicInteger;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

public class SubmitterAccessionCheck extends EntryValidationCheck {
  private static final String SUBMITTER_ACCESSION_MISSING_MESSAGE_ID = "SubmitterAccessionCheck_1";
  private static final String SUBMITTER_ACCESSION_TOO_LONG_MESSAGE_ID = "SubmitterAccessionCheck_2";
  private static final int TRUNCATED_ACCESSION_LENGTH = 30;
  private static final int SUBMITTER_ACCESSION_MAX_LENGTH = 50;
  private static final AtomicInteger uniqueAccessionIndex = new AtomicInteger(0);

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();
    if (entry == null) {
      return result;
    }
    SubmissionOptions options = getEmblEntryValidationPlanProperty().getOptions();
    ValidationScope validationScope = getEmblEntryValidationPlanProperty().validationScope.get();
    AssemblyType assemblyType = options.assemblyType;
    if (validationScope == ValidationScope.ASSEMBLY_CHROMOSOME
        || validationScope == ValidationScope.ASSEMBLY_SCAFFOLD
        || validationScope == ValidationScope.ASSEMBLY_CONTIG
        || validationScope == ValidationScope.ASSEMBLY_TRANSCRIPTOME) {

      String submitterAccession = entry.getSubmitterAccession();

      if (submitterAccession == null || submitterAccession.isEmpty()) {
        reportError(entry.getOrigin(), SUBMITTER_ACCESSION_MISSING_MESSAGE_ID);
      } else if (submitterAccession.length() > SUBMITTER_ACCESSION_MAX_LENGTH
          && !EntryUtils.excludeDistribution(assemblyType.getValue())) {
        // Handle assemblies that are distributed.
        if (options.isWebinCLI && showError(assemblyType)) {
          // throw error if WebinCli and showError
          reportError(
              entry.getOrigin(),
              SUBMITTER_ACCESSION_TOO_LONG_MESSAGE_ID,
              entry.getSubmitterAccession());
        } else if (!options.isWebinCLI) {
          // Truncate SubmitterAccession if context is NOT WebinCli.
          String truncatedAccession = truncateAndAddUniqueString(submitterAccession);
          entry.setSubmitterAccession(truncatedAccession);
        }
      }
    }
    return result;
  }

  // Show error only for assemblies that are not BINNEDMETAGENOME, PRIMARYMETAGENOME and
  // CLINICALISOLATEASSEMBLY
  private boolean showError(AssemblyType assemblyType) {
    return !isIgnoreError() && !EntryUtils.excludeDistribution(assemblyType.getValue());
  }

  /**
   * Truncates the given string to 30 characters and adds a unique string of 5 characters
   *
   * @param submitterAccession
   * @return
   */
  public String truncateAndAddUniqueString(String submitterAccession) {
    return submitterAccession.substring(0, TRUNCATED_ACCESSION_LENGTH)
        + "-"
        + uniqueAccessionIndex.incrementAndGet();
  }
}
