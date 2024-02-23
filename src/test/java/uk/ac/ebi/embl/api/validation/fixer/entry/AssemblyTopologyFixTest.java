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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class AssemblyTopologyFixTest {

  private AssemblyTopologyFix check;
  private EntryFactory entryFactory;
  private Entry entry;
  EmblEntryValidationPlanProperty property;
  SequenceFactory sequenceFactory = new SequenceFactory();

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    check = new AssemblyTopologyFix();
    property = TestHelper.testEmblEntryValidationPlanProperty();
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoSequence() throws ValidationEngineException {

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_sequencewithnoTopology() throws ValidationEngineException, SQLException {
    Sequence sequence = sequenceFactory.createSequence();
    entry.setSequence(sequence);
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_contigsequencewithLinearTopology()
      throws ValidationEngineException, SQLException {
    Sequence sequence = sequenceFactory.createSequence();
    sequence.setTopology(Topology.LINEAR);
    entry.setSequence(sequence);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("AssemblyTopologyFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_NonContigsequencewithLinearTopology()
      throws ValidationEngineException, SQLException {
    Sequence sequence = sequenceFactory.createSequence();
    sequence.setTopology(Topology.LINEAR);
    entry.setSequence(sequence);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("AssemblyTopologyFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_ContigsequencewithCircularTopology()
      throws ValidationEngineException, SQLException {
    Sequence sequence = sequenceFactory.createSequence();
    sequence.setTopology(Topology.CIRCULAR);
    entry.setSequence(sequence);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("AssemblyTopologyFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_NonContigsequencewithCircularTopology()
      throws ValidationEngineException, SQLException {
    Sequence sequence = sequenceFactory.createSequence();
    sequence.setTopology(Topology.CIRCULAR);
    entry.setSequence(sequence);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_SCAFFOLD);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("AssemblyTopologyFix_1", Severity.FIX));
  }
}
