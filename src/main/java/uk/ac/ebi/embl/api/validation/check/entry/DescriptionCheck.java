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
package uk.ac.ebi.embl.api.validation.check.entry;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@ExcludeScope(
    validationScope = {
      ValidationScope.ARRAYEXPRESS,
      ValidationScope.ASSEMBLY_CHROMOSOME,
      ValidationScope.ASSEMBLY_CONTIG,
      ValidationScope.ASSEMBLY_MASTER,
      ValidationScope.ASSEMBLY_SCAFFOLD,
      ValidationScope.ASSEMBLY_TRANSCRIPTOME,
      ValidationScope.NCBI,
      ValidationScope.NCBI_MASTER
    })
public class DescriptionCheck extends EntryValidationCheck {

  private static final String INVALID_DE_LINE = "templateInvalidDescription";

  @Override
  public ValidationResult check(Entry entry) throws ValidationEngineException {

    if (entry == null) {
      return result;
    }

    if (entry.getDescription() == null
        || StringUtils.isBlank(entry.getDescription().getText())
        || entry.getDescription().getText().length() < 10) {
      reportError(entry.getOrigin(), INVALID_DE_LINE);
    }

    return result;
  }
}
