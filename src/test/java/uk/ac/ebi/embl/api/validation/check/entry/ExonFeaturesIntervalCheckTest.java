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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class ExonFeaturesIntervalCheckTest {

  private ExonFeaturesIntervalCheck check;
  FeatureFactory featureFactory;
  EntryFactory entryFactory;
  LocationFactory locationFactory;
  Entry entry;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    check = new ExonFeaturesIntervalCheck();
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
  public void testCheck_NoQualifiers() {
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_withSingleExonFeatureSimpleLocation() {
    Feature feature = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location = locationFactory.createLocalRange(1L, 3L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    feature.setLocations(join);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withSingleExonFeatureRemoteLocation() {
    Feature feature = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    RemoteRange location = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    CompoundLocation<Location> join = new Join<Location>();
    join.addLocation(location);
    feature.setLocations(join);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesnotAdjacent() {
    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);
    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location2 = locationFactory.createLocalRange(30L, 33L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesnotAdjacentwithPartialLocation() {
    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);
    feature1.getLocations().setThreePrime(true);
    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location2 = locationFactory.createLocalRange(30L, 33L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    feature2.getLocations().setFivePrime(true);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesnotAdjacentwithRemoteLocation() {
    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    RemoteRange location1 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    RemoteRange location2 = locationFactory.createRemoteRange("A00001", 1, 24L, 26L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesAdjacent() {
    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);
    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesAdjacentDiffGene() {
    QualifierFactory qualFact = new QualifierFactory();

    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature1.addQualifier(qualFact.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "abcd"));
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);

    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);

    feature2.addQualifier(qualFact.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gett"));
    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesAdjacentSameGene() {
    QualifierFactory qualFact = new QualifierFactory();

    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature1.addQualifier(qualFact.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gett"));
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);

    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);

    feature2.addQualifier(qualFact.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gett"));
    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
  }

  @Test // invalid
  public void testExonFeaturesAdjacentOneWithGeneBothWithSameLocus() {
    QualifierFactory qualFact = new QualifierFactory();

    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature1.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "gett"));
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);

    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature2.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "gett"));
    feature2.addQualifier(qualFact.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "abcd"));
    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
  }

  @Test // invalid
  public void testExonFeaturesAdjacentOneWithNoGeneBothWithSameLocus() {
    QualifierFactory qualFact = new QualifierFactory();

    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature1.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "gett"));
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);

    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature2.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "gett"));

    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
  }

  @Test // valid
  public void testExonFeaturesAdjacentOneWithGeneBothWithDiffLocus() {
    QualifierFactory qualFact = new QualifierFactory();

    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature1.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "ghg"));
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);

    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature2.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "gett"));
    feature2.addQualifier(qualFact.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "abcd"));
    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test // valid
  public void testExonFeaturesAdjacentOneWithNoGeneBothWithDiffLocus() {
    QualifierFactory qualFact = new QualifierFactory();

    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature1.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "ghg"));
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);

    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    feature2.addQualifier(qualFact.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "gett"));
    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesAdjacentwithPartialLocation() {
    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location1 = locationFactory.createLocalRange(10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);
    feature1.getLocations().setThreePrime(true);
    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    LocalRange location2 = locationFactory.createLocalRange(21L, 23L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    feature2.getLocations().setFivePrime(true);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertFalse(result.isValid());
  }

  @Test
  public void testCheck_withExonFeaturesAdjacentwithRemoteLocation() {
    Feature feature1 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    RemoteRange location1 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    CompoundLocation<Location> join1 = new Join<Location>();
    join1.addLocation(location1);
    feature1.setLocations(join1);
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
    RemoteRange location2 = locationFactory.createRemoteRange("A00001", 1, 24L, 26L);
    CompoundLocation<Location> join2 = new Join<Location>();
    join2.addLocation(location2);
    feature2.setLocations(join2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }
}
