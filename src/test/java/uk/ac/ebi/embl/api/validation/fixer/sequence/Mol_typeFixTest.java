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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class Mol_typeFixTest {

  private Entry entry;
  private Mol_typeFix check;
  public EntryFactory entryFactory;
  public FeatureFactory featureFactory;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    Sequence sequence =
        new SequenceFactory()
            .createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
    sequence.setMoleculeType("mRNA");
    entry.setSequence(sequence);

    check = new Mol_typeFix();
  }

  public void testCheck_Empty() {
    entry.setSequence(null);
    ValidationResult result = check.check(entry);
    assertTrue(result.getMessages(Severity.FIX).isEmpty()); // dont make a
    // fuss, other
    // checks for
    // that
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoFeatures() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoDataclass() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoAssembly_Gap() {
    entry.setDataClass(Entry.TSA_DATACLASS);
    Feature feature = featureFactory.createFeature("CDS");
    entry.addFeature(feature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
  }

  @Test
  public void testCheck_NoTSA() {
    entry.setDataClass(Entry.CON_DATACLASS);
    Feature feature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    entry.addFeature(feature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
  }

  @Test
  public void testCheck_Assembly_gap_TSA() {
    entry.setDataClass(Entry.TSA_DATACLASS);
    Feature feature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    entry.addFeature(feature);
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
  }
}
