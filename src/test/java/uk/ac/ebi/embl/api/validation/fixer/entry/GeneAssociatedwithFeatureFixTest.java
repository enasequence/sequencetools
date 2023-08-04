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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class GeneAssociatedwithFeatureFixTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private LocationFactory locationFactory;
  private GeneAssociatedwithFeatureFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    locationFactory = new LocationFactory();
    check = new GeneAssociatedwithFeatureFix();
  }

  @Test
  public void testCheck_NoEntry() {
    ValidationResult result = check.check(null);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_NoFeatures() {
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_NoGenes() {
    Feature cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Order<Location> cdsFeatureLocation = new Order<Location>();
    cdsFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    cdsFeature.setLocations(cdsFeatureLocation);
    Feature tRNAFeature = featureFactory.createFeature(Feature.tRNA_FEATURE_NAME);
    Order<Location> tRNAFeatureLocation = new Order<Location>();
    cdsFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    cdsFeature.setLocations(tRNAFeatureLocation);
    entry.addFeature(cdsFeature);
    entry.addFeature(tRNAFeature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_NogeneAssociatedFeatures() {
    Feature geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    Order<Location> geneFeatureLocation = new Order<Location>();
    geneFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    geneFeature.setLocations(geneFeatureLocation);
    entry.addFeature(geneFeature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_withGeneandassociatedFeatureDifferentlocation() {

    Feature geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    Order<Location> geneFeatureLocation = new Order<Location>();
    geneFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    geneFeature.setLocations(geneFeatureLocation);
    entry.addFeature(geneFeature);
    Feature cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Order<Location> cdsFeatureLocation = new Order<Location>();
    cdsFeatureLocation.addLocation(locationFactory.createLocalRange(10l, 114l));
    cdsFeature.setLocations(cdsFeatureLocation);
    entry.addFeature(cdsFeature);
    Feature tRNAFeature = featureFactory.createFeature(Feature.tRNA_FEATURE_NAME);
    Order<Location> tRNAFeatureLocation = new Order<Location>();
    tRNAFeatureLocation.addLocation(locationFactory.createLocalRange(10l, 114l));
    tRNAFeature.setLocations(tRNAFeatureLocation);
    entry.addFeature(tRNAFeature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_withGeneandMultipleAssociationFeatureSamelocation() {

    Feature geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    Order<Location> geneFeatureLocation = new Order<Location>();
    geneFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    geneFeature.setLocations(geneFeatureLocation);
    entry.addFeature(geneFeature);
    Feature cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Order<Location> cdsFeatureLocation = new Order<Location>();
    cdsFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    cdsFeature.setLocations(cdsFeatureLocation);
    entry.addFeature(cdsFeature);
    Feature tRNAFeature = featureFactory.createFeature(Feature.tRNA_FEATURE_NAME);
    Order<Location> tRNAFeatureLocation = new Order<Location>();
    tRNAFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    tRNAFeature.setLocations(tRNAFeatureLocation);
    entry.addFeature(tRNAFeature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(1, result.getMessages().size());
    assertEquals(1, result.getMessages("GeneAssociatedwithFeatureFix", Severity.FIX).size());
  }

  @Test
  public void testCheck_withGeneandSingleAssociationFeatureSamelocation() {

    Feature geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    Order<Location> geneFeatureLocation = new Order<Location>();
    geneFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    geneFeature.setLocations(geneFeatureLocation);
    entry.addFeature(geneFeature);
    Feature tRNAFeature = featureFactory.createFeature(Feature.tRNA_FEATURE_NAME);
    Order<Location> tRNAFeatureLocation = new Order<Location>();
    tRNAFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
    tRNAFeature.setLocations(tRNAFeatureLocation);
    entry.addFeature(tRNAFeature);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(1, result.getMessages().size());
    assertEquals(1, result.getMessages("GeneAssociatedwithFeatureFix", Severity.FIX).size());
  }
}
