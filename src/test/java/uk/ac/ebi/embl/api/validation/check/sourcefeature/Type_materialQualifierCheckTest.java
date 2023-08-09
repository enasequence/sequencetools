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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class Type_materialQualifierCheckTest {
  private Type_materialQualifierCheck check;
  private SourceFeature sourceFeature;
  private Feature non_sourceFeature;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();
    sourceFeature = featureFactory.createSourceFeature();
    sourceFeature.setScientificName("Cauliflower mosaic virus");
    non_sourceFeature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    check = new Type_materialQualifierCheck();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoSourceFeature() {
    assertTrue(check.check(non_sourceFeature).isValid());
  }

  @Test
  public void testCheck_NoType_material() {
    assertTrue(check.check(sourceFeature).isValid());
  }

  @Test
  public void testCheck_withInvalidTypeMaterialFormat() {

    sourceFeature.addQualifier(
        Qualifier.TYPE_MATERIAL_QUALIFIER_NAME, "type strain Cauliflower mosaic viru");
    assertTrue(check.check(sourceFeature).isValid());
  }

  @Test
  public void testCheck_withInvalidTypeMaterialorganism() {
    sourceFeature.addQualifier(
        Qualifier.TYPE_MATERIAL_QUALIFIER_NAME, "type strain of Cauliflower mosaic viru");
    ValidationResult result = check.check(sourceFeature);
    assertEquals(1, result.count("type_materialQualifierCheck1", Severity.ERROR));
  }

  @Test
  public void testCheck_withValidTypeMaterial() {
    sourceFeature.addQualifier(
        Qualifier.TYPE_MATERIAL_QUALIFIER_NAME, "type strain of Cauliflower mosaic virus");
    ValidationResult result = check.check(sourceFeature);
    assertTrue(result.isValid());
    assertEquals(0, result.count("type_materialQualifierCheck1", Severity.ERROR));
  }
}
