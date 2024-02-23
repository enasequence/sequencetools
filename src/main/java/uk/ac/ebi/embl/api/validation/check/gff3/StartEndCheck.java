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

import uk.ac.ebi.embl.api.gff3.GFF3Record;
import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("End position \"{0}\" is less than the start position \"{1}\"")
public class StartEndCheck extends GFF3ValidationCheck {

  private static final String MESSAGE_ID = "StartEndCheck";

  public ValidationResult check(GFF3RecordSet gff3RecordSet) {
    result = new ValidationResult();

    if (gff3RecordSet == null) {
      return result;
    }

    for (GFF3Record gff3Record : gff3RecordSet.getRecords()) {
      if (gff3Record.getEnd() < gff3Record.getStart()) {
        reportError(
            gff3Record.getOrigin(),
            MESSAGE_ID,
            Integer.toString(gff3Record.getEnd()),
            Integer.toString(gff3Record.getStart()));
      }
    }

    return result;
  }
}
