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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class CollectionDateQualifierFixTest {

  private Feature feature;
  private Qualifier qualifier;
  private CollectionDateQualifierFix check;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() throws Exception {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    feature = featureFactory.createFeature("feature");
    qualifier = qualifierFactory.createQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
    feature.addQualifier(qualifier);
    property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(ValidationScope.EMBL);
    check = new CollectionDateQualifierFix();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoQualifier() {

    FeatureFactory featureFactory = new FeatureFactory();
    Feature feature1 = featureFactory.createFeature("feature1");
    assertTrue(check.check(feature1).isValid());
  }

  @Test
  public void testCheck_validcollectionDateValue() {
    qualifier.setValue("11-Jun-2012");
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("CollectionDateQualifierFix_ID_1", Severity.FIX));
  }

  @Test
  public void testCheck_invalidCollectionDateValue() {
    qualifier.setValue("1-Jun-2012");
    ValidationResult validationResult = check.check(feature);
    assertEquals(qualifier.getValue(), "01-Jun-2012");
    assertEquals(1, validationResult.count("CollectionDateQualifierFix_1", Severity.FIX));
  }

  @Test
  public void testCheck_CollectionDateValueFormat() {
    qualifier.setValue("1-04-2012");
    ValidationResult validationResult = check.check(feature);
    assertEquals(0, validationResult.count("CollectionDateQualifierFix_1", Severity.FIX));
  }

  @Test
  public void testNCBICollectionDateFormatDDMmmYY() throws Exception {
    property.validationScope.set(ValidationScope.NCBI);
    check = new CollectionDateQualifierFix();
    check.setEmblEntryValidationPlanProperty(property);
    qualifier.setValue("11-Oct-12");
    ValidationResult validationResult = check.check(feature);
    List<Qualifier> collectionDateQualifiers =
        feature.getQualifiers(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
    String fixedDate = collectionDateQualifiers.get(0).getValue();
    assertEquals("11-Oct-2012", fixedDate);
    assertEquals(1, validationResult.count("CollectionDateQualifierFix_1", Severity.FIX));
  }

  @Test
  public void testNCBICollectionDateFormatDMmmYY() throws Exception {
    property.validationScope.set(ValidationScope.NCBI);
    check = new CollectionDateQualifierFix();
    check.setEmblEntryValidationPlanProperty(property);
    qualifier.setValue("1-Oct-12");
    ValidationResult validationResult = check.check(feature);
    List<Qualifier> collectionDateQualifiers =
        feature.getQualifiers(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
    String fixedDate = collectionDateQualifiers.get(0).getValue();
    assertEquals("01-Oct-2012", fixedDate);
    assertEquals(1, validationResult.count("CollectionDateQualifierFix_1", Severity.FIX));
  }

  @Test
  public void testEMBLCollectionDateFormatDDMmmYY() throws Exception {
    check = new CollectionDateQualifierFix();
    check.setEmblEntryValidationPlanProperty(property);
    qualifier.setValue("11-Oct-12");
    ValidationResult validationResult = check.check(feature);
    List<Qualifier> collectionDateQualifiers =
        feature.getQualifiers(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
    String fixedDate = collectionDateQualifiers.get(0).getValue();
    assertEquals("11-Oct-12", fixedDate);
    assertEquals(0, validationResult.count("CollectionDateQualifierFix_1", Severity.FIX));
  }
}
