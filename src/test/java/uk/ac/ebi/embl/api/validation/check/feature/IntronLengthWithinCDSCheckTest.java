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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class IntronLengthWithinCDSCheckTest {

  private FeatureFactory featureFactory;
  private LocationFactory locationFactory;
  private Feature feature;
  private IntronLengthWithinCDSCheck check;

  @Before
  public void setUp() {
    featureFactory = new FeatureFactory();
    locationFactory = new LocationFactory();
    check = new IntronLengthWithinCDSCheck();
    feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
  }

  @Test
  public void testCheck_NoFeature() {
    feature = null;
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_Validintron() {
    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(30L, 25L));
    locationJoin.addLocation(locationFactory.createLocalRange(50L, 100L));
    feature.setLocations(locationJoin);
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_ValidintronAssembly() throws SQLException {
    Join<Location> locationJoin = new Join<Location>();
    EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
    check.setEmblEntryValidationPlanProperty(property);
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(30L, 25L));
    locationJoin.addLocation(locationFactory.createLocalRange(50L, 100L));
    feature.setLocations(locationJoin);
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_InvalidIntronAssemblywithArtificialLocation() throws SQLException {
    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(19L, 25L));
    EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
    check.setEmblEntryValidationPlanProperty(property);
    feature.setLocations(locationJoin);
    feature.addQualifier("artificial_location", "low-quality sequence region");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
    assertEquals(0, validationResult.count("IntronLengthWithinCDSCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_InvalidIntronAssemblywithRibosomalSlippage() throws SQLException {
    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(19L, 25L));
    EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
    check.setEmblEntryValidationPlanProperty(property);
    feature.setLocations(locationJoin);
    feature.addQualifier("ribosomal_slippage", "low-quality sequence region");
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
    assertEquals(0, validationResult.count("IntronLengthWithinCDSCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_intronwithNegetiveValue() throws SQLException {
    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(9L, 25L));
    EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
    check.setEmblEntryValidationPlanProperty(property);
    feature.setLocations(locationJoin);
    ValidationResult validationResult = check.check(feature);
    assertTrue(validationResult.isValid());
    assertEquals(0, validationResult.count("IntronLengthWithinCDSCheck_1", Severity.ERROR));
  }
}
