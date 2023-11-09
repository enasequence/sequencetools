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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.*;

public class AssemblyInfoAnalysisIdCheckTest {
  private AssemblyInfoAnalysisIdCheck check;
  Origin origin = null;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);
    check = new AssemblyInfoAnalysisIdCheck();
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoAnalysisId() {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("AssemblyInfoMissingAnalysisIDCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_InvalidAnalysisId() {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    entry.setAnalysisId("ERC090988");
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("AssemblyInfoInvalidAnalysisIDCheck", Severity.ERROR));
  }
}
