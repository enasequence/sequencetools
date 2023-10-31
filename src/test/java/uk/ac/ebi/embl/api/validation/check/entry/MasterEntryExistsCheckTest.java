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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class MasterEntryExistsCheckTest {

  private MasterEntryExistsCheck check;
  private EntryDAOUtils entryDAOUtils;
  private EntryFactory entryFactory;
  private Entry entry;
  EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    entryDAOUtils = createMock(EntryDAOUtils.class);
    check = new MasterEntryExistsCheck();
    property = TestHelper.testEmblEntryValidationPlanProperty();
    check.setEntryDAOUtils(entryDAOUtils);
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_noAnalysisId() throws ValidationEngineException, SQLException {
    entry.setDataClass(Entry.WGS_DATACLASS);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("MasterEntryExistsCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_invalidAnalysisId() throws SQLException, ValidationEngineException {
    entry.setDataClass(Entry.STD_DATACLASS);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    property.analysis_id.set("ERZ0001");
    check.setEmblEntryValidationPlanProperty(property);
    expect(entryDAOUtils.isEntryExists(property.analysis_id.get())).andReturn(false);
    replay(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("MasterEntryExistsCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_validAnalysisId() throws SQLException, ValidationEngineException {
    entry.setDataClass(Entry.STD_DATACLASS);
    entry.addXRef(new XRef("BioSample", "sdffg"));
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    property.analysis_id.set("ERZ0001");
    check.setEmblEntryValidationPlanProperty(property);
    expect(entryDAOUtils.isEntryExists(property.analysis_id.get())).andReturn(true);
    replay(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("MasterEntryExistsCheck_1", Severity.ERROR));
  }
}
