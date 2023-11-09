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

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.PeptideFeature;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class PeptideFeatureCheckTest {

  private Entry entry;
  private Feature cdsFeature;
  private FeatureFactory featureFactory;
  private LocationFactory locationFactory;
  private PeptideFeatureCheck check;
  private TaxonomyClient taxonomyClient;
  private EntryFactory entryFactory;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    locationFactory = new LocationFactory();
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    property.getOptions().isFixMode = false;
    entry = entryFactory.createEntry();
    cdsFeature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    Join<Location> join = new Join<Location>();
    join.addLocation(locationFactory.createLocalRange(5647L, 5804L));
    join.addLocation(locationFactory.createLocalRange(7506L, 7653L));
    join.addLocation(locationFactory.createLocalRange(7947L, 8183L));
    cdsFeature.setLocations(join);
    cdsFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");
    cdsFeature.addQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "LOCUS_IGF-II");

    entry.addFeature(cdsFeature);
    entry.setSequence(new SequenceFactory().createSequenceByte(TEST_SEQUENCE.getBytes()));

    taxonomyClient = createMock(TaxonomyClient.class);
    property.taxonClient.set(taxonomyClient);
    check = new PeptideFeatureCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @Test
  public void testCheck_noEntry() throws ValidationEngineException {
    ValidationResult validationResult = check.check(null);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_noFeatures() throws ValidationEngineException {
    ValidationResult validationResult = check.check(entryFactory.createEntry());
    assertTrue(validationResult.isValid());
  }

  /**
   * same locus tag but different strand
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_noRelevantPeptides_1() throws ValidationEngineException {

    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(5647L, 5718L);
    range.setComplement(
        true); // this puts it on the other strand to the cds - so should not be considered
    join.addLocation(range);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "LOCUS_IGF-II");

    entry.addFeature(peptideFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  /**
   * same locus tag but different strand - complex location
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_noRelevantPeptides_2() throws ValidationEngineException {

    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range =
        locationFactory.createLocalRange(
            5647L, 5718L,
            true); // this puts it on the other strand to the cds - so should not be considered
    join.addLocation(range);
    LocalRange range2 = locationFactory.createLocalRange(5700L, 5730L, false);
    join.addLocation(range2);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "LOCUS_IGF-II");

    entry.addFeature(peptideFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  /**
   * same gene but no overlap
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_noRelevantPeptides_3() throws ValidationEngineException {

    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range =
        locationFactory.createLocalRange(
            4000L, 5646L); // finishes just before the cds feature starts
    join.addLocation(range);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  /**
   * standard range with shared gene qualifier but CDS is pseudo and peptide feature is not
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_NoPseudo() throws ValidationEngineException {

    CdsFeature cdsFeature =
        (CdsFeature)
            SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry).iterator().next();
    cdsFeature.setPseudo(true);
    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(5647L, 5718L);
    join.addLocation(range);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
  }

  /**
   * standard range with shared gene qualifier and overlap
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_Okay() throws ValidationEngineException {

    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(5647L, 5718L);
    join.addLocation(range);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  /**
   * peptide feature not a substring of cds feature
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_NotSubstring() throws ValidationEngineException {

    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(5641L, 5718L);
    join.addLocation(range);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
    assertEquals(
        1,
        validationResult.count(PeptideFeatureCheck.PEPTIDE_NOT_SUBSTRING_MESSAGE, Severity.ERROR));
  }

  /**
   * peptide feature not a multiple of 3
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_NotMod3_1() throws ValidationEngineException {

    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(5647L, 5717L);
    join.addLocation(range);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count(PeptideFeatureCheck.NON_MOD_3_MESSAGE, Severity.ERROR));
  }

  /**
   * peptide feature not a multiple of 3, cds partial but not start codon 2 or 3
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_NotMod3_2() throws ValidationEngineException {

    CdsFeature cdsFeature =
        (CdsFeature)
            SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry).iterator().next();
    cdsFeature.setStartCodon(1);
    Feature peptideFeature = featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(5647L, 5717L);
    join.addLocation(range);
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);

    ValidationResult validationResult = check.check(entry);
    assertFalse(validationResult.isValid());
    assertEquals(1, validationResult.count(PeptideFeatureCheck.NON_MOD_3_MESSAGE, Severity.ERROR));
  }

  /**
   * peptide feature not a multiple of 3, cds partial start codon 2 or 3 and peptide begins at cds
   * start (okay)
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_NotMod3_3() throws ValidationEngineException {

    CdsFeature cdsFeature =
        (CdsFeature)
            SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry).iterator().next();
    cdsFeature.setStartCodon(2);
    cdsFeature.getLocations().setLeftPartial(true); // has to be if is start codon 2
    PeptideFeature peptideFeature =
        (PeptideFeature) featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    peptideFeature.setStartCodon(2);
    Join<Location> join = new Join<Location>();
    LocalRange range = locationFactory.createLocalRange(5647L, 5717L);
    join.addLocation(range);
    join.setLeftPartial(true); // as does not have a start codon
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  /**
   * peptide feature not a multiple of 3, cds partial and peptide ends at partial cds end (okay)
   *
   * @throws ValidationEngineException
   */
  @Test
  public void testCheck_NotMod3_4() throws ValidationEngineException {

    CdsFeature cdsFeature =
        (CdsFeature)
            SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry).iterator().next();
    cdsFeature.getLocations().setRightPartial(true);
    PeptideFeature peptideFeature =
        (PeptideFeature) featureFactory.createFeature(Feature.MAP_PEPTIDE_FEATURE_NAME, true);
    Join<Location> join = new Join<Location>();
    join.addLocation(locationFactory.createLocalRange(8177L, 8183L));
    peptideFeature.setLocations(join);
    peptideFeature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "IGF-II");

    entry.addFeature(peptideFeature);

    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
  }

  private static final String TEST_SEQUENCE =
      "cccaaccccgcgcacagcgggcactggtttcgggcctctctgtctcctacgaagtccgtagagcaact"
          + "cggatttgggaaatttctctctagcgttgcccaaacacacttgggtcggccgcgcgccctcaggacgtggacagggagggcttccccgtgtccaggaaagcgac"
          + "cgggcattgcccccagtctcccccaaatttgggcattgtccccgggtcttccaacggactgggcgnngctcccggacactgaggactggccccggggtctcgct"
          + "caccttcagcagcgtccaccgcctgccacagagcgttcgatcgctcgctgcctgagctcctggtgcgcccgcggacgcagcctccagcttcgcggtgagctccc"
          + "cgccgcgccgatcccctccgcctctgcgcccctgaccggctctcggcccgcatctgctgctgtcccgccggtgctggcgctcgtccgctgcgccggggaggccg"
          + "gcgtggggcgcgggacacggctgcggacttgcggctgcgctgcgctcgctcctgctgggcgccccgaaatccgcgccactttcgtttgctcattgcaaagatct"
          + "catttgtggggaaagcggctggagggtcccaaagtggggcgggcagggggctggggcgagggacgcggaggagaggcgctcccgccgggcggtaaagtgcctct"
          + "agcccgcgggcctaggactccgccgggagggcgcgcggagngcgaagtgattgatggcggaagcgggggggcaaggggggcaggggggcgcgggattccgccgg"
          + "cgaccccttccccttggctaggcttaggcggcggggggctggcggggtgcgggattttgtgcgtggtttttgacttggtaaaaatcacagtgctttcttacatc"
          + "gttcaaactctccaggagatggtttccccagacccccaaattatcgtggtggcccccgagaccgaactcgcgtctatgcaagtccaacgcactgaggacggggt"
          + "aaccattatccagatattttgggtgggccgcaaaggcgagctacttagacgcaccccggtgagctcggccatgcaggtaggatttgagctgtgtttcccgccct"
          + "gatcctctctcctctggcggccggagcctccgtaggctccaagcctggcccagattcggcggcgcagccggccttccgcgcgtccgcacctagcgggggctccg"
          + "gggctccggcgcggcaccggggggcgctcgggatctggctgaggctccaaggcccgcgtggccggctcctcctgctggggcaggtggcggctgcgcgccccgcc"
          + "cgagcccaggggccccctcagccgcaacaaccagcaaggaccccccgactcagccccaagccacctgcatctgcactcagacggggcgcacccgcagtgcagcc"
          + "tcctggtggggcgctgggagcccgcctgcccctgcctgcccggagaccccagctcacgagcacaggccgcccgggcaccccagaaacccgggatggggcccctg"
          + "aattctctaggacgggcattcagcatggccttggcgctctgcggctccctgccccccacccagcctcgcccccgcgcaccccccagcccctgcgaccgccgccc"
          + "ccccccccggggccccagggccccagcccgcaccccccgccccgctcttggctcgggttgcgggggcgggccgggggcggggcgagggctccgcgggcgcccat"
          + "tggcgcgggcgcgaggccagcggccccgcgcggccctgggccgcggctggcgcgactataagagccgggcgtgggcgcccgcagttcgcctgctctccggcgga"
          + "gctgcgtgaggcccggccggccccggccccccccttccggccgcccccgcctcctggcccacgcctgcccgcgctctgcccaccagcgcctccatcgggcaagg"
          + "cggccccgcgtcgacgccgcccgctgcctcgctgctgactcccgtcccgggcgccgtccgcggggtcgcgctccgccgggcctgcggattccccgccgcctcct"
          + "cttcatctacctcaactccccccatccccgcttcgcccgaggaggcggttccccccgcaggcagtccggctcgcaggccgccggcgttgtcaccccccccgcgc"
          + "tccccctccagccctccccccggcgcgcagcctcgggccgctcccctttccgcgctgcgtcccggagcggccccggtgccgccaccgcctgtccccctcccgag"
          + "gcccgggctcgcgacggcagagggctccgtcggcccaaaccgagctgggcgcccgcggtccgggtgcagcctccactccgccccccagtcaccgcctcccccgg"
          + "cccctcgacgtggcgcccttccctccgcttctctgtgctccccgcgcccctcttggcgtctggccccggcccccgctctttctcccgcaaccttcccttcgctc"
          + "cctcccgtcccccccagctcctagcctccgactccctccccccctcacgcccgccctctcgccttcgccgaaccaaagtggattaattacacgctttctgtttc"
          + "tctccgtgctgttctctcccgctgtgcgcctgcccgcctctcgctgtcctctctccccctcgccctctcttcggcccccccctttcacgttcactctgtctctc"
          + "ccactatctctgcccccctctatccttgatacaacagctgacctcatttcccgataccttttcccccccgaaaagtacaacatctggcccgccccagcccgaag"
          + "acagcccgtcctccctggacaatcagacgaattctccccccccccccaaaaaaaagccatccccccgctctgccccgtcgcacattcggcccccgcgactcggc"
          + "cagagcggcgctggcagaggagtgtccggcaggagggccaacgcccgctgttcggtttgcgacacgcagcagggaggtgggcggcagcgtcgccggcttccaggt"
          + "aagcggcgtgtgcgggccgggccggggccggggctggggcggcgcgggcttgcggcgacgcccggcccttcctccgcccgctcccggcccggggcctgcggggct"
          + "cggcggggcggctgagccgggggggaggaggaggaggaggaggaggacggacggctgcgggtcccgttccctgcgcggagcccgcgctaccnnnnnnnnnnnnnn"
          + "nnnnnnnnnnnngacgtccccgctgaagggggtcggtctgtgggtgcagggggtgccgcctcacatgtgtgattcgtgccttgcgggccctggcctccggggtgc"
          + "tgggtaacgaggaggggcgcggagccgcagaagcccaccctggtgtcgttgacgccggtgccagcgagaccgcgagaggaagacgggggcgggcggggccaggat"
          + "ggagaggggccgagttggcaggagtcatggcagacgccacactcgcgaccatctcccccacacccctctggcctctgtccgcaacatttccaaacaggagtcccg"
          + "ggagagggggagaggggctgctggtctgaggctaagaagggcagagccttcgacccggagagaggccgcggccgcctgccccagtggcaacgttgaagttttcc"
          + "atacaacggaggtcgggaaggagaccccccccccccttcactgccctgtgaagagatgagccgggggtgcaggatgggagcccatggcacttcgctacgggatg"
          + "tccagggctccggttgggggtgcaggagagaagagactggctgggaggagggagagggcgggagcaaaggcgcgggggtgtggtcagagggagaggggtggggg"
          + "ttaggtggagcccgggctgggaggagtcggctcacacataaaactgaggcactgaccagcctgcaaactggatattagcttctcctgtgaaagagacttccagc"
          + "ttcctcctcctcctcttcctcctcctcctcctgccccagcgagccttctgctgagctgtaggtaaccagggctgtggagtgaaggacccccgctgccatcccac"
          + "tccagcctgaggcagggcagcagggggcacggcccacgcctgggcctcgggccctgcagccgccagcccgctgcctctcggacagcacccccctcccctctttt"
          + "cctctgcccctgcccccacctggcgtctctgctccctcacctgctccttccctttctgttccttcccttcggccccctccttgcccagctcaggacttttcctg"
          + "ggccctcacctgctccgcaccgctgcatgcttcctgtcctgctttctgccggtcccctgacccggacctccaagcgcagagtggtggggcttgttgcggaagcg"
          + "cggcgagggctagagtggccagctggcggagtgtgctcttagaatttggaagggggtggcagagggggcggtgagaggactggccagggtccgccatgtcaagg"
          + "agatgaccaaggaggctttcagatcctcggcgcagtcgcccactagtctttagagagggcatgcaaagttgtgcttctgtcccactgcctgctcagtcgctcac"
          + "ataatttattgcatcaaaaactcccctgggtctgcggagcaaggctggggctgcccgcctggagggtaccaccttctgcaggagcagggccaacttgctgtggt"
          + "ggctcccggcctcccacccccgagtgggtaacccggccctgtgacctgcagcctgtggagggggtgtgcctaagactggcctccccttccagattgtagtctgg"
          + "ggaacctggtgtcggacttcccaggtggcctgagctggtctcttcagctccacggggagagtttggtagcgcaaatagggagatgttctgggcccctggcctta"
          + "ctggttcgatttgaggcctggaaaggaggctctgggcgtgtgtgtgtgtgtttgggggtacccaaggcagactggagttggagaactgggtgactgggaaaaca"
          + "aggtttctagagcatgggtggcgtggttgtgttaaccattggagtcgcttgacccaggcctggctcagctgcagactggaaaggtggaaaagccagggggaggg"
          + "gcggggctggcccagcaggactggcctgctgctttgagggcgatggtcctcctggaccccccctgctcagctgggggttgtggggaggaaggggctggtcctcc"
          + "ttggagcacatgctctgtaggggtggggctgtctgccatcttggcggcgctggaggcctgagaagtggcgatgtaacgctgggctggccctgcccccatggtgt"
          + "cataggacggaggcaggtcgggtgtccagcctgggcccctgcagctgtggatgccgctgagctcctgcaataatgaccgtgcagatggtcacccctcgtgtaaa"
          + "attactagtgcttcttgcaaatggaaggaactgggccttttctgtgtgcttctggacgcttcattctgcacatggccctgcgccctcacctcggcattatgacc"
          + "tgtgtgttacttttgtaataaaaataatgtttataggaaagccgtgctttcaattttcaactgaatttgtaggttggcaaatttggtttgggaggggcacctct"
          + "ggcctggggcttggcctggctgccccgctcacgccacttctctcccgcccccagacaccaatgggaatcccaatggggaagtcgatgctggtgcttctcacctt"
          + "cttggccttcgcctcgtgctgcattgctgcttaccgccccagtgagaccctgtgcggcggggagctggtggacaccctccagttcgtctgtggggaccgcggct"
          + "tctacttcagtaagtagcagggaggggcttcctcagacctggtcaggcccctagagtgaccggtgaggatctcccatcctcaagccaggggagcacactcctag"
          + "gtcagcagcccagccgcttgctctgagactttgaccttcccgccgcgtttctgagcacgtgcggtgtcccagggcatccacaccagctgcctttcccatcacac"
          + "gcctccttcgaagggtgggccagaggtgccccctagacgtcaggggcatctacaggggtctccctgggcatcagaatttctgttgggggccgtgaggctcctgc"
          + "tcctgaggcaccgcacgcctagtgcagggcttcaggctctggaggaagagcctgcctttcttcctgcaccttttggacattttgacaagggacgtgcgttcggt"
          + "gaatgatcagaattaaaatcaataaagtgatttatataattaaaatcaataagacaagtgcagttggtgggtggcaggggtgagcggtgcatgcgcctccttgg"
          + "gccccaaggctgccgtggggggtgcccacctgctgacctcaaggacgcttcagcctttcctcatgtttctctcttggttctccagcctgggggctggcaggtgggtgcatggcccattgtccttgagaccccacccccagataggggggctgggtggatgcagaggcaggcatggtgcctgggcatgcctgatggggcaggggaggggccgctccttactggcagaggccgcaacttattccacctgacactcaccacgtgacatctttaccaccactgcttactcacgctgtgaaatgggctcacaggatgcaaatgcacttcaaagcttctctctgaaaagttcctgctgcttgactctggaagcccctgcccgccctggcctctcctgtgccctctctcttgcctgccccatttgggggtaggaagtggcactgcagggcctggtgccagccagtccttgcccagggagaagcttccctgcaccaggctttcctgagaggaggggagggccaagcccccacttgggggcccccgtgacggggcctcctgctccctcctccggctgatggcacctgccctttggcaccccaaggtggagcccccagcgaccttccccttccagctgagcattgctgtgggggagagggggaagacgggaggaaagaagggagtggttccatcacgcctcctcagcctcctctcctcccgtcttctcctctcctgcccttgtctccctgtctcagcagctccaggggtggtgtgggcccctccagcctcccaggtggtgccaggccagagtccaagctcacggacagcagtcctcctgtgggggccctgaactgggctcacatcccacacattttccaaaccactcccattgtgagcctttggtcctggtggtgtccctctggttgtgggaccaagagcttgtgcccatttttcatctgaggaaggaggcagcagaagtcacgggctggtctgggccccactcacctcccctctcacctctcttcttcctgggacgcctctgcctgccggctctcacttccctcccctgacccgcagggtggctgcgnccttccagggcctggcctgagggcaggggtggtttgctgggggttcggcctccgggggctgggggtcggtgcggtgctaacacggctctctctgtgctgtgggacttccaggcaggcccgcaagccgtgtgagccgtcgcagccgtggcatcgttgaggagtgctgtttccgcagctgtgacctggccctcctggagacgtactgtgctacccccgccaagtccgagagggacgtgtcgacccctccgaccgtgcttccggtgagggtcctgggcccctttcccactctctagagacagagaaatagggcttcgggcgcccagcgtttcctgtggcctctgggacctcttggccagggacaaggacccgtgacttccttgcttgctgtgtggcccgggagcagctcagacgctggctccttctgtccctctgcccgtggacattagctcaagtcactgatcagtcacaggggtggcctgtcaggtcaggcgggcggctcaggcggaagagcgtggagagcaggcacctgctgaccagccccttcccctcccaggacaacttccccgagatacccctgggcaagttcttccaatatgacacctggaagcagtccacccagcgcctgcgcaggggcctgcctgccctcctgcgtgcccgccggggtcacgtgctcgccaaggagctcgaggcgttcagggaggccaaacgtcaccgtcccctgattgctctacccacccaagaccccgcccacgggggcgcccccccagagatggccagcaatcggaagtgagcaaaactgccgcaagtctgcagcccggcgccaccatcctgcagcctcctcctgaccacggacgtttccatcaggttccatcccgaaaatctctcggttccacgtcccctggggcttctcctgacccagtccccgtgccccgcctccccgaaacaggctactctcctcggccccctccatcgggctgaggaagcacagcagcatcttcaaacatgtacaaaatcgattggctttaaacacccttcacataccctccccccaaattatccccaattatccccacacataaaaaatcaaaacattaaactaacccccttcccccccccccacaacaaccctcttaaaactaattggctttttagaaacaccccacaaaagctcagaaattggctttaaaaaaaacaaccaccaaaaaaaatcaattggctaaaaaaaaaaagtattaaaaacgaattggctgagaaacaattggcaaaataaaggaatttggcactccccacccccctctttctcttctcccttggactttgagtcaaattggcctggacttgagtccctgaaccagcaaagagaaaagaagggccccagaaatcacaggtgggcacgtcgcgtctaccgccatctcccttctcacgggaattttcagggtaaact";
}
