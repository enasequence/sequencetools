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
package uk.ac.ebi.embl.flatfile.writer.degenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 25-Oct-2010 Time: 16:29:44 To change this template
 * use File | Settings | File Templates.
 */
public class DEGeneratorTest extends TestCase {

  EntryFactory entryFactory;
  FeatureFactory featureFactory;
  QualifierFactory qualifierFactory;
  SequenceFactory sequenceFactory;
  LocationFactory locationFactory;
  Entry entry;

  @Before
  public void setUp() throws Exception {
    this.entryFactory = new EntryFactory();
    this.featureFactory = new FeatureFactory();
    this.sequenceFactory = new SequenceFactory();
    this.qualifierFactory = new QualifierFactory();
    this.locationFactory = new LocationFactory();

    this.entry = entryFactory.createEntry();
    entry.setDataClass(Entry.STANDARD_DATACLASS);
    entry.addFeature(featureFactory.createFeature(Feature.SOURCE_FEATURE_NAME));
    entry.setSequence(sequenceFactory.createSequence());
  }

  @Test
  public void test_no_entry() {
    DEGenerator.writeDE(null);
  }

  @Test
  public void test_no_keyword() {
    entry.getKeywords().clear();
    DEGenerator.writeDE(entry);
  }

  @Test
  public void test_no_dataclass() {
    entry.setDataClass(null);
    ValidationResult validationResult = DEGenerator.writeDE(entry);
    assertEquals(1, validationResult.count("Entry has no dataclass", Severity.ERROR));
  }

  @Test
  public void test_no_topology() {
    entry.getSequence().setTopology(null);
    DEGenerator.writeDE(entry);
  }

  @Test
  public void test_no_mol_type() {
    entry.getSequence().setTopology(null);
    DEGenerator.writeDE(entry);
  }

  @Test
  public void testTPAFile1() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("/test_files/de_files/testfile2.txt");
    String entryString = readFileAsString(stream);

    EmblEntryReader entryReader =
        new EmblEntryReader(new BufferedReader(new StringReader(entryString)));
    entryReader.read();
    Entry entry = entryReader.getEntry();

    ValidationResult validationResult = DEGenerator.writeDE(entry);
    assertEquals(0, validationResult.count());
    String deLine = entry.getDescription().getText();

