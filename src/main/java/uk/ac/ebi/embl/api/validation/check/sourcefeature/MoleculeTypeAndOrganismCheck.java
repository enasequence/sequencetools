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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import java.util.Collection;
import org.apache.commons.lang.ArrayUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("Organism must belong to one of {0} when molecule type is {1}.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class MoleculeTypeAndOrganismCheck extends EntryValidationCheck {

  private static final String MESSAGE_ID = "MoleculeTypeAndOrganismCheck";

  public MoleculeTypeAndOrganismCheck() {}

  public ValidationResult check(Entry entry) {
    DataSet dataSet = GlobalDataSets.getDataSet(GlobalDataSetFile.MOLTYPE_ORGANISM);
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    for (DataRow dataRow : dataSet.getRows()) {
      String[] requiredOrganisms = dataRow.getStringArray(0);
      String moleculeType = dataRow.getString(1);

      if (moleculeType == null || ArrayUtils.isEmpty(requiredOrganisms)) {
        return result;
      }
      if (moleculeType.equals(SequenceEntryUtils.getMoleculeType(entry))) {
        Collection<Feature> sources =
            SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
        if (sources.isEmpty()) {
          return result; // just get out - other checks for source features
        }
        for (Feature source : sources) {
          Collection<Qualifier> organisms = source.getQualifiers(Qualifier.ORGANISM_QUALIFIER_NAME);
          if (organisms.isEmpty()) {
            reportError(entry, requiredOrganisms, moleculeType);
          }
          for (Qualifier organism : organisms) {
            if (getEmblEntryValidationPlanProperty()
                    .taxonClient
                    .get()
                    .isOrganismValid(organism.getValue())
                && !getEmblEntryValidationPlanProperty()
                    .taxonClient
                    .get()
                    .isChildOfAny(organism.getValue(), requiredOrganisms)) {
              reportError(entry, requiredOrganisms, moleculeType);
            }
          }
        }
      }
    }
    return result;
  }

  private void reportError(Entry entry, String[] requiredOrganisms, String moleculeType) {
    reportError(
        entry.getOrigin(), MESSAGE_ID, Utils.paramArrayToString(requiredOrganisms), moleculeType);
  }
}
