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
package uk.ac.ebi.embl.api.validation.check.gff3;

import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Invalid amino acid {0} in translation.")
public class AttributeCheck extends GFF3ValidationCheck {

  private static final String MESSAGE_ID = "CDSPhaseCheck";

  public ValidationResult check(GFF3RecordSet gff3RecordSet) {
    result = new ValidationResult();

    if (gff3RecordSet == null) {
      return result;
    }
    // todo
    /*
    ID Indicates the name of the feature. IDs must be unique within the scope of the GFF file.
    */
    return result;
  }
}
