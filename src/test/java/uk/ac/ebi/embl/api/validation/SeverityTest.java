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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SeverityTest {

  @Test
  public void testisLessSevereThan() {
    assertFalse(Severity.FIX.isLessSevereThan(Severity.FIX));
    assertTrue(Severity.FIX.isLessSevereThan(Severity.INFO));
    assertTrue(Severity.FIX.isLessSevereThan(Severity.WARNING));
    assertTrue(Severity.FIX.isLessSevereThan(Severity.ERROR));

    assertFalse(Severity.INFO.isLessSevereThan(Severity.FIX));
    assertFalse(Severity.INFO.isLessSevereThan(Severity.INFO));
    assertTrue(Severity.INFO.isLessSevereThan(Severity.WARNING));
    assertTrue(Severity.INFO.isLessSevereThan(Severity.ERROR));

    assertFalse(Severity.WARNING.isLessSevereThan(Severity.FIX));
    assertFalse(Severity.WARNING.isLessSevereThan(Severity.INFO));
    assertFalse(Severity.WARNING.isLessSevereThan(Severity.WARNING));
    assertTrue(Severity.WARNING.isLessSevereThan(Severity.ERROR));

    assertFalse(Severity.ERROR.isLessSevereThan(Severity.FIX));
    assertFalse(Severity.ERROR.isLessSevereThan(Severity.INFO));
    assertFalse(Severity.ERROR.isLessSevereThan(Severity.WARNING));
    assertFalse(Severity.ERROR.isLessSevereThan(Severity.ERROR));
  }
}
