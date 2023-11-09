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
package uk.ac.ebi.embl.api.validation.check.file;

import java.util.HashMap;
import java.util.List;
import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.plan.GenomeAssemblyValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.ChromosomeListFileReader;

@Description("")
public class ChromosomeListFileValidationCheck extends FileValidationCheck {

  public ChromosomeListFileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
    super(options, sharedInfo);
  }

  @Override
  public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException {
    ValidationResult validationResult = new ValidationResult();
    Origin origin = null;
    try {
      clearReportFile(getReportFile(submissionFile));

      if (!validateFileFormat(
          submissionFile.getFile(),
          uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.CHROMOSOME_LIST)) {
        addErrorAndReport(validationResult, submissionFile, "InvalidFileFormat", "chromosome_list");
        return validationResult;
      }
      ChromosomeListFileReader reader = new ChromosomeListFileReader(submissionFile.getFile());

      validationResult.append(reader.read());

      if (!validationResult.isValid()) {
        if (getOptions().reportDir.isPresent())
          getReporter().writeToFile(getReportFile(submissionFile), validationResult);
        addMessageStats(validationResult.getMessages());
      }
      getOptions().getEntryValidationPlanProperty().fileType.set(FileType.CHROMOSOMELIST);
      GenomeAssemblyValidationPlan plan =
          new GenomeAssemblyValidationPlan(getOptions().getEntryValidationPlanProperty());

      List<ChromosomeEntry> chromosomeEntries = reader.getentries();
      for (ChromosomeEntry entry : chromosomeEntries) {
        origin = entry.getOrigin();
        ValidationResult planResult = plan.execute(entry);
        validationResult.append(planResult);
        if (!planResult.isValid()) {
          getReporter().writeToFile(getReportFile(submissionFile), planResult);
          addMessageStats(planResult.getMessages());
        }
        if (entry.getObjectName() != null)
          sharedInfo.chromosomeNameQualifiers.put(entry.getObjectName().toUpperCase(), entry);
        if (entry.getChromosomeName() != null)
          sharedInfo.chromosomeNames.add(entry.getChromosomeName().toUpperCase());
      }

    } catch (ValidationEngineException e) {
      getReporter()
          .writeToFile(getReportFile(submissionFile), Severity.ERROR, e.getMessage(), origin);
      throw e;
    } catch (Exception e) {
      getReporter()
          .writeToFile(getReportFile(submissionFile), Severity.ERROR, e.getMessage(), origin);
      throw new ValidationEngineException(e.getMessage(), e);
    }
    return validationResult;
  }

  public HashMap<String, ChromosomeEntry> getChromosomeQualifiers() {
    return sharedInfo.chromosomeNameQualifiers;
  }

  @Override
  public ValidationResult check() {
    throw new UnsupportedOperationException();
  }
}
