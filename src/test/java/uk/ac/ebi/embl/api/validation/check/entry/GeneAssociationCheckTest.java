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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class GeneAssociationCheckTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private QualifierFactory qualifierFactory;
  private GeneAssociationCheck check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();

    entry = entryFactory.createEntry();

    check = new GeneAssociationCheck();
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
    Feature feature = featureFactory.createFeature("feature");
    Feature feature2 = featureFactory.createFeature("feature2");
    entry.addFeature(feature);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_DifferentLocusAssociation() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(
        1, result.count(GeneAssociationCheck.MESSAGE_ID_DIFFERENT_LOCUS_VALUES, Severity.WARNING));
  }

  @Test
  public void testCheck_DifferentLocusAssociationMessage() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(
        1, result.count(GeneAssociationCheck.MESSAGE_ID_DIFFERENT_LOCUS_VALUES, Severity.WARNING));
    assertEquals(
        "Features sharing gene \"gene1\" are associated with \"\\locus_tag\" qualifiers with different values (\"bod\" and \"cod\")\".",
        result
            .getMessages(GeneAssociationCheck.MESSAGE_ID_DIFFERENT_LOCUS_VALUES, Severity.WARNING)
            .iterator()
            .next()
            .getMessage());
  }

  /** an expected, stable association of gene qualifiers and locus tags - no errors */
  @Test
  public void testCheck_LocusAssociationOkay2() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_LocusAssociationOkay() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature2);
    Feature feature3 = featureFactory.createFeature("feature3");
    feature3.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature3);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_DifferentPseudogeneAssociation() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "cod"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(
        1, result.count(GeneAssociationCheck.MESSAGE_ID_DIFFERENT_LOCUS_VALUES, Severity.WARNING));
  }

  @Test
  public void testCheck_DifferentPseudoAssociationMessage() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "cod"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(
        1, result.count(GeneAssociationCheck.MESSAGE_ID_DIFFERENT_LOCUS_VALUES, Severity.WARNING));
    assertEquals(
        "Features sharing gene \"gene1\" are associated with \"\\pseudogene\" qualifiers with different values (\"bod\" and \"cod\")\".",
        result
            .getMessages(GeneAssociationCheck.MESSAGE_ID_DIFFERENT_LOCUS_VALUES, Severity.WARNING)
            .iterator()
            .next()
            .getMessage());
  }

  /** an expected, stable association of gene qualifiers and pseudogenes- no errors */
  @Test
  public void testCheck_pseudogeneAssociationOkay2() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_PseudoAssociationOkay() {
    Feature feature1 = featureFactory.createFeature("feature1");
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.PSEUDOGENE_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature2);
    Feature feature3 = featureFactory.createFeature("feature3");
    feature3.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature3);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_rRNA_LocusAssociation() {
    Feature feature1 = featureFactory.createFeature(Feature.rRNA_FEATURE_NAME);
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "bod"));
    entry.addFeature(feature1);
    Feature feature2 = featureFactory.createFeature(Feature.rRNA_FEATURE_NAME);
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(
        qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, "cod"));
    entry.addFeature(feature2);
    Feature feature3 = featureFactory.createFeature(Feature.rRNA_FEATURE_NAME);
    feature3.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature3);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }
}
