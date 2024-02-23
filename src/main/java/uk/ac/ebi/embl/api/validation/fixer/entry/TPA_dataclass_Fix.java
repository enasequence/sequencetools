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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("TPA Description line fix")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class TPA_dataclass_Fix extends EntryValidationCheck {

  private static final String FIX_ID = "TPA_dataclass_Fix";
  private static final Pattern PATTERN = Pattern.compile("((TPA)(_)(.*))(:)((\\s*.*)+)");

  public TPA_dataclass_Fix() {}

  public ValidationResult check(Entry entry) {
    result = new ValidationResult();

    if (entry == null) {
      return result;
    }
    /* get the TPA description line Text */

    String tpaDescription = entry.getDescription().getText();
    if (tpaDescription != null) {
      Matcher matcher = PATTERN.matcher(tpaDescription);
      boolean matchFound = matcher.find();
      /* converting 'TPA_inf:', 'TPA_exp:' and 'TPA_reasm:' into 'TPA:' */
      if (matchFound) {

        /*
         * for (int i = 0; i <= matcher.groupCount(); i++) { String
         * groupStr = matcher.group(i); System.out.println(i + ":" +
         * groupStr); }
         */

        String tpaStr1 = matcher.group(2); // TPA
        String tpaStr2 = matcher.group(5); // :
        String tpaStr3 = matcher.group(6); // REMAINING TEXT
        tpaDescription = tpaStr1 + tpaStr2 + tpaStr3;
        entry.setDescription(new Text(tpaDescription));

        reportMessage(Severity.FIX, entry.getOrigin(), FIX_ID, tpaDescription);
      }
    }
    return result;
  }
}
