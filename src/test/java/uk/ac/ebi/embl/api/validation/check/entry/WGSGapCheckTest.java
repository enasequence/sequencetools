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

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.validation.*;

public class WGSGapCheckTest {

  private Entry entry;
  private WGSGapCheck check;
  private FeatureFactory featureFactory;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);

    EntryFactory entryFactory = new EntryFactory();

    entry = entryFactory.createEntry();
    featureFactory = new FeatureFactory();
    check = new WGSGapCheck();
    check.init();
  }

  @Test
  public void testCheck_NoSequence() {
    entry.setSequence(null);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid()); // dont make a fuss, other checks for that
  }

  @Test
  public void testCheck_NoFeature() {
    entry.clearFeatures();
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_WGSDataClassNoGapFeatures() {
    entry.getFeatures().clear();
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertTrue(result.getMessages().isEmpty());
  }

  @Test
  public void testCheck_WGSDataClassGapFeature() {

    entry.setDataClass(Entry.WGS_DATACLASS);
    Feature feature1 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    ValidationResult validationResult = check.check(entry);

    assertTrue(validationResult.isValid());
    assertEquals(1, validationResult.count("WGSGapCheck", Severity.WARNING));
  }

  @Test
  public void testCheck_WGSDataClassMultipleGapFeatures() {
    entry.setDataClass(Entry.WGS_DATACLASS);
    Feature feature1 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    feature1.setOrigin(new TestOrigin());
    Feature feature2 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    feature2.setOrigin(new TestOrigin());

    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult validationResult = check.check(entry);
    Collection<ValidationMessage<Origin>> messages = validationResult.getMessages();
    assertEquals(1, messages.size());
    assertEquals(2, messages.iterator().next().getOrigins().size());
  }

  @Test
  public void testCheck_nonWGSDataClassGapFeatures() {
    entry.setDataClass(Entry.EST_DATACLASS);
    Feature feature1 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertTrue(validationResult.getMessages().isEmpty());
  }

  @Test
  public void testCheck_Message() {
    entry.setDataClass(Entry.WGS_DATACLASS);
    Feature feature1 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    feature1.setOrigin(new TestOrigin());
    Feature feature2 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    feature2.setOrigin(new TestOrigin());

    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult validationResult = check.check(entry);

    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("WGSGapCheck", Severity.WARNING);

    assertEquals(
        "Entries of type \"WGS\" must not contain \"gap\" features.",
        messages.iterator().next().getMessage());
  }
}
