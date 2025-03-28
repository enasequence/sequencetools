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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;

@Description("Invalid Location:Complement ignored in transl_except")
public class Transl_exceptLocationFix extends FeatureValidationCheck {

  private static final String Transl_ExceptValueFix_ID = "transl_exceptLocationFix";

  public Transl_exceptLocationFix() {}

  public ValidationResult check(Feature feature) {
    result = new ValidationResult();

    if (feature == null) {
      return result;
    }

    for (Qualifier tequalifier : feature.getQualifiers(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME)) {

      TranslExceptQualifier tQualifier = (TranslExceptQualifier) tequalifier;
      StringBuffer fixedValue = new StringBuffer("(pos:");

      try {
        boolean fixed = false;
        CompoundLocation<Location> compoundlocation = tQualifier.getLocations();

        // Remove complement from individual location
        for (Location slocation : compoundlocation.getLocations()) {
          if (slocation.isComplement()) {
            fixed = true;
            slocation.setComplement(false);
          }
        }

        // Remove complement from compound location
        if (compoundlocation.isComplement()) {
          fixed = true;
          compoundlocation.setComplement(false);
        }

        String locationString = FeatureLocationWriter.renderCompoundLocation(compoundlocation);
        fixedValue.append(locationString);
        fixedValue.append(",aa:" + tQualifier.getAminoAcid().getAbbreviation() + ")");
        tQualifier.setValue(fixedValue.toString());
        if (fixed) reportMessage(Severity.FIX, tequalifier.getOrigin(), Transl_ExceptValueFix_ID);
      } catch (ValidationException e) {

      }
    }

    return result;
  }
}
