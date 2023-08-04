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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class Assembly_gapFeatureCheckTest {

  private Entry entry;
  private Assembly_gapFeatureCheck check;
  public SequenceFactory sequenceFactory;
  public FeatureFactory featureFactory;
  public LocationFactory locationFactory;
  public QualifierFactory qualifierFactory;
  public Feature assembly_gapFeature1, assembly_gapFeature2, gapFeature, geneFeature;
  public Qualifier gap_typeQualifier1,
      gap_typeQualifier2,
      linkage_evidenceQualifier,
      estimated_lengthQualifier;
  public Order<Location> location1, location2, featureLocation;
  public Sequence sequence;

  @Before
  public void setUp() {
    EntryFactory entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    locationFactory = new LocationFactory();
    qualifierFactory = new QualifierFactory();
    sequenceFactory = new SequenceFactory();
    entry = entryFactory.createEntry();
    check = new Assembly_gapFeatureCheck();
    // features:assembly_gap,gap
    assembly_gapFeature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    assembly_gapFeature2 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    gapFeature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
    geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    // qualifiers:
    gap_typeQualifier1 = qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
    gap_typeQualifier2 = qualifierFactory.createQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME);
    linkage_evidenceQualifier =
        qualifierFactory.createQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);
    estimated_lengthQualifier =
        qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
    // contig locations
    location1 = new Order<Location>();
    location2 = new Order<Location>();
    location1.addLocation(locationFactory.createLocalRange(13l, 18l));
    location2.addLocation(locationFactory.createRemoteRange("B13063", 1, 200L, 350L));
    // feature locations
    featureLocation = new Order<Location>();
    featureLocation.addLocation(locationFactory.createLocalBetween(1l, 10l)); // not a range
    // sequence
    sequence = sequenceFactory.createSequence();
    entry.setSequence(sequence);
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
  public void testCheck_NoQualifiers() {
    entry.addFeature(assembly_gapFeature1);
    entry.addFeature(assembly_gapFeature2);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(
        0,
        validationResult.count(
            Severity.ERROR)); // but it will give FeatureKeyCheck_2 error messages
  }

  @Test
  public void testCheck_invalidQualifiers() {
    assembly_gapFeature1.addQualifier(Qualifier.NOTE_QUALIFIER_NAME);
    entry.addFeature(assembly_gapFeature1);
    entry.addFeature(assembly_gapFeature2);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(
            Assembly_gapFeatureCheck.INVALID_QUALIFIER_MESSAGE,
            Severity.ERROR)); // FeatureKeyCheck_2 error messages
  }

  @Test
  public void testCheck_QualifierValuesInvalid() {

    gap_typeQualifier1.setValue("spiA");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    entry.addFeature(assembly_gapFeature1);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid()); // but it will give QualifierCheck-4 error
  }

  @Test
  public void testCheck_assembly_gap_feature() {

    gap_typeQualifier1.setValue("between scaffolds");
    geneFeature.addQualifier(gap_typeQualifier1);
    entry.addFeature(geneFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(Assembly_gapFeatureCheck.NO_ASSEMBLY_GAP_MESSAGE, Severity.ERROR));
  }

  @Test
  public void testCheck_gap_feature() {

    gap_typeQualifier1.setValue("between scaffolds");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    entry.addFeature(assembly_gapFeature1);
    entry.addFeature(gapFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(
        1, validationResult.count(Assembly_gapFeatureCheck.GAP_FEATURE_MESSAGE, Severity.ERROR));
  }

  @Test
  public void testCheck_QualifierValues() {

    gap_typeQualifier1.setValue("between scaffolds");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
    entry.addFeature(assembly_gapFeature1);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_linkage_evidenceQualifier1() {

    gap_typeQualifier1.setValue("between scaffolds");
    linkage_evidenceQualifier.setValue("paired ends");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
    assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
    entry.addFeature(assembly_gapFeature1);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(
            Assembly_gapFeatureCheck.LINKAGE_EVIDENCE_NOTALLOWED_MESSAGE, Severity.ERROR));
  }

  @Test
  public void testCheck_linkage_evidenceQualifier2() {

    gap_typeQualifier1.setValue("within scaffold");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
    entry.addFeature(assembly_gapFeature1);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(
            Assembly_gapFeatureCheck.LINKAGE_EVIDENCE_MISSING_MESSAGE, Severity.ERROR));
  }

  @Test
  public void testCheck_linkage_evidenceQualifier3() {

    gap_typeQualifier1.setValue("within scaffold");
    linkage_evidenceQualifier.setValue("paired ends");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
    assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
    entry.addFeature(assembly_gapFeature1);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(
        0,
        validationResult.count(
            Assembly_gapFeatureCheck.LINKAGE_EVIDENCE_MISSING_MESSAGE, Severity.ERROR));
  }
  /*@Test
  public void testCheck_co_line() {

  	gap_typeQualifier1.setValue("within scaffold");
  	linkage_evidenceQualifier.setValue("paired ends");
  	assembly_gapFeature1.addQualifier(gap_typeQualifier1);
  	assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
  	assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
  	entry.addFeature(assembly_gapFeature1);
  	ValidationResult validationResult = check.check(entry);
  	assertTrue(!validationResult.isValid());
  	assertEquals(1, validationResult.count(Assembly_gapFeatureCheck.CO_LINE_MESSAGE, Severity.ERROR));
  }*/
  // 	 EMD-6443
  @Test
  public void testCheck_feature_location1() {

    gap_typeQualifier1.setValue("within scaffold");
    linkage_evidenceQualifier.setValue("paired ends");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
    assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
    assembly_gapFeature1.setLocations(featureLocation);
    featureLocation.setSimpleLocation(true);
    entry.addFeature(assembly_gapFeature1);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(
        0,
        validationResult.count(Assembly_gapFeatureCheck.INVALID_LOCATION_MESSAGE, Severity.ERROR));
  }

  @Test
  public void testCheck_feature_location2() {

    gap_typeQualifier1.setValue("within scaffold");
    linkage_evidenceQualifier.setValue("paired ends");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
    assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
    assembly_gapFeature1.setLocations(featureLocation);
    featureLocation.setSimpleLocation(false);
    entry.addFeature(assembly_gapFeature1);
    entry.getSequence().addContigs(location1.getLocations());
    entry.getSequence().addContigs(location2.getLocations());
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(Assembly_gapFeatureCheck.INVALID_LOCATION_MESSAGE, Severity.ERROR));
  }
  /*@Test
  public void testCheck_sequence() {

  	gap_typeQualifier1.setValue("within scaffold");
  	linkage_evidenceQualifier.setValue("paired ends");
  	assembly_gapFeature1.addQualifier(gap_typeQualifier1);
  	assembly_gapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
  	assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
  	assembly_gapFeature1.setLocations(featureLocation);
  	featureLocation.setSimpleLocation(true);
  	entry.addFeature(assembly_gapFeature1);
  	entry.addContigs(location1.getLocations());
  	entry.addContigs(location2.getLocations());
  	sequence.setSequence("ccttgattac cacggttgtc gttgtgtctc atttcaccct ttagcatgta ggtgaccgta");
  	entry.setSequence(sequence);
  	ValidationResult validationResult = check.check(entry);
  	assertTrue(!validationResult.isValid());
  	assertEquals(1, validationResult.count(Assembly_gapFeatureCheck.SEQUENCE_MESSAGE, Severity.ERROR));
  }*/
  // 	 EMD-6443

  @Test
  public void testCheck_TSA_Fail() {
    entry.setDataClass("TSA");
    gap_typeQualifier1.setValue("between scaffolds");
    linkage_evidenceQualifier.setValue("unspecified");
    estimated_lengthQualifier.setValue("unknown");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(estimated_lengthQualifier);
    assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
    assembly_gapFeature1.setLocations(featureLocation);
    entry.addFeature(assembly_gapFeature1);
    ValidationResult validationResult = check.check(entry);
    assertTrue(!validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(
            Assembly_gapFeatureCheck.ESTIMATED_LENGTH__MESSAGE_TSA, Severity.ERROR));
    assertEquals(
        1, validationResult.count(Assembly_gapFeatureCheck.GAP_TYPE_MESSAGE_TSA, Severity.ERROR));
    assertEquals(
        1,
        validationResult.count(
            Assembly_gapFeatureCheck.LINKAGE_EVIDENCE_INVALID_MESSAGE_TSA, Severity.ERROR));
  }

  @Test
  public void testCheck_TSA() {
    entry.setDataClass("TSA");
    gap_typeQualifier1.setValue("within scaffold");
    linkage_evidenceQualifier.setValue("paired ends");
    estimated_lengthQualifier.setValue("12");
    assembly_gapFeature1.addQualifier(gap_typeQualifier1);
    assembly_gapFeature1.addQualifier(estimated_lengthQualifier);
    assembly_gapFeature1.addQualifier(linkage_evidenceQualifier);
    assembly_gapFeature1.setLocations(featureLocation);
    entry.addFeature(assembly_gapFeature1);
    featureLocation.setSimpleLocation(true);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }
}
