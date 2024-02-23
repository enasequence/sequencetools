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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class FeatureQualifierDuplicateValueFixTest {

  private Feature feature1, feature2;
  private Qualifier qualifier1, qualifier2, qualifier3;
  private FeatureQualifierDuplicateValueFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    feature1 = featureFactory.createFeature("feature1");
    qualifier1 = qualifierFactory.createQualifier("locus_tag");
    qualifier2 = qualifierFactory.createQualifier("old_locus_tag");
    qualifier3 = qualifierFactory.createQualifier("old_locus_tag");
    feature1.addQualifier(qualifier1);
    feature1.addQualifier(qualifier2);
    feature1.addQualifier(qualifier3);
    feature2 = featureFactory.createFeature("feature2");

    check = new FeatureQualifierDuplicateValueFix();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_noLocusTag() {
    feature2.addQualifier(qualifier1);
    assertTrue(check.check(feature2).isValid());
  }

  @Test
  public void testCheck_noOld_LocusTag() {
    feature2.addQualifier(qualifier2);
    assertTrue(check.check(feature2).isValid());
  }

  @Test
  public void testCheck_noLocusAndOldLocusTag() {
    assertTrue(check.check(feature2).isValid());
  }

  @Test
  public void testCheck_duplicateLocusAndOldLocus() {
    qualifier1.setValue("Dmel_CR18275");
    qualifier2.setValue("Dmel_CR18275");
    qualifier3.setValue("Dmel_CG18275");
    ValidationResult validationResult = check.check(feature1);
    assertEquals(1, validationResult.count("FeatureQualifierDuplicateValueFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_duplicateOldLocusTags() {
    qualifier1.setValue("Dmel_CG18275");
    qualifier2.setValue("Dmel_CR18275");
    qualifier3.setValue("Dmel_CR18275");
    ValidationResult validationResult = check.check(feature1);
    assertEquals(1, validationResult.count("FeatureQualifierDuplicateValueFix_2", Severity.FIX));
  }

  @Test
  public void testCheck_duplicateLocusAndOldLocusTags() {
    qualifier1.setValue("Dmel_CR18275");
    qualifier2.setValue("Dmel_CR18275");
    qualifier3.setValue("Dmel_CR18275");
    ValidationResult validationResult = check.check(feature1);
    assertEquals(1, validationResult.count("FeatureQualifierDuplicateValueFix_2", Severity.FIX));
    assertEquals(1, validationResult.count("FeatureQualifierDuplicateValueFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_noDuplicateLocusAndOldLocusTags() {
    qualifier1.setValue("Dmel_CR18275");
    qualifier2.setValue("Dmel_CG18275");
    qualifier3.setValue("Dmel_CF18275");
    ValidationResult validationResult = check.check(feature1);
    assertEquals(0, validationResult.count("FeatureQualifierDuplicateValueFix_2", Severity.FIX));
    assertEquals(0, validationResult.count("FeatureQualifierDuplicateValueFix_1", Severity.FIX));
  }
}
