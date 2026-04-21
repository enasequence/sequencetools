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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

public class MacronuclearQualifierFixTest {

  private FeatureFactory featureFactory;
  private MacronuclearQualifierFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    featureFactory = new FeatureFactory();
    check = new MacronuclearQualifierFix();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NonSourceFeature() {
    Feature feature = featureFactory.createFeature("gene");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_NoOrganelleQualifier() {
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Paramecium tetraurelia");
    assertTrue(check.check(source).isValid());
  }

  @Test
  public void testCheck_ConvertsOrganelleMacronuclearToMacronuclearQualifier() {
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "macronuclear");

    ValidationResult result = check.check(source);

    assertNull(source.getSingleQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME));
    assertNotNull(source.getSingleQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME));
    assertNull(source.getSingleQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME).getValue());
    assertEquals(1, result.count("MacronuclearQualifierFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_DoesNotDuplicateMacronuclearQualifier() {
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME);
    source.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "macronuclear");

    ValidationResult result = check.check(source);

    assertNull(source.getSingleQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME));
    assertEquals(1, source.getQualifiers(Qualifier.MACRONUCLEAR_QUALIFIER_NAME).size());
    assertEquals(1, result.count("MacronuclearQualifierFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_DoesNotChangeOtherOrganelleValues() {
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "mitochondrion");

    ValidationResult result = check.check(source);

    assertNotNull(source.getSingleQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME));
    assertEquals(
        "mitochondrion", source.getSingleQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME).getValue());
    assertNull(source.getSingleQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME));
    assertEquals(0, result.count("MacronuclearQualifierFix_1", Severity.FIX));
  }
}
