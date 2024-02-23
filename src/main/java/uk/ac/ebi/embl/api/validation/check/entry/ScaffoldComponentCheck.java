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

import java.util.HashSet;
import java.util.Set;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Scaffold \"{0}\" has only \"{1}\" component, minimum two components expected.")
@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_CHROMOSOME,
      ValidationScope.ASSEMBLY_CONTIG,
      ValidationScope.ASSEMBLY_MASTER,
      ValidationScope.NCBI_MASTER
    })
public class ScaffoldComponentCheck extends EntryValidationCheck {

  private static final String SCAFFOLD_WITH_ONE_COMPONENT = "ScaffoldWithOneComponentCheck";

  public ValidationResult check(Entry entry) throws ValidationEngineException {

    result = new ValidationResult();

    if (entry == null
        || isIgnoreError()
        || entry.getSequence().getAgpRows() == null
        || entry.getSequence().getAgpRows().isEmpty()) {
      return result;
    }

    if (ValidationScope.ASSEMBLY_SCAFFOLD.equals(
        getEmblEntryValidationPlanProperty().validationScope.get())) {
      Set<String> components = new HashSet<>();
      for (AgpRow agpRow : entry.getSequence().getAgpRows()) {
        if (!agpRow.getComponent_type_id().equals("N")
            && !agpRow.getComponent_type_id().equals("U")) {
          components.add(agpRow.getComponent_id().toUpperCase());
        }
      }
      if (components.size() < 2) {
        reportError(
            entry.getOrigin(),
            SCAFFOLD_WITH_ONE_COMPONENT,
            entry.getSubmitterAccession(),
            components.size());
      }
    }
    return result;
  }
}
