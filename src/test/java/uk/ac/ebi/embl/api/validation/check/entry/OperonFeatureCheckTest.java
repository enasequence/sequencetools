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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class OperonFeatureCheckTest {

  private OperonFeatureCheck check;
  QualifierFactory qualifierFactory;
  FeatureFactory featureFactory;
  EntryFactory entryFactory;
  Entry entry;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    check = new OperonFeatureCheck();
    qualifierFactory = new QualifierFactory();
    featureFactory = new FeatureFactory();
    entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
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
  public void testCheck_NoQualifiers() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_withNooperonFeatureandQualifier() {
    Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature.addQualifier(Qualifier.PSEUDO_QUALIFIER_NAME);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_Organism_withOperonFeatureandQualifier() {
    Feature feature = featureFactory.createFeature(Feature.OPERON_FEATURE_NAME);
    feature.addQualifier(Qualifier.OPERON_QUALIFIER_NAME);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_Organism_withSingleOperonQualifierandNotFeature() {
    Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature.addQualifier(Qualifier.OPERON_QUALIFIER_NAME);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("OperonFeatureCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_Organism_withMultipleOperonQualifierandNotFeature1() {
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature1.addQualifier(Qualifier.OPERON_QUALIFIER_NAME, "ab");
    Feature feature2 = featureFactory.createFeature(Feature.VARIATION_FEATURE);
    feature2.addQualifier(Qualifier.OPERON_QUALIFIER_NAME, "abc");
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(2, result.count("OperonFeatureCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_Organism_withMultipleOperonQualifierandNotFeature2() {
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature1.addQualifier(Qualifier.OPERON_QUALIFIER_NAME, "ab");
    Feature feature2 = featureFactory.createFeature(Feature.VARIATION_FEATURE);
    feature2.addQualifier(Qualifier.OPERON_QUALIFIER_NAME, "ab");
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("OperonFeatureCheck_2", Severity.ERROR));
  }
}
