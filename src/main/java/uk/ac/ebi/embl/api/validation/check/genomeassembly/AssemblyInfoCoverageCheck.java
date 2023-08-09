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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")
public class AssemblyInfoCoverageCheck extends GenomeAssemblyValidationCheck<AssemblyInfoEntry> {
  private final String MESSAGE_KEY_COVERAGE_ERROR = "AssemblyinfoCoverageCheck";

  @Override
  public ValidationResult check(AssemblyInfoEntry entry) throws ValidationEngineException {
    if (entry == null || entry.getCoverage() == null) return result;
    entry.setCoverage(StringUtils.removeEnd(entry.getCoverage().trim().toLowerCase(), "x"));
    if (!NumberUtils.isNumber(entry.getCoverage()))
      reportError(entry.getOrigin(), MESSAGE_KEY_COVERAGE_ERROR, entry.getCoverage());
    return result;
  }
}
