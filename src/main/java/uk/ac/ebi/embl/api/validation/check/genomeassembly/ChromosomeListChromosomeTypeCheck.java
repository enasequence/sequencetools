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

import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")
public class ChromosomeListChromosomeTypeCheck
    extends GenomeAssemblyValidationCheck<ChromosomeEntry> {

  private final String MESSAGE_KEY_MISSING_CHROMOSOME_TYPE_ERROR =
      "ChromosomeListMissingChromosomeTypeCheck";
  private final String MESSAGE_KEY_INVALID_CHROMOSOME_TYPE_ERROR =
      "ChromosomeListChromosomeTypeValidCheck";

  public ValidationResult check(ChromosomeEntry entry) throws ValidationEngineException {
    if (entry == null) {
      return result;
    }

    if (null == entry.getChromosomeType()) {
      reportError(entry.getOrigin(), MESSAGE_KEY_MISSING_CHROMOSOME_TYPE_ERROR);
    } else {
      if (checkAndFixChromosomeType(entry, "chromosome")) return result;
      if (checkAndFixChromosomeType(entry, "plasmid")) return result;
      if (checkAndFixChromosomeType(entry, "monopartite")) return result;
      if (checkAndFixChromosomeType(entry, "segmented")) return result;
      if (checkAndFixChromosomeType(entry, "multipartite")) return result;
      if (checkAndFixChromosomeType(entry, "linkage_group")) return result;

      reportError(
          entry.getOrigin(), MESSAGE_KEY_INVALID_CHROMOSOME_TYPE_ERROR, entry.getChromosomeType());
    }

    return result;
  }

  private boolean checkAndFixChromosomeType(ChromosomeEntry entry, String type) {
    if (entry.getChromosomeType().equalsIgnoreCase(type)) {
      entry.setChromosomeType(type);
      return true;
    }
    return false;
  }
}
