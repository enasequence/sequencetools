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

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class AssemblyLevelEntryNameFixTest {

  private AssemblyLevelEntryNameFix check;
  private EntryFactory entryFactory;
  private Entry entry;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_MASTER);
    check = new AssemblyLevelEntryNameFix();
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoSeqNumber() throws ValidationEngineException, SQLException {
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    check.check(entry);
    assertNull(entry.getSubmitterAccession());
  }

  @Test
  public void testCheck_noEntryName() throws SQLException, ValidationEngineException {
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    property.sequenceNumber.set(1);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertNotNull(entry.getSubmitterAccession());
    assertEquals("contig1", entry.getSubmitterAccession());
    assertEquals(1, result.count("AssemblyLevelEntryNameFix", Severity.FIX));
  }

  @Test
  public void testCheck_withEntryName() throws SQLException, ValidationEngineException {
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    entry.setSubmitterAccession("fgdfgd");
    ValidationResult result = check.check(entry);
    assertNotNull(entry.getSubmitterAccession());
    assertEquals(0, result.count("AssemblyLevelEntryNameFix", Severity.FIX));
  }
}
