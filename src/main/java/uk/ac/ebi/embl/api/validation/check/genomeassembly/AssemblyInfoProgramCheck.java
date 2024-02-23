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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;

@Description("")
@GroupIncludeScope(group = {ValidationScope.Group.ASSEMBLY})
@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_CONTIG,
      ValidationScope.ASSEMBLY_SCAFFOLD,
      ValidationScope.ASSEMBLY_CHROMOSOME,
      ValidationScope.ASSEMBLY_MASTER,
      ValidationScope.NCBI_MASTER
    })
public class AssemblyInfoProgramCheck extends GenomeAssemblyValidationCheck<AssemblyInfoEntry> {

  private final String MESSAGE_KEY_PROGRAM_ERROR = "AssemblyInfoProgramMissingCheck";

  @Override
  public ValidationResult check(AssemblyInfoEntry entry) throws ValidationEngineException {
    if (entry == null) return result;

    if (entry.getProgram() == null || entry.getProgram().isEmpty()) {
      reportError(entry.getOrigin(), MESSAGE_KEY_PROGRAM_ERROR);
      return result;
    }
    return result;
  }
}
