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

public class LocusTagValueFixTest {

  private Feature feature;
  private Qualifier qualifier;
  private LocusTagValueFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    qualifier = qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
    check = new LocusTagValueFix();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_LocusTag() {
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("LocusTagValueFix-1", Severity.FIX));
  }

  @Test
  public void testCheck_locusTagValuenoUppercase() {
    qualifier.setValue("addffg_1234");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("LocusTagValueFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_locusTagValuenoAllUppercase() {
    qualifier.setValue("addFFg_1234");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("LocusTagValueFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_locusTagUppercase() {
    qualifier.setValue("ADDFG_1234");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("Linkage_evidenceFix_1", Severity.FIX));
  }
}
