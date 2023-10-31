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

import java.sql.SQLException;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class GaptoAssemblyGapFeatureFixTest {
  private EntryFactory entryFactory;
  private FeatureFactory featureFactory;
  private GaptoAssemblyGapFeatureFix check;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() throws Exception {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    entryFactory = new EntryFactory();
    new SequenceFactory();
    featureFactory = new FeatureFactory();
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check = new GaptoAssemblyGapFeatureFix();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoEntry() {
    ValidationResult result = check.check(null);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  public void testCheck_NoFeatures() {
    Entry entry = entryFactory.createEntry();
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withnoAssemblyGapandGapFeatures() {
    Entry entry = entryFactory.createEntry();
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    entry.addFeature(feature1);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_withNoAssemblyGapFeatures() {
    Entry entry = entryFactory.createEntry();
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Feature feature2 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(
        1, SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry).size());
    assertEquals(0, SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry).size());
    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("GaptoAssemblyGapFeatureFix_1", Severity.FIX);

    assertEquals(1, messages.size());
  }

  @Test
  public void testCheck_withNoAssemblyScope() throws SQLException {
    Entry entry = entryFactory.createEntry();
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Feature feature2 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    property.validationScope.set(ValidationScope.EMBL);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(
        0, SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry).size());
    assertEquals(1, SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry).size());
    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("GaptoAssemblyGapFeatureFix_1", Severity.FIX);

    assertEquals(0, messages.size());
  }

  @Test
  public void testCheck_withNoGapFeatures() {
    Entry entry = entryFactory.createEntry();
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Feature feature2 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(
        1, SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry).size());
    assertEquals(0, SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry).size());
    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("GaptoAssemblyGapFeatureFix_1", Severity.FIX);

    assertEquals(0, messages.size());
  }

  @Test
  public void testCheck_withAssemblyGapandGapFeatures() {
    Entry entry = entryFactory.createEntry();
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Feature feature2 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Feature feature3 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    entry.addFeature(feature3);

    ValidationResult validationResult = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        validationResult.getMessages("GaptoAssemblyGapFeatureFix_1", Severity.FIX);

    assertEquals(1, messages.size());
    assertEquals(
        "GAP feature replaced with ASSEMBLY_GAP feature as entry has both gap and assembly_gap features. gap and assembly_gap features are mutually exclusive.",
        messages.iterator().next().getMessage());
  }
}
