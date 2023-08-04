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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class HoldDateCheckTest {

  private Entry entry;
  private HoldDateCheck check;

  @Before
  public void setUp() {
    EntryFactory entryFactory = new EntryFactory();

    entry = entryFactory.createEntry();
    check = new HoldDateCheck();
  }

  public void testCheck_NoDataSet() {
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_PastDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 1970);
    Date pastDate = calendar.getTime();
    entry.setHoldDate(pastDate);
    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid()); // i.e. there were failures
  }

  @Test
  public void testCheck_GoodDate() {
    Calendar calendar = Calendar.getInstance();
    int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
    calendar.set(
        Calendar.DAY_OF_YEAR,
        dayOfYear + 1); // yes, this test will fail on new year's eve - go home!!!
    Date futureDate = calendar.getTime();
    entry.setHoldDate(futureDate);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }
}
