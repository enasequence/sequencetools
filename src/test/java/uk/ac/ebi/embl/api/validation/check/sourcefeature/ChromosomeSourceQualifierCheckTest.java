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
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class ChromosomeSourceQualifierCheckTest {

  private FeatureFactory featureFactory;
  private ChromosomeSourceQualifierCheck check;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    featureFactory = new FeatureFactory();
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    check = new ChromosomeSourceQualifierCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoFeature() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NonSourceFeature() throws ValidationEngineException {
    Feature feature = featureFactory.createFeature("gene");
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_MacronuclearQualifierIsAcceptedAsSingleChromosomeQualifier()
      throws ValidationEngineException {
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME);

    ValidationResult result = check.check(source);

    assertEquals(0, result.count("ChromosomeSourceQualiferCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_MacronuclearAndOrganelleQualifiersFailAsMultipleChromosomeQualifiers()
      throws ValidationEngineException {
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME);
    source.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "mitochondrion");

    ValidationResult result = check.check(source);

    assertEquals(1, result.count("ChromosomeSourceQualiferCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_NotAssemblyChromosomeScope() throws ValidationEngineException {
    property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
    check.setEmblEntryValidationPlanProperty(property);

    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(Qualifier.MACRONUCLEAR_QUALIFIER_NAME);
    source.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "mitochondrion");

    ValidationResult result = check.check(source);

    assertTrue(result.isValid());
    assertEquals(0, result.count("ChromosomeSourceQualiferCheck_1", Severity.ERROR));
  }
}
