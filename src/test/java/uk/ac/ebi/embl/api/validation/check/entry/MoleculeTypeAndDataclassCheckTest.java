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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;

public class MoleculeTypeAndDataclassCheckTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private MoleculeTypeAndDataclassCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry = entryFactory.createEntry();
    Sequence sequence = sequenceFactory.createSequence();
    entry.setSequence(sequence);

    check = new MoleculeTypeAndDataclassCheck();
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoDataclass() {
    entry.setDataClass(null);
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoMoleculeType() {
    entry.setDataClass(entry.STD_DATACLASS);
    entry.getSequence().setMoleculeType(null);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoSequence() {
    entry.setSequence(null);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_Valid() {
    entry.getSequence().setMoleculeType("rRNA");
    entry.setDataClass(entry.EST_DATACLASS);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_WrongDataclass1() {

    entry.setDataClass(entry.EST_DATACLASS);
    entry.getSequence().setMoleculeType("genomic DNA");
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("MoleculeTypeAndDataclassCheck-1", Severity.ERROR));
  }

  @Test
  public void testCheck_WrongDataclass2() {
    entry.getSequence().setMoleculeType("other DNA");
    entry.setDataClass(entry.GSS_DATACLASS);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("MoleculeTypeAndDataclassCheck-2", Severity.WARNING));
  }

  @Test
  public void testCheck_differentDataclass() {
    entry.setDataClass(entry.STD_DATACLASS);
    entry.getSequence().setMoleculeType("rRNA");
    assertTrue(check.check(entry).isValid());
  }
}
