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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.ProteinIdQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class DuplicateFeatureCheckTest {

  private Entry entry;
  private DuplicateFeatureCheck check;
  public FeatureFactory featureFactory;
  public LocationFactory locationFactory;
  public QualifierFactory qualifierFactory;

  @Before
  public void setUp() {
    EntryFactory entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    locationFactory = new LocationFactory();
    qualifierFactory = new QualifierFactory();

    entry = entryFactory.createEntry();
    check = new DuplicateFeatureCheck();
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoFeatures() {
    entry.clearFeatures();
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheckSimpleDuplication() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    Feature feature = featureFactory.createFeature("featureName");
    feature.setLocations(locationJoin);

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L)); // same location
    Feature feature2 = featureFactory.createFeature("featureName"); // same name
    feature2.setLocations(locationJoin2);

    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(DuplicateFeatureCheck.DUPLICATE_FEATURE_LOCATIONS, Severity.ERROR));
  }

  @Test
  public void testCheckMultipleDuplication() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    Feature feature = featureFactory.createFeature("featureName");
    feature.setLocations(locationJoin);

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L)); // same location
    Feature feature2 = featureFactory.createFeature("featureName"); // same name
    feature2.setLocations(locationJoin2);

    Join<Location> locationJoin3 = new Join<Location>();
    locationJoin3.addLocation(locationFactory.createLocalRange(1L, 10L)); // same location
    Feature feature3 = featureFactory.createFeature("featureName"); // same name
    feature3.setLocations(locationJoin3);

    entry.addFeature(feature);
    entry.addFeature(feature2);
    entry.addFeature(feature3);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
    assertEquals(
        2,
        validationResult.count(DuplicateFeatureCheck.DUPLICATE_FEATURE_LOCATIONS, Severity.ERROR));
  }

  @Test
  public void testCheckSimpleNonDuplication() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    Feature feature = featureFactory.createFeature("featureName");
    feature.setLocations(locationJoin);

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L)); // same location
    Feature feature2 = featureFactory.createFeature("featureName2"); // different name
    feature2.setLocations(locationJoin2);

    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheckJoinDuplication() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(15L, 25L, true));
    Feature feature = featureFactory.createFeature("featureName");
    feature.setLocations(locationJoin);

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin2.addLocation(locationFactory.createLocalRange(15L, 25L, true));
    Feature feature2 = featureFactory.createFeature("featureName");
    feature2.setLocations(locationJoin2);

    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(DuplicateFeatureCheck.DUPLICATE_FEATURE_LOCATIONS, Severity.ERROR));
  }

  @Test
  public void testCheckJoinNonDuplication() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    locationJoin.addLocation(locationFactory.createLocalRange(15L, 25L, true));
    Feature feature = featureFactory.createFeature("featureName");
    feature.setLocations(locationJoin);

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L));
    // this one is not complement - so not a duplication
    locationJoin2.addLocation(locationFactory.createLocalRange(15L, 25L));
    Feature feature2 = featureFactory.createFeature("featureName");
    feature2.setLocations(locationJoin2);

    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheckCDSNoDuplication() {
    ProteinIdQualifier proteinQualifier1 = qualifierFactory.createProteinIdQualifier("CAA00031.1");
    ProteinIdQualifier proteinQualifier2 = qualifierFactory.createProteinIdQualifier("CAA00033.1");
    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature.setLocations(locationJoin);
    feature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.CODON_START_QUALIFIER_NAME, "5"));

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L));
    Feature feature2 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature2.setLocations(locationJoin2);
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.CODON_START_QUALIFIER_NAME, "6")); // different
    feature.addQualifier(proteinQualifier2);
    feature2.addQualifier(proteinQualifier1);
    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheckSourceDuplication1() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    SourceFeature feature = featureFactory.createSourceFeature();
    feature.setLocations(locationJoin);
    feature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "value"));

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L));
    SourceFeature feature2 = featureFactory.createSourceFeature();
    feature2.setLocations(locationJoin2);
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "value"));

    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(
            DuplicateFeatureCheck.DUPLICATE_SOURCE_ORGANISM_MESSAGE_ID, Severity.ERROR));
  }

  @Test
  public void testCheckSourceDuplication2() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    SourceFeature feature = featureFactory.createSourceFeature();
    feature.setLocations(locationJoin);
    feature.setTransgenic(true);

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L));
    SourceFeature feature2 = featureFactory.createSourceFeature();
    feature2.setLocations(locationJoin2);
    feature2.setTransgenic(false);
    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(
        0,
        validationResult.count(DuplicateFeatureCheck.DUPLICATE_FEATURE_LOCATIONS, Severity.ERROR));
  }

  @Test
  public void testCheckSourceNoDuplication() {

    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(1L, 10L));
    SourceFeature feature = featureFactory.createSourceFeature();
    feature.setLocations(locationJoin);
    feature.setTransgenic(true);

    Join<Location> locationJoin2 = new Join<Location>();
    locationJoin2.addLocation(locationFactory.createLocalRange(1L, 10L));
    SourceFeature feature2 = featureFactory.createSourceFeature();
    feature2.setLocations(locationJoin2);
    feature2.setTransgenic(true);

    entry.addFeature(feature);
    entry.addFeature(feature2);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }
}
