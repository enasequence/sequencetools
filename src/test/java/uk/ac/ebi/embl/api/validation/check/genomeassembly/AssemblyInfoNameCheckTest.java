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
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class AssemblyInfoNameCheckTest {
  private AssemblyInfoEntry assemblyEntry;
  private AssemblyInfoNameCheck check;
  private EmblEntryValidationPlanProperty planProperty;

  @Before
  public void setUp() throws SQLException {
    check = new AssemblyInfoNameCheck();
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    planProperty = TestHelper.testEmblEntryValidationPlanProperty();
    planProperty.analysis_id.set("ERZ0001");
    check.setEmblEntryValidationPlanProperty(planProperty);
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoName() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    ValidationResult result = check.check(assemblyEntry);
    assertEquals(1, result.count("AssemblyInfoMissingNameCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_invalidNamePattern() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    assemblyEntry.setName("dfdfg878*dhfgh");
    ValidationResult result = check.check(assemblyEntry);
    assertEquals(1, result.count("AssemblyInfoInvalidAssemblyName", Severity.ERROR));
  }

  @Test
  public void testCheck_invalidNameLength() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    assemblyEntry.setName(
        "dsfdfhfjhgkhjkdgsfsdfsfsfgdfhdghgjhfjdtsrrgdfhdgjhfjyyttkkykykykrthhdsdgghjgklwerrtytrtyuytutiyuiyuioio");
    ValidationResult result = check.check(assemblyEntry);
    assertEquals(1, result.count("AssemblyInfoInvalidAssemblyNameLength", Severity.ERROR));
  }

  @Test
  public void testCheck_validName() throws ValidationEngineException {
    assemblyEntry = new AssemblyInfoEntry();
    assemblyEntry.setName("fdgfghhjhgj");
    assertTrue(check.check(assemblyEntry).isValid());
  }
}
