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
package uk.ac.ebi.embl.api.validation.fixer.genomeassembly;

import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.GenomeAssemblyValidationCheck;

@Description("Assembly Type has been changed from : {0} to {1}")
public class AssemblyTypeFix extends GenomeAssemblyValidationCheck<AssemblyInfoEntry> {
  private final String MESSAGE_KEY_ASSEMBLY_TYPE_FIX = "AssemblyinfoAssemblyTypeFix";

  @Override
  public ValidationResult check(AssemblyInfoEntry entry) {

    if (entry == null || entry.getAssemblyType() == null) return result;

    for (AssemblyType assemblyType : AssemblyType.class.getEnumConstants()) {
      if (entry.getAssemblyType().toUpperCase().equals(assemblyType.getValue())) {
        if (!assemblyType.getFixedValue().equals(entry.getAssemblyType())) {
          entry.setAssemblyType(assemblyType.getFixedValue());
          reportMessage(
              Severity.FIX,
              entry.getOrigin(),
              MESSAGE_KEY_ASSEMBLY_TYPE_FIX,
              entry.getAssemblyType());
          break;
        }
      }
    }

    return result;
  }
}
