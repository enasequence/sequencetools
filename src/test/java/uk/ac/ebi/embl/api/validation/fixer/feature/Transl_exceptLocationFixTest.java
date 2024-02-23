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

public class Transl_exceptLocationFixTest {

  private Feature feature;
  private Qualifier qualifier;
  private Transl_exceptLocationFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    feature = featureFactory.createFeature("feature");
    qualifier = qualifierFactory.createQualifier("transl_except");
    feature.addQualifier(qualifier);
    check = new Transl_exceptLocationFix();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_validtransl_exceptValue() {
    qualifier.setValue("(pos:23..25,aa:Met)");
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("transl_exceptLocationFix", Severity.FIX));
  }

  // check for transl_except having complement location
  @Test
  public void testCheck_transl_exceptWithComplementLocationvalue() {
    qualifier.setValue("(pos:complement(23..25),aa:Met)");
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("transl_exceptLocationFix", Severity.FIX));
  }

  @Test
  public void testCheck_Notransl_exceptQualifier() {
    feature.removeQualifier(qualifier);
    assertTrue(check.check(feature).isValid());
  }
}
