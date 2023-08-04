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
package uk.ac.ebi.embl.api.validation.check.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class CircularRNAQualifierCheckTest {

  private Feature feature;
  private CircularRNAQualifierCheck check;

  @Before
  public void setUp() {
    check = new CircularRNAQualifierCheck();
  }

  @Test
  public void testInvalidQualForFeature() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("gene");
    feature.addQualifier("circular_RNA");
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("QualifierNotAllowedInFeatureCheck", Severity.ERROR));
  }

  @Test
  public void testValidQualFormRNAFeature() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("mRNA");
    feature.addQualifier("circular_RNA");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testValidQualForCDSFeature() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("CDS");
    feature.addQualifier("circular_RNA");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }
}
