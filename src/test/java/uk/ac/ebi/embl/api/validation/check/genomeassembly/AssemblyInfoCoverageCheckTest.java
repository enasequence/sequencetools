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

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class AssemblyInfoCoverageCheckTest {
  private AssemblyInfoEntry assemblyEntry;
  private AssemblyInfoCoverageCheck check;

  @Before
  public void setUp() throws SQLException {
    check = new AssemblyInfoCoverageCheck();
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoCoverage() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    assertTrue(check.check(assemblyEntry).isValid());
  }

  @Test
  public void testCheck_invalidCoverage() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    assemblyEntry.setCoverage("sdffgdfg");
    ValidationResult result = check.check(assemblyEntry);
    assertEquals(1, result.count("AssemblyinfoCoverageCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_validCoverage1() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    assemblyEntry.setCoverage("123.45x");
    ValidationResult result = check.check(assemblyEntry);
    assertEquals(0, result.count("AssemblyinfoCoverageCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_validCoverage2() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    assemblyEntry.setCoverage("546x");
    ValidationResult result = check.check(assemblyEntry);
    assertEquals(0, result.count("AssemblyinfoCoverageCheck", Severity.ERROR));
  }
}
