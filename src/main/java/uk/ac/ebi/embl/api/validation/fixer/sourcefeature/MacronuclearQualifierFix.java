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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description(
    "Qualifier \"organelle\" with value \"macronuclear\" has been converted to the qualifier \"macronuclear\".")
public class MacronuclearQualifierFix extends FeatureValidationCheck {

  private static final String FIX_MESSAGE_ID = "MacronuclearQualifierFix_1";

  @Override
  public ValidationResult check(Feature feature) {
    result = new ValidationResult();

    if (!(feature instanceof SourceFeature)) {
      return result;
    }

    SourceFeature source = (SourceFeature) feature;
    List<Qualifier> organelleQualifiers =
        new ArrayList<>(source.getQualifiers(Qualifier.ORGANELLE_QUALIFIER_NAME));

    if (organelleQualifiers.isEmpty()) {
      return result;
    }

    boolean hasMacronuclearQualifier =
        source.getSingleQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME) != null;

    for (Qualifier organelleQualifier : organelleQualifiers) {
      String value = organelleQualifier.getValue();
      if (value != null && value.trim().equalsIgnoreCase(Qualifier.MACRONUCLEAR_QUALIFIER_NAME)) {
        source.removeQualifier(organelleQualifier);
        if (!hasMacronuclearQualifier) {
          source.addQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME);
          hasMacronuclearQualifier = true;
        }
        reportMessage(Severity.FIX, source.getOrigin(), FIX_MESSAGE_ID);
      }
    }

    return result;
  }
}
