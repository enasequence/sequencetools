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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;

public class GapFeatureBasesCheckTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private LocationFactory locationFactory;
  private QualifierFactory qualifierFactory;
  private GapFeatureBasesCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequence();
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();
    locationFactory = new LocationFactory();
    entry = entryFactory.createEntry();
    ByteBuffer sequenceBuffer =
        ByteBuffer.wrap("agagagagagagannnnnnnagagagagagagagagagagagag".getBytes());
    entry.setSequence(sequence);
    entry.getSequence().setSequence(sequenceBuffer);
    //
    // entry.setSequence(sequenceFactory.createSequence("agagagagagagannnnnnnagagagagagagagagagagagag"));
    check = new GapFeatureBasesCheck();
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
    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
    Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    ValidationResult result1 = check.check(entry);
    assertTrue(result1.isValid());
    assertEquals(0, result1.getMessages().size());
  }

  @Test
  public void testCheck_BadGapSequence() {

    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Order<Location> location = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(1L, 5L));
    feature.setLocations(location);

    entry.addFeature(feature);

    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count(GapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
    Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> location1 = new Order<Location>();
    location1.addLocation(locationFactory.createLocalRange(1L, 5L));
    feature1.setLocations(location1);
    entry.removeFeature(feature);
    entry.addFeature(feature1);

    ValidationResult result1 = check.check(entry);
    assertFalse(result1.isValid());
    assertEquals(1, result1.count(GapFeatureBasesCheck.MESSAGE_ID, Severity.ERROR));
  }

  @Test
  public void testCheck_GoodGapSequence() {

    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Order<Location> location = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(14L, 20L));
    feature.setLocations(location);

    entry.addFeature(feature);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> location1 = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(13L, 20L));
    feature.setLocations(location1);
    entry.removeFeature(feature);
    entry.addFeature(feature1);
    ValidationResult result1 = check.check(entry);
    assertTrue(result1.isValid());
  }

  @Test
  public void testCheck_ComplexLocation() {

    /**
     * this should bail out quietly if the location on the gap feature is complex - other checks to
     * tell submitters off for this.
     */
    Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    Order<Location> location = new Order<Location>();
    location.addLocation(locationFactory.createLocalRange(13L, 20L));
    location.addLocation(locationFactory.createLocalRange(22L, 29L));
    feature.setLocations(location);

    entry.addFeature(feature);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> location1 = new Order<Location>();
    location1.addLocation(locationFactory.createLocalRange(13L, 20L));
    location1.addLocation(locationFactory.createLocalRange(22L, 29L));
    feature1.setLocations(location1);
    entry.removeFeature(feature);
    entry.addFeature(feature);
    ValidationResult result1 = check.check(entry);
    assertTrue(result1.isValid());
  }

  @Test
  public void testCheck_MultipleLocation() {

    test("aaanaaa", getLocationOrder(List.of(getLocation(4, 4))), true);
    test("aaannnnaaa", getLocationOrder(List.of(getLocation(4, 7))), true);
    test(
        "aaannnnaannnnnnaaannnn",
        getLocationOrder(List.of(getLocation(4, 7), getLocation(10, 15), getLocation(19, 22))),
        true);
    test("anaaa", getLocationOrder(List.of(getLocation(1, 1))), false);
    test("aaaaaannnnaaaa", getLocationOrder(List.of(getLocation(7, 10))), true);
    test("annaannnaa", getLocationOrder(List.of(getLocation(2, 3), getLocation(6, 8))), true);
    test("annaannnaa", getLocationOrder(List.of(getLocation(2, 3), getLocation(6, 9))), false);
  }

  private void test(String sequenceString, Order<Location> locations, boolean valid) {
    Entry testEntry = getEntry(sequenceString);
    for (Location location : locations.getLocations()) {
      Order<Location> locationOrder = new Order<>();
      locationOrder.addLocation(location);
      Feature feature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
      feature.setLocations(locationOrder);
      testEntry.addFeature(feature);
    }

    if (valid) {
      assertTrue(check.check(testEntry).isValid());
    } else {
      ValidationResult result = check.check(testEntry);
      assertFalse(result.isValid());
      assertTrue(
          ((ValidationMessage<Origin>) ((List) result.getMessages()).get(0))
              .getMessage()
              .contains(
                  "\"gap\" or \"assembly_gap\" features must span a set of bases that are only \"n\""));
    }
  }

  private Entry getEntry(String sequenceStr) {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequence();
    ByteBuffer sequenceBuffer = ByteBuffer.wrap(sequenceStr.getBytes());
    EntryFactory entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    entry.setSequence(sequence);
    entry.getSequence().setSequence(sequenceBuffer);
    return entry;
  }

  private Location getLocation(long begin, long end) {
    return locationFactory.createLocalRange(begin, end);
  }

  private Order<Location> getLocationOrder(List<Location> locationList) {
    Order<Location> locationOrder = new Order<Location>();
    locationList.forEach(
        location -> {
          locationOrder.addLocation(
              locationFactory.createLocalRange(
                  location.getBeginPosition(), location.getEndPosition()));
        });

    return locationOrder;
  }
}
