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
package uk.ac.ebi.embl.api.validation.check.entries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;

@ExcludeScope(validationScope = {ValidationScope.ASSEMBLY_MASTER})
@GroupIncludeScope(group = {ValidationScope.Group.ASSEMBLY})
public class NonUniqueSubmitterAccessionCheck extends EntriesValidationCheck {
  protected static final String SUBMITTER_ACCESSION_NOT_UNIQUE_MESSAGE_ID =
      "NonUniqueSubmitterAccessionCheck";

  @Override
  public ValidationResult check(ArrayList<Entry> entryList) {
    result = new ValidationResult();
    if (entryList == null) {
      return result;
    }
    Set<String> submitterAccessions = new HashSet<>();
    for (Entry entry : entryList) {
      String submitterAccession = entry.getSubmitterAccession();
      if (submitterAccession == null) {
        continue;
      }
      if (!submitterAccessions.add(submitterAccession)) {
        reportError(
            entry.getOrigin(), SUBMITTER_ACCESSION_NOT_UNIQUE_MESSAGE_ID, submitterAccession);
      }
    }

    return result;
  }
}
