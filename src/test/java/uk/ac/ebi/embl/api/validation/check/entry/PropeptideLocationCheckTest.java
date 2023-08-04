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
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.validation.*;

public class PropeptideLocationCheckTest {

  private PropeptideLocationCheck check;
  FeatureFactory featureFactory;
  EntryFactory entryFactory;
  LocationFactory locationFactory;
  Entry entry;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    check = new PropeptideLocationCheck();
    featureFactory = new FeatureFactory();
    entryFactory = new EntryFactory();
    locationFactory = new LocationFactory();
    entry = entryFactory.createEntry();
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoFeatures() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoPropeptideFeatures() {
    entry.addFeature(featureFactory.createFeature(Feature.CDS_FEATURE_NAME));
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_withInvalidPropeptideFeature() {
    Feature feature = featureFactory.createFeature(Feature.PROPETIDE_FEATURE_NAME);
    LocalRange location = locationFactory.createLocalRange(1L, 13L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    feature.setLocations(join);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("PropeptideLocationCheck1", Severity.ERROR));
  }

  @Test
  public void testCheck_withvalidPropeptideFeature() {
    Feature feature = featureFactory.createFeature(Feature.PROPETIDE_FEATURE_NAME);
    LocalRange location = locationFactory.createLocalRange(1L, 12L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    feature.setLocations(join);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("PropeptideLocationCheck1", Severity.ERROR));
  }

  @Test
  public void testCheck_withInvalidPropeptideandCDSFeature() {
    Feature propetide_feature = featureFactory.createFeature(Feature.PROPETIDE_FEATURE_NAME);
    Feature cds_feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    LocalRange location = locationFactory.createLocalRange(1L, 12L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    propetide_feature.setLocations(join);
    entry.addFeature(propetide_feature);
    LocalRange cdsLocation = locationFactory.createLocalRange(1L, 10L);
    CompoundLocation<Location> cdsJoin = new Join<Location>();
    cdsJoin.addLocation(cdsLocation);
    cds_feature.setLocations(cdsJoin);
    entry.addFeature(cds_feature);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("PropeptideLocationCheck2", Severity.ERROR));
  }

  @Test
  public void testCheck_withvalidPropeptideandCDSFeature() {
    Feature propetide_feature = featureFactory.createFeature(Feature.PROPETIDE_FEATURE_NAME);
    Feature cds_feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    LocalRange location = locationFactory.createLocalRange(1L, 12L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    propetide_feature.setLocations(join);
    entry.addFeature(propetide_feature);
    LocalRange cdsLocation = locationFactory.createLocalRange(1L, 13L);
    CompoundLocation<Location> cdsJoin = new Join<Location>();
    cdsJoin.addLocation(cdsLocation);
    cds_feature.setLocations(cdsJoin);
    entry.addFeature(cds_feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("PropeptideLocationCheck2", Severity.ERROR));
  }

  @Test
  public void testCheck_withInvalidPropeptideandpeptideFeature() {
    Feature propetide_feature = featureFactory.createFeature(Feature.PROPETIDE_FEATURE_NAME);
    Feature cds_feature = featureFactory.createFeature(Feature.SIG_PEPTIDE_FEATURE_NAME);
    LocalRange location = locationFactory.createLocalRange(1L, 12L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    propetide_feature.setLocations(join);
    entry.addFeature(propetide_feature);
    LocalRange cdsLocation = locationFactory.createLocalRange(1L, 13L);
    CompoundLocation<Location> cdsJoin = new Join<Location>();
    cdsJoin.addLocation(cdsLocation);
    cds_feature.setLocations(cdsJoin);
    entry.addFeature(cds_feature);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("PropeptideLocationCheck3", Severity.ERROR));
  }

  @Test
  public void testCheck_withvalidPropeptideandpeptideFeature() {
    Feature propetide_feature = featureFactory.createFeature(Feature.PROPETIDE_FEATURE_NAME);
    Feature cds_feature = featureFactory.createFeature(Feature.SIG_PEPTIDE_FEATURE_NAME);
    LocalRange location = locationFactory.createLocalRange(1L, 12L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    propetide_feature.setLocations(join);
    entry.addFeature(propetide_feature);
    LocalRange cdsLocation = locationFactory.createLocalRange(13L, 23L);
    CompoundLocation<Location> cdsJoin = new Join<Location>();
    cdsJoin.addLocation(cdsLocation);
    cds_feature.setLocations(cdsJoin);
    entry.addFeature(cds_feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("PropeptideLocationCheck3", Severity.ERROR));
  }
}
