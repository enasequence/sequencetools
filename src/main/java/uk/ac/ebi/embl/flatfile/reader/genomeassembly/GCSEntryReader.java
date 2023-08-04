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
package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import java.io.File;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.FlatFileReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

public abstract class GCSEntryReader implements FlatFileReader<Object> {
  ValidationResult validationResult = new ValidationResult();
  File file = null;

  public GCSEntryReader() {
    ValidationMessageManager.addBundle(FlatFileValidations.GENOMEASSEMBLY_FLAT_FILE_BUNDLE);
  }

  protected void error(int lineNumber, String messageKey, Object... params) {
    validationResult.append(
        FlatFileValidations.message(lineNumber, Severity.ERROR, messageKey, params));
  }

  protected void warning(int lineNumber, String messageKey, Object... params) {
    validationResult.append(
        FlatFileValidations.message(lineNumber, Severity.WARNING, messageKey, params));
  }

  protected void fix(int lineNumber, String messageKey, Object... params) {
    validationResult.append(
        FlatFileValidations.message(lineNumber, Severity.FIX, messageKey, params));
  }
}
