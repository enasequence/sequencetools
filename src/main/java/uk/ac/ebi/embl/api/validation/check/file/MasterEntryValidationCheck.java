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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

@Description("")
public class MasterEntryValidationCheck extends FileValidationCheck {

  public MasterEntryValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
    super(options, sharedInfo);
  }

  @Override
  public ValidationResult check() throws ValidationEngineException {
    ValidationResult validationResult = new ValidationResult();

    if (getOptions().getEntryValidationPlanProperty() != null
        && getOptions().getEntryValidationPlanProperty().validationScope.get()
            != ValidationScope.NCBI_MASTER) {
      getOptions()
          .getEntryValidationPlanProperty()
          .validationScope
          .set(ValidationScope.ASSEMBLY_MASTER);
    }
    getOptions().getEntryValidationPlanProperty().fileType.set(FileType.MASTER);

    sharedInfo.masterEntry =
        SequenceToolsServices.masterEntryService()
            .createMasterEntry(getOptions(), validationResult);

    if (!validationResult.isValid()) {
      getReporter()
          .writeToFile(Paths.get(getOptions().reportDir.get(), "MASTER.report"), validationResult);
      addMessageStats(validationResult.getMessages());
    } else {
      if (!getOptions().isWebinCLI) {
        try {
          new EmblEntryWriter(sharedInfo.masterEntry)
              .write(
                  new PrintWriter(getOptions().processDir.get() + File.separator + masterFileName));
        } catch (IOException ex) {
          throw new ValidationEngineException(ex);
        }
      }
    }

    return validationResult;
  }

  @Override
  public ValidationResult check(SubmissionFile file) throws ValidationEngineException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
