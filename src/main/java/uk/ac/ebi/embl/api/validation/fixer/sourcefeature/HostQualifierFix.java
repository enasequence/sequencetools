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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import java.util.List;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

@Description("\"{0}\" qualifier value has been changed from \"{1}\" to scientific name \"{2}\"")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class HostQualifierFix extends FeatureValidationCheck {
  private static final String HOST_QUALIFIER_VALUE_FIX_ID = "HostQualifierFix_1";

  public ValidationResult check(Feature feature) {
    result = new ValidationResult();

    if (!(feature instanceof SourceFeature)) {
      return result;
    }

    SourceFeature source = (SourceFeature) feature;
    List<Qualifier> hostQualifiers = source.getQualifiers(Qualifier.HOST_QUALIFIER_NAME);
    for (Qualifier hostQualifier : hostQualifiers) {
      String hostQualifierValue = hostQualifier.getValue();
      if (!getEmblEntryValidationPlanProperty()
          .taxonClient
          .get()
          .isOrganismValid(hostQualifierValue)) {

        List<Taxon> taxon =
            getEmblEntryValidationPlanProperty()
                .taxonClient
                .get()
                .getTaxonsByCommonName(hostQualifierValue);
        if (taxon != null && !taxon.isEmpty()) {
          String newValue = taxon.get(0).getScientificName();
          if (newValue != null) {
            reportMessage(
                Severity.FIX,
                hostQualifier.getOrigin(),
                HOST_QUALIFIER_VALUE_FIX_ID,
                Qualifier.HOST_QUALIFIER_NAME,
                hostQualifier.getValue(),
                newValue);
            hostQualifier.setValue(newValue);
          }
        }
      }
    }

    return result;
  }
}
