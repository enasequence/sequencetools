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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class ContigstosequenceFixTest {

  private ContigstosequenceFix check;
  private EntryFactory entryFactory;
  private SequenceFactory sequenceFactory;
  private LocationFactory locationFactory;
  private Entry entry;
  private ArrayList<Location> contigs;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    sequenceFactory = new SequenceFactory();
    locationFactory = new LocationFactory();
    entry = entryFactory.createEntry();
    entry.setSequence(sequenceFactory.createSequence());
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    property.analysis_id.set("ERZ0001");
    check = new ContigstosequenceFix();
    check.setEmblEntryValidationPlanProperty(property);
    RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    RemoteRange remoteRange2 = locationFactory.createRemoteRange("A00002", 1, 10L, 20L);
    RemoteRange remoteRange3 = locationFactory.createRemoteRange("A00003", 1, 10L, 20L);
    Gap gap1 = locationFactory.createGap(10);
    Gap gap2 = locationFactory.createGap(10);
    contigs = new ArrayList<Location>();
    contigs.add(remoteRange1);
    contigs.add(gap1);
    contigs.add(remoteRange2);
    contigs.add(gap2);
    contigs.add(remoteRange3);
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoContigs() throws ValidationEngineException {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_withSequence() throws ValidationEngineException {
    entry.getSequence().setSequence(ByteBuffer.wrap("aaa".getBytes()));
    assertTrue(check.check(entry).isValid());
  }
}
