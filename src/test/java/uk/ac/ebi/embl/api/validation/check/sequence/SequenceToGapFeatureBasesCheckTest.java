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

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class SequenceToGapFeatureBasesCheckTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private LocationFactory locationFactory;
  private QualifierFactory qualifierFactory;
  private SequenceToGapFeatureBasesCheck check;
  private SequenceFactory sequenceFactory;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    sequenceFactory = new SequenceFactory();
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();
    locationFactory = new LocationFactory();
    entry = entryFactory.createEntry();
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    check = new SequenceToGapFeatureBasesCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoEntry() {
    ValidationResult result = check.check(null);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_NoFeatures() {
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_NoSequence() {
    entry.setSequence(null);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_NoLocations() {
    Feature feature1 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Feature feature2 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_WarningNSequence() {

    entry.setSequence(
        sequenceFactory.createSequenceByte("aaannnnnnaaa".getBytes())); // 6 n's = warning

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.WARNING));
  }

  @Test
  public void testCheck_ErrorNSequence() {

    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "aaannnnnnnnnnnnnnnnnnnnnnnnaaa".getBytes())); // 8 n's = error

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
  }

  @Test
  public void testCheck_MultipleErrorNSequence() {

    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "aaannnnnnnnaaannnnnnnnnnnnnnnnnnnnnnnnnnnnnnnaaaa"
                .getBytes())); // 2 lots of n stretches

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.WARNING));
  }

  @Test
  public void testCheck_BadLocationMatch() {

    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Order<Location> location = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(1L, 5L));
    feature.setLocations(location);

    Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> location1 = new Order<Location>();
    location1.addLocation(locationFactory.createLocalRange(4L, 10L));
    feature1.setLocations(location1);

    entry.addFeature(feature);
    entry.addFeature(feature1);
    entry.setSequence(
        sequenceFactory.createSequenceByte("aaannnnnnnnnnnnnnnnnnnnnnnaaaaaaa".getBytes()));

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
  }

  @Test
  public void testCheck_GoodLocationMatch() {

    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Order<Location> location = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(4L, 11L));
    feature.setLocations(location);
    Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> location1 = new Order<Location>();
    location1.addLocation(locationFactory.createLocalRange(4L, 11L));
    feature1.setLocations(location1);

    entry.addFeature(feature1);
    entry.addFeature(feature);
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "aaannnnnnnnaaaaaaa".getBytes())); // 2 lots of n stretches

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count());
  }

  @Test
  public void testCheck_MultipleGoodLocationMatch() {

    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Order<Location> location = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(4L, 11L));
    feature.setLocations(location);
    entry.addFeature(feature);

    Feature feature2 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> location2 = new Order<Location>();
    location2.addLocation(locationFactory.createLocalRange(16L, 20L));
    feature2.setLocations(location2);
    entry.addFeature(feature2);

    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "aaannnnnnnnaaaannnnnaaa".getBytes())); // 2 lots of n stretches

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count());
  }

  @Test
  public void testCheck_wgscon_WarningNSequence() {
    entry.setDataClass(Entry.WGS_DATACLASS);
    entry.setSequence(sequenceFactory.createSequenceByte("aaannaaa".getBytes()));
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.WARNING));
  }

  @Test
  public void testCheck_wgscon_ErrorNSequence() {
    entry.setDataClass(Entry.WGS_DATACLASS);
    entry.setSequence(sequenceFactory.createSequenceByte("aaannnnnnnnnnnaaa".getBytes()));
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
  }

  @Test
  public void testCheck_wgscon_NSequencewithGapfeature() {
    entry.setDataClass(Entry.CON_DATACLASS);
    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Order<Location> location = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(4L, 24L));
    feature.setLocations(location);
    entry.addFeature(feature);

    Feature feature2 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> location2 = new Order<Location>();
    location2.addLocation(locationFactory.createLocalRange(29L, 33L));
    feature2.setLocations(location2);
    entry.addFeature(feature2);

    entry.setSequence(
        sequenceFactory.createSequenceByte("aaannnnnnnnnnnnnnnnnnnnnaaaannnnnaaa".getBytes()));
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count());
  }

  @Test
  public void testCheck_WGS_with_min_gap_length() throws SQLException {
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    property.options.minGapLength = 5;
    check.setEmblEntryValidationPlanProperty(property);
    entry.setDataClass(Entry.WGS_DATACLASS);
    entry.setSequence(
        sequenceFactory.createSequenceByte("aaannnnnnnnnnnnnnnnnnnnnaaaannnnnaaa".getBytes()));
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
    assertEquals(5, SequenceToGapFeatureBasesCheck.ERROR_THRESHOLD);
  }

  @Test
  public void testCheck_non_WGS_with_min_gap_length() throws SQLException {
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    property.options.minGapLength = 5;
    check.setEmblEntryValidationPlanProperty(property);
    entry.setDataClass(Entry.STD_DATACLASS);
    entry.setSequence(
        sequenceFactory.createSequenceByte("aaannnnnnnnnnnnnnnnnnnnnaaaannnnnaaa".getBytes()));
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
    assertEquals(5, SequenceToGapFeatureBasesCheck.ERROR_THRESHOLD);
  }

  @Test
  public void testCheckOnlyNSequence() {

    entry.setSequence(sequenceFactory.createSequenceByte("nnnnnnnnnnnnnnnnnnnn".getBytes()));

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(
        1, result.count(SequenceToGapFeatureBasesCheck.MESSAGE_SEQ_WITH_MORE_N, Severity.ERROR));
  }
}
