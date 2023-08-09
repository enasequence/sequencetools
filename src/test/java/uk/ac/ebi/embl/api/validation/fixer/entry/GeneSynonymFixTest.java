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
import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.GENE_SYNONYM_NAME;
import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.LOCUS_TAG_QUALIFIER_NAME;

import java.util.Collection;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;

public class GeneSynonymFixTest {

  private Entry entry;
  private FeatureFactory featureFactory;
  private QualifierFactory qualifierFactory;
  private GeneSynonymFix check;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();

    entry = entryFactory.createEntry();

    check = new GeneSynonymFix();
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
  public void testCheck_NoLocusOrGeneTags() {
    Feature feature = featureFactory.createFeature("feature");
    Feature feature2 = featureFactory.createFeature("feature2");
    entry.addFeature(feature);
    entry.addFeature(feature2);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.getMessages().size());
  }

  /**
   * a cds feature has its gene_synonyms set as the master synonyms, which are added to a feature
   * with the same gene qualifier
   */
  @Test
  public void testFix1() {
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    entry.addFeature(feature1);

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("GeneSynonymFix", Severity.FIX);
    assertEquals(2, messages.size());
    assertEquals(2, feature2.getQualifiers(GENE_SYNONYM_NAME).size());
    String fixMessage =
        "Added gene_synonym \"synonym1\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.";

    /** we don't know which message will correspond as cant guarentee order, so check both */
    assertTrue(
        fixMessage.equals(messages.iterator().next().getMessage())
            || fixMessage.equals(messages.iterator().next().getMessage()));
  }

  /**
   * a cds feature has its gene_synonyms set as the master synonyms, which are added to a feature
   * with the same gene qualifier. An additional qualifier is removed from the non-master feature
   */
  @Test
  public void testFix2() {
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    entry.addFeature(feature1);

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "extra synonym"));
    entry.addFeature(feature2);

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("GeneSynonymFix2", Severity.FIX);
    assertEquals(1, messages.size());
    assertEquals(2, feature2.getQualifiers(GENE_SYNONYM_NAME).size());
    String fixMessage =
        "Removed gene_synonym \"extra synonym\" from feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.";
    String actualMessage = messages.iterator().next().getMessage();
    System.out.println(fixMessage.equals(actualMessage));
    assertEquals(fixMessage, actualMessage);
  }

  /**
   * a cds feature has its gene_synonyms set as the master synonyms, which are added to a feature
   * with the same gene qualifier. An additional qualifier which also has synonyms is not treated as
   * master due to not being CDS
   */
  @Test
  public void testFix3() {
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    entry.addFeature(feature1);

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym3"));
    entry.addFeature(feature2);

    Feature feature3 = featureFactory.createFeature("feature3");
    feature3.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature3);

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("GeneSynonymFix", Severity.FIX);
    assertEquals(2, messages.size());
    assertEquals(2, feature3.getQualifiers(GENE_SYNONYM_NAME).size());
    Iterator<ValidationMessage<Origin>> messageIterator = messages.iterator();
    assertEquals(
        "Added gene_synonym \"synonym1\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.",
        messageIterator.next().getMessage());
    assertEquals(
        "Added gene_synonym \"synonym2\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.",
        messageIterator.next().getMessage());
    assertEquals(1, result.getMessages("GeneSynonymFix2").size()); // 1 also removed from feature 2
  }

  /**
   * no CDS feature, so the first feature with synonyms is used as master, these synonyms are added
   * to a feature with the same gene qualifier.
   */
  @Test
  public void testFix4() {

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym3"));
    entry.addFeature(feature2);

    Feature feature3 = featureFactory.createFeature("feature3");
    feature3.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature3);

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("GeneSynonymFix", Severity.FIX);
    assertEquals(3, messages.size());
    assertEquals(3, feature3.getQualifiers(GENE_SYNONYM_NAME).size());
    Iterator<ValidationMessage<Origin>> messageIterator = messages.iterator();
    assertEquals(
        "Added gene_synonym \"synonym1\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.",
        messageIterator.next().getMessage());
    assertEquals(
        "Added gene_synonym \"synonym2\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.",
        messageIterator.next().getMessage());
    assertEquals(
        "Added gene_synonym \"synonym3\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.",
        messageIterator.next().getMessage());
  }

  /**
   * a cds feature has its gene_synonyms set as the master synonyms, which are added to a feature
   * with the same *locus_tag* qualifier - treated same as gene. An additional qualifier which also
   * has synonyms is not treated as master due to not being CDS
   */
  @Test
  public void testFix5() {
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    entry.addFeature(feature1);

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    feature2.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym3"));
    entry.addFeature(feature2);

    /**
     * this one has a locus tag, rather than a gene, but has the same value as gene so should be
     * recognized
     */
    Feature feature3 = featureFactory.createFeature("feature3");
    feature3.addQualifier(qualifierFactory.createQualifier(LOCUS_TAG_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature3);

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("GeneSynonymFix", Severity.FIX);
    assertEquals(2, messages.size());
    assertEquals(2, feature3.getQualifiers(GENE_SYNONYM_NAME).size());
    Iterator<ValidationMessage<Origin>> messageIterator = messages.iterator();
    assertEquals(
        "Added gene_synonym \"synonym1\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.",
        messageIterator.next().getMessage());
    assertEquals(
        "Added gene_synonym \"synonym2\" to feature sharing locus_tag/gene \"gene1\" - to create a stable list of gene_synonyms.",
        messageIterator.next().getMessage());
    assertEquals(1, result.getMessages("GeneSynonymFix2").size()); // 1 also removed from feature 2
  }

  /**
   * Two CDS features with same gene qualifier have different gene_synonyms - this means we cant
   * decide which list to use so will not attempt any fixes
   */
  @Test
  public void testFix6() {
    Feature feature1 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym1"));
    feature1.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    entry.addFeature(feature1);

    Feature feature2 = featureFactory.createFeature("feature2");
    feature2.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    entry.addFeature(feature2);

    Feature feature3 = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature3.addQualifier(qualifierFactory.createQualifier(Qualifier.GENE_QUALIFIER_NAME, "gene1"));
    feature3.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym2"));
    feature3.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, "synonym3"));
    entry.addFeature(feature3);

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("GeneSynonymFix", Severity.FIX);
    assertEquals(0, messages.size());
  }
}
