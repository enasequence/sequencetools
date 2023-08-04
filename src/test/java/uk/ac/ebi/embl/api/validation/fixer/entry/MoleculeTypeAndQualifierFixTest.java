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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class MoleculeTypeAndQualifierFixTest {

  private Entry entry;
  private MoleculeTypeAndQualifierFix check;
  public EntryFactory entryFactory;
  public FeatureFactory featureFactory;
  private QualifierFactory qualifierFactory;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();
    entry = entryFactory.createEntry();
    Sequence sequence =
        new SequenceFactory()
            .createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
    sequence.setMoleculeType("mRNA");
    entry.setSequence(sequence);
    DataRow dataRow1 = new DataRow("genomic DNA", Qualifier.GERMLINE_QUALIFIER_NAME);
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.SOURCE_QUALIFIERS_MOLTYPE_VALUES, dataRow1);
    check = new MoleculeTypeAndQualifierFix();
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  public void testCheck_Empty() {
    entry.setSequence(null);
    ValidationResult result = check.check(entry);
    assertTrue(result.getMessages(Severity.FIX).isEmpty());
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
  public void testCheck_NoMolType() {
    entry.getSequence().setMoleculeType(null);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_withInvalidMol_type() {
    Feature feature = featureFactory.createSourceFeature();
    feature.addQualifier(qualifierFactory.createQualifier(Qualifier.GERMLINE_QUALIFIER_NAME));
    entry.addFeature(feature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.getMessages(Severity.FIX).size() == 1);
    assertEquals(
        1, validationResult.getMessages("MoleculeTypeAndQualifierFix", Severity.FIX).size());
  }

  @Test
  public void testCheck_withvalidMol_type() {
    Feature feature = featureFactory.createSourceFeature();
    feature.addQualifier(qualifierFactory.createQualifier(Qualifier.GERMLINE_QUALIFIER_NAME));
    entry.getSequence().setMoleculeType("genomic DNA");
    entry.addFeature(feature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.getMessages(Severity.FIX).size() == 0);
    assertEquals(
        0, validationResult.getMessages("MoleculeTypeAndQualifierFix", Severity.FIX).size());
  }

  @Test
  public void testCheck_noQualifierwithMoltype() {
    Feature feature = featureFactory.createSourceFeature();
    feature.addQualifier(qualifierFactory.createQualifier(Qualifier.ANTICODON_QUALIFIER_NAME));
    entry.getSequence().setMoleculeType("genomic DNA");
    entry.addFeature(feature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.getMessages(Severity.FIX).size() == 0);
    assertEquals(
        0, validationResult.getMessages("MoleculeTypeAndQualifierFix", Severity.FIX).size());
  }
}
