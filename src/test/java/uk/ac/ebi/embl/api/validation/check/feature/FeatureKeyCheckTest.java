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

import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 26-Jan-2009 Time: 10:50:52 To change this template
 * use File | Settings | File Templates.
 */
public class FeatureKeyCheckTest {

  private Feature feature;
  private FeatureKeyCheck check;
  private EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() throws SQLException {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("misc_feature");
    property = TestHelper.testEmblEntryValidationPlanProperty();

    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.FEATURE_KEYS,
        new DataRow("misc_feature"),
        new DataRow("operon"),
        new DataRow("CDS"),
        new DataRow("intron"));
    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.FEATURE_KEY_QUALIFIERS,
        new DataRow("misc_feature", "gene", "N", "Y", "Y"),
        new DataRow("operon", "operon", "Y", "N", "N"),
        new DataRow("intron", "number", "N", "Y", "Y"),
        new DataRow("intron", "gene", "N", "Y", "Y"));
    check = new FeatureKeyCheck();

    check.setEmblEntryValidationPlanProperty(property);
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoQualifiers() {
    assertTrue(check.check(feature).isValid());
  }

  @Test
  public void testCheck_InvalidKey() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("meaty_pipe");
    ValidationResult validationResult = check.check(feature);
    assertEquals(1, validationResult.count("FeatureKeyCheck-1", Severity.ERROR));
  }

  @Test
  public void testCheck_NoMandatoryQualifier() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("operon");
    feature.addQualifier("gene", "gene name");
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    System.out.println(
        ((ValidationMessage) ((ArrayList) validationResult.getMessages("FeatureKeyCheck-2")).get(0))
            .getCuratorMessage());
    assertEquals(1, validationResult.count("FeatureKeyCheck-2", Severity.ERROR));
  }

  @Test
  public void testCheck_NoPermittedQualifier() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("operon");
    feature.addQualifier("gene", "gene name");
    feature.addQualifier("operon", "");
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    System.out.println(
        ((ValidationMessage) ((ArrayList) validationResult.getMessages("FeatureKeyCheck-7")).get(0))
            .getCuratorMessage());
    assertEquals(1, validationResult.count("FeatureKeyCheck-7", Severity.ERROR));
  }

  @Test
  public void testCheck_NoRecomendedQualifier() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("misc_feature");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
    assertEquals(1, validationResult.count("FeatureKeyCheck-4", Severity.WARNING));
  }

  @Test
  public void testCheck_NoRecomendedQualifier_intron() throws SQLException {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
    feature.addQualifier("gene", "ilvE");
    property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
    assertEquals(1, validationResult.count("FeatureKeyCheck-6", Severity.WARNING));
  }

  @Test
  public void testCheck_TooManyQualifiers() {
    feature.addQualifier("gene");
    feature.addQualifier("gene");
    ValidationResult validationResult = check.check(feature);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count("FeatureKeyCheck-3", Severity.ERROR));
  }

  @Test
  public void testCheck_CDSCodonStart() {
    // throws error if location is left partial and there is no codon_start qualifier
    FeatureFactory featureFactory = new FeatureFactory();
    Feature cdsFeature = featureFactory.createFeature("CDS");

    LocationFactory locationFactory = new LocationFactory();
    LocalRange location = locationFactory.createLocalRange(1L, 3L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    join.setLeftPartial(true);
    cdsFeature.setLocations(join);

    ValidationResult validationResult = check.check(cdsFeature);

    assertTrue(validationResult.isValid());
    assertEquals(1, validationResult.count("FeatureKeyCheck-5", Severity.WARNING));

    // if right partial we are okay
    join.setLeftPartial(false);
    join.setRightPartial(true);
    validationResult = check.check(cdsFeature);

    assertTrue(validationResult.isValid());
    assertEquals(0, validationResult.count("FeatureKeyCheck-5", Severity.WARNING));
  }

  @Test
  public void testCheck_Fine() {
    FeatureFactory featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature("operon");
    feature.addQualifier("operon");
    feature.addQualifier("operon");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }
}
