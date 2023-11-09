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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class SequenceLengthCheckTest {

  private Entry entry;
  private Feature feature1, feature2;
  private SequenceFactory sequenceFactory;
  private FeatureFactory featureFactory;
  private EntryFactory entryFactory;
  private SequenceLengthCheck check;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    sequenceFactory = new SequenceFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    feature1 = featureFactory.createFeature(Feature.ncRNA_FEATURE_NAME);
    feature2 = featureFactory.createFeature(Feature.REPEAT_REGION);
    feature2.addQualifier(Qualifier.SATELLITE_QUALIFIER_NAME, "microsatellite: DC130");
    entry.setDataClass(Entry.EST_DATACLASS);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    check = new SequenceLengthCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoSequence() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_sequenceLessthanMin1() {
    entry.setSequence(sequenceFactory.createSequenceByte("attcttcatctcgtgctgtttt".getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages().size());
    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("SequenceLengthCheck", Severity.ERROR);
    assertEquals(
        "Sequence length must not be shorter than 100 bps for the entry \"null\".",
        messages.iterator().next().getMessage());
    assertEquals(
        "Sequence does not fall under the accepted categories (ancient DNA, non-coding-RNA, microsatellites or complete exons) and therefore can not be accepted for submission into ENA's EMBL-Bank.Exceptions require the submitter to demonstrate that a peer-reviewed journal has accepted a manuscript by the submitter, confirming the relevance of the short sequences to the scientific community. Please contact us if you can demonstrate this requirement or if your sequence belongs to the 'ancient DNA' or 'complete exon' category.",
        messages.iterator().next().getCuratorMessage());
  }

  @Test
  public void testCheck_sequenceLessthanMin2() {
    entry.setSequence(sequenceFactory.createSequenceByte("attcttcatctcgtgctgtttt".getBytes()));
    entry.addFeature(feature1);
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_sequenceLessthanMin3() {
    entry.setSequence(sequenceFactory.createSequenceByte("attcttcatctcgtgctgtttt".getBytes()));
    entry.addFeature(feature2);
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_sequenceGreaterthanMin1() {
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "attcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgtttt"
                .getBytes()));
    entry.addFeature(feature1);
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_sequenceGreaterthanMin2() {
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "attcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgtttt"
                .getBytes()));
    entry.addFeature(feature2);
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_sequenceGreaterthanMin3() {
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "attcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgttttattcttcatctcgtgctgtttt"
                .getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_sequenceLessThanMin_GSS_Dataclass() {
    entry.setSequence(sequenceFactory.createSequenceByte("attcttcatctcgtgctgtttt".getBytes()));
    entry.setDataClass(Entry.GSS_DATACLASS);
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_scope() {
    entry.setDataClass(Entry.PAT_DATACLASS);
    entry.setSequence(sequenceFactory.createSequenceByte("attcttcatctcgtgctgtttt".getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_dataclass_min() {
    entry.setDataClass(Entry.GSS_DATACLASS);
    entry.setSequence(sequenceFactory.createSequenceByte("attcttcat".getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.count("SequenceLengthCheck3", Severity.ERROR));
  }

  @Test
  public void testCheck_dataclass_max() {
    entry.setDataClass(Entry.GSS_DATACLASS);
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "attcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattc"
                .getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.count("SequenceLengthCheck3", Severity.ERROR));
  }

  @Test
  public void testCheck_dataclass_tsa_min() {
    entry.setDataClass(Entry.TSA_DATACLASS);
    entry.setSequence(
        sequenceFactory.createSequenceByte("attcttcaattcttcaattcttcaattcttcaattctt".getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.count("SequenceLengthCheck4", Severity.ERROR));
  }

  @Test
  public void testCheck_dataclass_tsa_max() {
    entry.setDataClass(Entry.TSA_DATACLASS);
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "attcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattc"
                .getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck4", Severity.ERROR));
  }

  @Test
  public void testCheck_LongNcRna_min() {
    feature1.addQualifier(Qualifier.NCRNA_CLASS_QUALIFIER_NAME, "lncRNA");
    entry.addFeature(feature1);
    entry.setSequence(
        sequenceFactory.createSequenceByte("aattcttcaattcttcaattcttcaattcttcaattctt".getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.count("SequenceLengthCheck5", Severity.WARNING));
  }

  @Test
  public void testCheck_LongNcRna_max() {
    feature1.addQualifier(Qualifier.NCRNA_CLASS_QUALIFIER_NAME, "lncRNA");
    entry.addFeature(feature1);
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "attcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattcttcaattc"
                .getBytes()));
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.count("SequenceLengthCheck5", Severity.WARNING));
  }
}
