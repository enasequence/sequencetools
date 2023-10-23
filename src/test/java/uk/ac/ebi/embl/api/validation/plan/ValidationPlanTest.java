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
package uk.ac.ebi.embl.api.validation.plan;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.*;

public class ValidationPlanTest {

  private ValidationPlan plan;
  private ValidationResult result;

  @Before
  public void init() {
    ValidationPlan validationPlan =
        new ValidationPlan(ValidationScope.EMBL, false) {
          @Override
          public ValidationResult execute(Object target) {
            return null;
          }
        };
    plan = validationPlan;
    plan.addMessageBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    plan.addMessageBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    result = new ValidationResult();
    result.append(
        new ValidationResult().append(new ValidationMessage<Origin>(Severity.ERROR, "KEY1")));
    result.append(
        new ValidationResult().append(new ValidationMessage<Origin>(Severity.WARNING, "KEY2")));
    result.append(
        new ValidationResult().append(new ValidationMessage<Origin>(Severity.INFO, "KEY3")));
  }

  @Test
  public void testDemoteSeverity_toError() {
    plan.demoteSeverity(result, Severity.ERROR);
    assertEquals(3, result.count());
    assertEquals(1, result.count(Severity.ERROR));
    assertEquals(1, result.count(Severity.WARNING));
    assertEquals(1, result.count(Severity.INFO));
  }

  @Test
  public void testDemoteSeverity_toWarning() {
    plan.demoteSeverity(result, Severity.WARNING);
    assertEquals(3, result.count());
    assertEquals(2, result.count(Severity.WARNING));
    assertEquals(1, result.count(Severity.INFO));
  }

  @Test
  public void testDemoteSeverity_toInfo() {
    plan.demoteSeverity(result, Severity.INFO);
    assertEquals(3, result.count());
    assertEquals(3, result.count(Severity.INFO));
  }

  @Test
  public void testIsInValidationScope_Empty() {
    assertFalse(plan.isInValidationScope(new ValidationScope[0]));
  }

  @Test
  public void testIsInValidationScope_Null() {
    assertFalse(plan.isInValidationScope(null));
  }

  @Test
  public void testIsInValidationScope_NullElement() {
    assertFalse(plan.isInValidationScope(new ValidationScope[] {null}));
  }

  @Test
  public void testIsInValidationScope_Right() {
    assertTrue(plan.isInValidationScope(new ValidationScope[] {ValidationScope.EMBL}));
  }

  @Test
  public void testIsInValidationScopePutffGroup() {
    assertTrue(plan.validationScope.isInGroup(ValidationScope.Group.PUTFF));
    assertFalse(plan.validationScope.isInGroup(ValidationScope.Group.PIPELINE));
  }

  @Test
  public void testIsInValidationScopePipelineGroup() {
    ValidationPlan plan =
        new ValidationPlan(ValidationScope.ASSEMBLY_MASTER, false) {
          @Override
          public ValidationResult execute(Object target) {
            return null;
          }
        };

    assertTrue(plan.validationScope.isInGroup(ValidationScope.Group.PIPELINE));
    assertTrue(plan.validationScope.isInGroup(ValidationScope.Group.ASSEMBLY));
    assertFalse(plan.validationScope.isInGroup(ValidationScope.Group.PUTFF));
  }
}
