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
package uk.ac.ebi.embl.api.validation.check.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class TranslExceptQualifierCheckTest {

  private FeatureFactory featureFactory;
  private QualifierFactory qualifierFactory;
  private LocationFactory locationFactory;

  private Feature feature;
  private Qualifier qualifier;
  private TranslExceptQualifierCheck check;

  @Before
  public void setUp() {
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();
    locationFactory = new LocationFactory();
    check = new TranslExceptQualifierCheck();
    feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(15L, 25L));
    locationJoin.addLocation(locationFactory.createLocalRange(30L, 40L));
    feature.setLocations(locationJoin);
    qualifier = qualifierFactory.createQualifier(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME);
  }

  @Test
  public void testCheck_NoFeature() {
    feature = null;
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_NoTranslExcept() {
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_TranslExceptPositionRange() {
    qualifier.setValue("(pos:219444..219446,aa:His)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_TranslExceptPositionGap() {
    qualifier.setValue("(pos:219444..219448,aa:His)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_4", Severity.ERROR));
  }

  @Test
  public void testCheck_TranslExceptPositionOrder() {
    qualifier.setValue("(pos:219445..219444,aa:His)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_2", Severity.ERROR));
  }

  @Test
  public void testCheck_TranslExceptStartPosition() {
    qualifier.setValue("(pos:0..2,aa:His)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_3", Severity.ERROR));
  }

  @Test
  public void testCheck_TranslExceptAminoAcid() {
    qualifier.setValue("(pos:0..2,aa:Hi)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_5", Severity.ERROR));
  }

  @Test
  public void testCheck_TranslExceptValid() {
    qualifier.setValue("(pos:25..27,aa:His)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_TranslExceptcomplementPositionRange() {
    qualifier.setValue("(pos:complement(219444..219446),aa:His)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_TranslExceptJoinPositionRange() {
    qualifier.setValue("(pos:join(219444..219444,219445..219446),aa:His)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_TranslExceptInvalidCaseAminoAcid() {
    qualifier.setValue("(pos:25..27,aa:HIS)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_6", Severity.WARNING));
  }

  @Test
  public void testCheck_TranslExceptInvalidAminoAcid() {
    qualifier.setValue("(pos:25..27,aa:HIC)");
    feature.addQualifier(qualifier);
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("TranslExceptQualifierCheck_5", Severity.ERROR));
  }
}
