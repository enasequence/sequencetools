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

import uk.ac.ebi.embl.api.entry.genomeassembly.ChromosomeEntry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")
public class ChromosomeListChromosomeLocationCheck
    extends GenomeAssemblyValidationCheck<ChromosomeEntry> {
  private final String MESSAGE_KEY_INVALID_CHROMOSOME_LOCATION_ERROR =
      "ChromosomeListChromosomeLocationValidCheck";

  public ValidationResult check(ChromosomeEntry entry) {
    if (entry == null) return result;
    if (null == entry.getChromosomeLocation()) return result;

    if (checkAndFixChromosomeLocation(entry, "macronuclear")) return result;
    if (checkAndFixChromosomeLocation(entry, "nucleomorph")) return result;
    if (checkAndFixChromosomeLocation(entry, "mitochondrion")) return result;
    if (checkAndFixChromosomeLocation(entry, "kinetoplast")) return result;
    if (checkAndFixChromosomeLocation(entry, "chloroplast")) return result;
    if (checkAndFixChromosomeLocation(entry, "chromoplast")) return result;
    if (checkAndFixChromosomeLocation(entry, "plastid")) return result;
    if (checkAndFixChromosomeLocation(entry, "virion")) return result;
    if (checkAndFixChromosomeLocation(entry, "phage")) return result;
    if (checkAndFixChromosomeLocation(entry, "proviral")) return result;
    if (checkAndFixChromosomeLocation(entry, "prophage")) return result;
    if (checkAndFixChromosomeLocation(entry, "viroid")) return result;
    if (checkAndFixChromosomeLocation(entry, "cyanelle")) return result;
    if (checkAndFixChromosomeLocation(entry, "apicoplast")) return result;
    if (checkAndFixChromosomeLocation(entry, "leucoplast")) return result;
    if (checkAndFixChromosomeLocation(entry, "proplastid")) return result;
    if (checkAndFixChromosomeLocation(entry, "hydrogenosome")) return result;
    if (checkAndFixChromosomeLocation(entry, "chromatophore")) return result;

    reportError(
        entry.getOrigin(),
        MESSAGE_KEY_INVALID_CHROMOSOME_LOCATION_ERROR,
        entry.getChromosomeLocation());
    return result;
  }

  private boolean checkAndFixChromosomeLocation(ChromosomeEntry entry, String location) {
    if (entry.getChromosomeLocation().equalsIgnoreCase(location)) {
      entry.setChromosomeLocation(location);
      return true;
    }
    return false;
  }
}
