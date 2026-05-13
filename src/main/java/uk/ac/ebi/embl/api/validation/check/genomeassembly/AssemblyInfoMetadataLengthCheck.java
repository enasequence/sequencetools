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
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")
public class AssemblyInfoMetadataLengthCheck
    extends GenomeAssemblyValidationCheck<AssemblyInfoEntry> {
  private static final String MESSAGE_KEY_METADATA_FIELD_LENGTH_ERROR =
      "AssemblyInfoMetadataFieldLengthCheck";
  private static final int MAX_METADATA_FIELD_LENGTH = 1000;

  @Override
  public ValidationResult check(AssemblyInfoEntry entry) throws ValidationEngineException {
    if (entry == null) return result;

    reportIfTooLong(entry, "PROGRAM", entry.getProgram());
    reportIfTooLong(entry, "PLATFORM", entry.getPlatform());
    reportIfTooLong(entry, "COVERAGE", entry.getCoverage());
    reportIfTooLong(entry, "ASSEMBLY_TYPE", entry.getAssemblyType());
    reportIfTooLong(entry, "MOLECULETYPE", entry.getMoleculeType());
    reportIfTooLong(entry, "ORGANISM", entry.getOrganism());

    return result;
  }

  private void reportIfTooLong(AssemblyInfoEntry entry, String fieldName, String fieldValue) {
    if (fieldValue == null || fieldValue.length() <= MAX_METADATA_FIELD_LENGTH) return;

    reportError(
        entry.getOrigin(),
        MESSAGE_KEY_METADATA_FIELD_LENGTH_ERROR,
        fieldName,
        fieldValue.length(),
        MAX_METADATA_FIELD_LENGTH);
  }
}
