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

import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;

@Description(
    "Multiple strain/isolate qualifiers are not allowed in Source feature"
        + "Organism name \"{0}\" is not submittable")
@GroupIncludeScope(group = {ValidationScope.Group.ASSEMBLY})
@ExcludeScope(
    validationScope = {
      ValidationScope.ASSEMBLY_CONTIG,
      ValidationScope.ASSEMBLY_SCAFFOLD,
      ValidationScope.ASSEMBLY_CHROMOSOME,
      ValidationScope.NCBI,
      ValidationScope.NCBI_MASTER
    })
public class MasterEntrySourceCheck extends EntryValidationCheck {

  private static final String MASTER_ENTRY_SOURCE_MESSAGE_ID = "MasterEntrySourceCheck_1";
  private static final String MASTER_ENTRY_ORGANISM_MESSAGE_ID = "MasterEntrySourceCheck_2";

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null || entry.getPrimarySourceFeature() == null) {
      return result;
    }

    List<Qualifier> strainQualifiers =
        entry.getPrimarySourceFeature().getQualifiers(Qualifier.STRAIN_QUALIFIER_NAME);
    List<Qualifier> isolateQualifiers =
        entry.getPrimarySourceFeature().getQualifiers(Qualifier.ISOLATE_QUALIFIER_NAME);
    String organism = entry.getPrimarySourceFeature().getScientificName();
    Long taxId = entry.getPrimarySourceFeature().getTaxId();

    if (strainQualifiers.size() > 1 || isolateQualifiers.size() > 1) {
      reportError(entry.getPrimarySourceFeature().getOrigin(), MASTER_ENTRY_SOURCE_MESSAGE_ID);
    }

    if (getEmblEntryValidationPlanProperty().taxonClient.get() != null) {
      boolean binomialRequired =
          EntryUtils.isBinomialRequired(entry, getEmblEntryValidationPlanProperty());
      boolean isOrganismSubmittable =
          getEmblEntryValidationPlanProperty()
              .taxonClient
              .get()
              .isOrganismSubmittable(organism, binomialRequired);
      boolean isTaxidSubmittable = isOrganismSubmittable;
      boolean isAnyNameSubmittable = false;
      if (taxId != null)
        isTaxidSubmittable =
            getEmblEntryValidationPlanProperty()
                .taxonClient
                .get()
                .isTaxidSubmittable(taxId, binomialRequired);
      if (!isOrganismSubmittable && !isTaxidSubmittable) {
        isAnyNameSubmittable =
            getEmblEntryValidationPlanProperty()
                .taxonClient
                .get()
                .isAnyNameSubmittable(organism, binomialRequired);
        if (!isAnyNameSubmittable)
          reportError(entry.getOrigin(), MASTER_ENTRY_ORGANISM_MESSAGE_ID, organism);
      }
    }
    return result;
  }
}
