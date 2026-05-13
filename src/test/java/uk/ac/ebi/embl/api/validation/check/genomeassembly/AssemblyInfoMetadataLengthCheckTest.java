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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class AssemblyInfoMetadataLengthCheckTest {
  private static final String MESSAGE_KEY = "AssemblyInfoMetadataFieldLengthCheck";
  private static final int MAX_LENGTH = 1000;

  private AssemblyInfoMetadataLengthCheck check;

  @Before
  public void setUp() throws SQLException {
    check = new AssemblyInfoMetadataLengthCheck();
  }

  @Test
  public void testCheck_noEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_programTooLong() throws ValidationEngineException {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    entry.setProgram(StringUtils.repeat("A", MAX_LENGTH + 1));

    ValidationResult result = check.check(entry);

    assertEquals(1, result.count(MESSAGE_KEY, Severity.ERROR));
  }

  @Test
  public void testCheck_platformTooLong() throws ValidationEngineException {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    entry.setPlatform(StringUtils.repeat("A", MAX_LENGTH + 1));

    ValidationResult result = check.check(entry);

    assertEquals(1, result.count(MESSAGE_KEY, Severity.ERROR));
  }

  @Test
  public void testCheck_coverageTooLong() throws ValidationEngineException {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    entry.setCoverage(StringUtils.repeat("A", MAX_LENGTH + 1));

    ValidationResult result = check.check(entry);

    assertEquals(1, result.count(MESSAGE_KEY, Severity.ERROR));
  }

  @Test
  public void testCheck_assemblyTypeTooLong() throws ValidationEngineException {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    entry.setAssemblyType(StringUtils.repeat("A", MAX_LENGTH + 1));

    ValidationResult result = check.check(entry);

    assertEquals(1, result.count(MESSAGE_KEY, Severity.ERROR));
  }

  @Test
  public void testCheck_moleculeTypeTooLong() throws ValidationEngineException {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    entry.setMoleculeType(StringUtils.repeat("A", MAX_LENGTH + 1));

    ValidationResult result = check.check(entry);

    assertEquals(1, result.count(MESSAGE_KEY, Severity.ERROR));
  }

  @Test
  public void testCheck_organismTooLong() throws ValidationEngineException {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    entry.setOrganism(StringUtils.repeat("A", MAX_LENGTH + 1));

    ValidationResult result = check.check(entry);

    assertEquals(1, result.count(MESSAGE_KEY, Severity.ERROR));
  }

  @Test
  public void testCheck_allWithinLimit() throws ValidationEngineException {
    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    String value = StringUtils.repeat("A", MAX_LENGTH);
    entry.setProgram(value);
    entry.setPlatform(value);
    entry.setCoverage(value);
    entry.setAssemblyType(value);
    entry.setMoleculeType(value);
    entry.setOrganism(value);

    ValidationResult result = check.check(entry);

    assertEquals(0, result.count(MESSAGE_KEY, Severity.ERROR));
  }
}