    assertTrue(deLine.startsWith("TPA"));
  }

  @Test
  public void testOrgainsm1() throws IOException {
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features
        .get(0)
        .addQualifier(
            qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "chicken"));
    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("chicken", deLine);
  }

  /**
   * no proviral qualifier - despite endogenous retrovirus note
   *
   * @throws IOException
   */
  @Test
  public void testOrgainsm2() throws IOException {
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features
        .get(0)
        .addQualifier(
            qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "chicken"));
    features
        .get(0)
        .addQualifier(
            qualifierFactory.createQualifier(
                Qualifier.NOTE_QUALIFIER_NAME, "endogenous retrovirus"));
    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("chicken", deLine);
  }

  @Test
  public void testOrgainsm3() throws IOException {
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features
        .get(0)
        .addQualifier(
            qualifierFactory.createQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "chicken"));
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.PROVIRAL_QUALIFIER_NAME));
    features
        .get(0)
        .addQualifier(
            qualifierFactory.createQualifier(
                Qualifier.NOTE_QUALIFIER_NAME, "endogenous retrovirus"));
    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("endogenous retrovirus", deLine);
  }

  @Test
  public void testOrganelle1() throws IOException {
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features
        .get(0)
        .addQualifier(
            qualifierFactory.createQualifier(
                Qualifier.ORGANELLE_QUALIFIER_NAME, "blah blah : mitochondrian gubbins"));
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.PLASMID_QUALIFIER_NAME, "thingy"));
    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("mitochondrian gubbins plasmid thingy", deLine);
  }

  @Test
  public void testBody1() throws IOException {
    entry.setDataClass(Entry.EST_DATACLASS);
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "urine"));
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.CLONE_QUALIFIER_NAME, "cb1"));
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.MAP_QUALIFIER_NAME, "kinase"));
    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("EST, isolate urine, clone cb1, map kinase", deLine);
  }

  @Test
  public void testBody2() throws IOException {
    entry.getSequence().setMoleculeType(Sequence.GENOMIC_DNA_MOLTYPE);
    Feature cdsFeature = featureFactory.createCdsFeature();
    cdsFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "g1"));
    cdsFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "p1"));
    entry.addFeature(cdsFeature);
    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("g1 gene for p1", deLine);
  }

  /**
   * gene rather than cds
   *
   * @throws IOException
   */
  @Test
  public void testBody3() throws IOException {
    entry.getSequence().setMoleculeType(Sequence.GENOMIC_DNA_MOLTYPE);
    Feature geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    geneFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "g1"));
    geneFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "p1"));
    entry.addFeature(geneFeature);
    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("g1 gene for p1", deLine);
  }

  @Test
  public void testBody4() throws IOException {
    entry.getSequence().setMoleculeType(Sequence.GENOMIC_DNA_MOLTYPE);
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "urine"));

    Feature cdsFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    cdsFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "g1"));
    cdsFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "p1"));
    entry.addFeature(cdsFeature);

    Feature cdsFeature2 = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    cdsFeature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "g2"));
    cdsFeature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "p2"));
    entry.addFeature(cdsFeature2);

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("g1 gene for p1, g2 gene for p2, isolate urine", deLine);
  }

  @Test
  public void testBody5() throws IOException {

    entry.getSequence().setTopology(Sequence.Topology.CIRCULAR);

    //        entry.setDataClass(Entry.STANDARD_DATACLASS);
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "urine"));
    features
        .get(0)
        .addQualifier(qualifierFactory.createQualifier(Qualifier.STRAIN_QUALIFIER_NAME, "s1"));

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("complete sequence, isolate urine, strain s1", deLine);
  }

  @Test
  public void testBody5_2() throws IOException {

    entry.getSequence().setTopology(Sequence.Topology.CIRCULAR);

    //        entry.setDataClass(Entry.STANDARD_DATACLASS);
    Feature feature = entry.getPrimarySourceFeature();
    feature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME, "3"));
    feature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "urine"));
    feature.addQualifier(qualifierFactory.createQualifier(Qualifier.STRAIN_QUALIFIER_NAME, "s1"));

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("chromosome 3 complete sequence, isolate urine, strain s1", deLine);
  }

  @Test
  public void testBody5_3() throws IOException {

    entry.getSequence().setTopology(Sequence.Topology.CIRCULAR);

    //        entry.setDataClass(Entry.STANDARD_DATACLASS);
    Feature feature = entry.getPrimarySourceFeature();
    feature.addQualifier(qualifierFactory.createQualifier(Qualifier.SEGMENT_QUALIFIER_NAME, "3"));
    feature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "urine"));
    feature.addQualifier(qualifierFactory.createQualifier(Qualifier.STRAIN_QUALIFIER_NAME, "s1"));

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("segment 3 complete sequence, isolate urine, strain s1", deLine);
  }

  @Test
  public void testBody6() throws IOException {

    entry.getSequence().setMoleculeType(Sequence.MRNA_MOLTYPE);
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features.get(0).addQualifier(Qualifier.MOL_TYPE_QUALIFIER_NAME, Sequence.MRNA_MOLTYPE);

    Feature geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    geneFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "g1"));
    geneFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "p1"));
    entry.addFeature(geneFeature);

    /** this one should take precedence over the gene feature */
    Feature cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    cdsFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "c1"));
    cdsFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "c1"));
    entry.addFeature(cdsFeature);

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("mRNA for c1(c1 gene)", deLine);
  }

  @Test
  public void testBody7() throws IOException {

    entry.getSequence().setMoleculeType(Sequence.MRNA_MOLTYPE);
    List<Feature> features = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);
    features.get(0).addQualifier(Qualifier.MOL_TYPE_QUALIFIER_NAME, Sequence.MRNA_MOLTYPE);

    Feature geneFeature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    geneFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "g1"));
    geneFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "p1"));
    entry.addFeature(geneFeature);

    Feature geneFeature2 = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    geneFeature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "g2"));
    geneFeature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "g2"));
    entry.addFeature(geneFeature2);

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("polycistronic mRNA for p1(g1 gene), g2(g2 gene)", deLine);
  }

  @Test
  public void testBody8() throws IOException {

    Feature feature = featureFactory.createFeature(Feature.MISC_FEATURE_NAME);
    feature.addQualifier(Qualifier.NOTE_QUALIFIER_NAME, "bacon");
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(1L, 10L);
    join.addLocation(range);
    feature.setLocations(join);
    entry.addFeature(feature);

    entry.getSequence().setSequence(ByteBuffer.wrap("aaaaaaaaaa".getBytes())); // 10 long
    // entry.getSequence().setLength(10);
    entry.getSequence().setMoleculeType(Sequence.GENOMIC_DNA_MOLTYPE);

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("bacon", deLine);
  }

  @Test
  public void testBody9() throws IOException {

    entry.setDataClass(Entry.STANDARD_DATACLASS);
    Feature feature = featureFactory.createFeature(Feature.rRNA_FEATURE_NAME);
    feature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "bacon");
    entry.getPrimarySourceFeature().addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "thingy");
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(1L, 10L);
    join.addLocation(range);
    join.setLeftPartial(true);
    feature.setLocations(join);
    entry.addFeature(feature);

    entry.getSequence().setSequence(ByteBuffer.wrap("aaaaaaaaaa".getBytes())); // 10 long
    // entry.getSequence().setLength(10);
    entry.getSequence().setMoleculeType(Sequence.RRNA_MOLTYPE);

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("partial bacon, isolate thingy", deLine);
  }

  @Test
  public void testBody10() throws IOException {

    entry.setDataClass(Entry.STANDARD_DATACLASS);
    Feature feature = featureFactory.createFeature(Feature.rRNA_FEATURE_NAME);
    feature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "bacon");
    entry.getPrimarySourceFeature().addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "thingy");
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(1L, 10L);
    join.addLocation(range);
    join.setLeftPartial(true);
    feature.setLocations(join);
    entry.addFeature(feature);

    entry.getSequence().setSequence(ByteBuffer.wrap("aaaaaaaaaa".getBytes())); // 10 long
    // entry.getSequence().setLength(10);
    entry.getSequence().setMoleculeType(Sequence.GENOMIC_DNA_MOLTYPE);

    DEGenerator.writeDE(entry);

    String deLine = entry.getDescription().getText();
    assertEquals("partial gene for bacon, isolate thingy", deLine);
  }

  @Test
  public void testCompleteGenomeFile() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("/test_files/de_files/testfile3.txt");
    String entryString = readFileAsString(stream);

    EmblEntryReader entryReader =
        new EmblEntryReader(new BufferedReader(new StringReader(entryString)));
    entryReader.read();
    Entry entry = entryReader.getEntry();

    ValidationResult validationResult = DEGenerator.writeDE(entry);
    assertEquals(0, validationResult.count());
    String deLine = entry.getDescription().getText();

    assertEquals("Halobacterium salinarum R1 complete sequence, strain DSM 671 = R1", deLine);
  }

  @Test
  public void testCircularSegmentFile() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("/test_files/de_files/testfile4.txt");
    String entryString = readFileAsString(stream);

    EmblEntryReader entryReader =
        new EmblEntryReader(new BufferedReader(new StringReader(entryString)));
    entryReader.read();
    Entry entry = entryReader.getEntry();

    ValidationResult validationResult = DEGenerator.writeDE(entry);
    assertEquals(0, validationResult.count());
    String deLine = entry.getDescription().getText();

    assertEquals(
        "Faba bean necrotic stunt virus segment DNA-U4 complete sequence, isolate JKI-1998/99",
        deLine);
  }

  private static String readFileAsString(InputStream stream) throws java.io.IOException {
    byte[] buffer = new byte[(int) stream.available()];
    stream.read(buffer);
    return new String(buffer);
  }
}
