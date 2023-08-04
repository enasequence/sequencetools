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
package uk.ac.ebi.embl.api.validation.check.entries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class NonUniqueSubmitterAccessionCheckTest {
  private final NonUniqueSubmitterAccessionCheck check = new NonUniqueSubmitterAccessionCheck();

  @Test
  public void testCheck_NullEntryList() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_EmptyEntryList() {
    ArrayList<Entry> entryList = new ArrayList<>();
    ValidationResult result = check.check(entryList);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_NoSubmitterAccession() {
    ArrayList<Entry> entryList = new ArrayList<>();
    EntryFactory entryFactory = new EntryFactory();
    Entry entry1 = entryFactory.createEntry();
    Entry entry2 = entryFactory.createEntry();
    entryList.add(entry1);
    entryList.add(entry2);
    ValidationResult result = check.check(entryList);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_duplicateSubmitterAccession() {
    ArrayList<Entry> entryList = new ArrayList<>();
    EntryFactory entryFactory = new EntryFactory();
    Entry entry1 = entryFactory.createEntry();
    Entry entry2 = entryFactory.createEntry();
    entry1.setSubmitterAccession("test1");
    entry2.setSubmitterAccession("test1");
    entryList.add(entry1);
    entryList.add(entry2);
    ValidationResult result = check.check(entryList);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("NonUniqueSubmitterAccessionCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_uniqueSubmitterAccession() {
    ArrayList<Entry> entryList = new ArrayList<>();
    EntryFactory entryFactory = new EntryFactory();
    Entry entry1 = entryFactory.createEntry();
    Entry entry2 = entryFactory.createEntry();
    entry1.setSubmitterAccession("test1");
    entry2.setSubmitterAccession("test2");
    entryList.add(entry1);
    entryList.add(entry2);
    ValidationResult result = check.check(entryList);
    assertTrue(result.isValid());
  }
}
