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
package uk.ac.ebi.embl.api.validation;

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.plan.ValidationUnit;

public class ChecksAndFixFilter {

  private static List<Class<? extends EmblEntryValidationCheck<?>>> checks;
  private static List<Class<? extends EmblEntryValidationCheck<?>>> fix;

  public static void main(String[] args) {
    new ChecksAndFixFilter().filterChecksAndFix(ValidationScope.NCBI);
    checks.forEach(System.out::println);
    System.out.println("==================================================================");
    fix.forEach(System.out::println);
  }

  private void filterChecksAndFix(ValidationScope valScope) {
    checks = new ArrayList<>();
    fix = new ArrayList<>();

    for (ValidationUnit val : ValidationUnit.values()) {

      for (Class<? extends EmblEntryValidationCheck<?>> checkOrFix : val.getValidationUnit()) {

        ExcludeScope excludeScopeAnnotation = checkOrFix.getAnnotation(ExcludeScope.class);
        boolean isInScope = true;
        if (excludeScopeAnnotation != null) {

          for (ValidationScope scope : excludeScopeAnnotation.validationScope()) {
            if (scope.equals(valScope)) {
              isInScope = false;
              break;
            }
          }
        }

        if (isInScope) {
          if (checkOrFix.getCanonicalName().endsWith("Fix")) {
            fix.add(checkOrFix);
          } else {
            checks.add(checkOrFix);
          }
        }
      }
    }
  }

  public List<Class<? extends EmblEntryValidationCheck<?>>> getChecks(ValidationScope scope) {
    if (checks == null) filterChecksAndFix(scope);
    return checks;
  }

  public List<Class<? extends EmblEntryValidationCheck<?>>> getFix(ValidationScope scope) {
    if (fix == null) filterChecksAndFix(scope);
    return fix;
  }
}
