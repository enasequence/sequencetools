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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

public class PrimaryAccessionCheckTest {

  private Entry entry;
  private PrimaryAccessionCheck check;
  private EntryDAOUtils entryDAOUtils;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    check = new PrimaryAccessionCheck();
    entryDAOUtils = createMock(EntryDAOUtils.class);
  }

  @Test
  public void testcheck_noEntry() throws ValidationEngineException {
    ValidationResult result = check.check(null);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_NoPrimaryaccession() throws ValidationEngineException {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_validPrimaryAccession() throws ValidationEngineException, SQLException {
    entry.setPrimaryAccession("ABCD01000001");
    entry.setDataClass("WGS");
    expect(entryDAOUtils.getDbcode("ABCD")).andReturn("E");
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_invalidPrimaryAccessionPrefix()
      throws ValidationEngineException, SQLException {
    entry.setPrimaryAccession("ABCD01000001");
    entry.setDataClass("WGS");
    expect(entryDAOUtils.getDbcode("ABCD")).andReturn(null);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("PrimaryAccessionCheck2", Severity.ERROR));
  }

  @Test
  public void testCheck_invalidDataclass() throws ValidationEngineException, SQLException {
    entry.setPrimaryAccession("ABCD01000001");
    entry.setDataClass("CON");
    expect(entryDAOUtils.getDbcode("ABCD")).andReturn(null);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("PrimaryAccessionCheck1", Severity.ERROR));
  }
}
