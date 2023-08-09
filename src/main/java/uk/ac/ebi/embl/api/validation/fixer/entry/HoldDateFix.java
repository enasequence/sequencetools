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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import java.util.Calendar;
import java.util.Date;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("hold_date has been removed from entry as it is in the past")
@GroupIncludeScope(group = {ValidationScope.Group.ASSEMBLY})
public class HoldDateFix extends EntryValidationCheck {

  private static final String FIX_ID = "Hold_dateFix_1";

  public HoldDateFix() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    if (entry.getHoldDate() == null) {
      return result;
    }

    Date holdDate = entry.getHoldDate();
    Date now = Calendar.getInstance().getTime();
    if (holdDate.compareTo(now) < 0) {

      entry.setHoldDate(null);
      reportMessage(Severity.FIX, entry.getOrigin(), FIX_ID);
    }

    return result;
  }
}
