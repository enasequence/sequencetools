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
package uk.ac.ebi.embl.api.validation.check.sequence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class SequenceExistsCheckTest {
  private Entry entry;
  private EntryFactory entryFactory;
  private LocationFactory locationFactory;
  private SequenceExistsCheck check;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    locationFactory = new LocationFactory();
    entry = entryFactory.createEntry();
    entry.setDataClass(Entry.STANDARD_DATACLASS);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    check = new SequenceExistsCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoSequence() {
    assertFalse(check.check(entry).isValid());
  }

  @Test
  public void testCheck_scope() {
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    assertFalse(check.check(entry).isValid());
  }

  @Test
  public void testCheck_Coline() {
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    Collection<Location> locations = new ArrayList<Location>();
    locations.add(locationFactory.createLocalRange(5L, 10L));
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequence();
    entry.setSequence(sequence);
    entry.getSequence().addContigs(locations);
    assertFalse(check.check(entry).isValid());
  }
}
