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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")
public class AssemblyInfoNameCheck extends GenomeAssemblyValidationCheck<AssemblyInfoEntry> {
  private final String MESSAGE_KEY_MISSING_NAME_ERROR = "AssemblyInfoMissingNameCheck";
  private final String MESSAGE_KEY_INVALID_ASEMBLY_NAME_ERROR = "AssemblyInfoInvalidAssemblyName";
  private final String MESSAGE_KEY_INVALID_ASEMBLY_NAME_LENGTH_ERROR =
      "AssemblyInfoInvalidAssemblyNameLength";
  private final String assemblyNameRegex = "^[A-Za-z0-9 _\\-\\.#]*$";
  private final Pattern assemblyNamePattern = Pattern.compile(assemblyNameRegex);

  @Override
  public ValidationResult check(AssemblyInfoEntry entry) {

    if (entry == null) return result;
    if (entry.getName() == null || entry.getName().isEmpty()) {
      reportError(entry.getOrigin(), MESSAGE_KEY_MISSING_NAME_ERROR);
      return result;
    }
    if (!assemblyNamePattern.matcher(entry.getName()).matches())
      reportError(
          entry.getOrigin(),
          MESSAGE_KEY_INVALID_ASEMBLY_NAME_ERROR,
          entry.getName(),
          assemblyNameRegex);

    if (entry.getName().length() >= 100)
      reportError(
          entry.getOrigin(), MESSAGE_KEY_INVALID_ASEMBLY_NAME_LENGTH_ERROR, entry.getName());
    return result;
  }

  public boolean isValidName(String name) {
    return name != null && !name.trim().isEmpty() && assemblyNamePattern.matcher(name).matches();
  }
}
