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
package uk.ac.ebi.embl.api.validation.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import uk.ac.ebi.embl.api.validation.*;

public interface SubmissionReporter {
  void writeToFile(Path reportFile, ValidationResult validationResult, String targetOrigin);

  void writeToFile(Path reportFile, ValidationResult validationResult);

  void writeToFile(Path reportFile, ValidationMessage validationMessage);

  void writeToFile(Path reportFile, Severity severity, String message, Origin origin);

  void writeToFile(Path reportFile, Severity severity, String message);

  void writeToFile(Path reportFile, ConcurrentMap<String, AtomicLong> messageStats)
      throws IOException;

  void writeToFile(File reportFile, ValidationResult validationResult, String targetOrigin);

  void writeToFile(File reportFile, ValidationResult validationResult);

  void writeToFile(File reportFile, ValidationMessage validationMessage);

  void writeToFile(File reportFile, Severity severity, String message, Origin origin);

  void writeToFile(File reportFile, Severity severity, String message);
}
