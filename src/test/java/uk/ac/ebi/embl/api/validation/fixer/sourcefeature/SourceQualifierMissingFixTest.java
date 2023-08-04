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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class SourceQualifierMissingFixTest {

  private Entry entry;
  private SourceQualifierMissingFix check;
  public EntryFactory entryFactory;
  public FeatureFactory featureFactory;
  public QualifierFactory qualifierFactory;
  public EmblEntryValidationPlanProperty planProperty;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

    entryFactory = new EntryFactory();
    qualifierFactory = new QualifierFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    planProperty = new EmblEntryValidationPlanProperty();
    planProperty.taxonClient.set(new TaxonomyClient());
    check = new SourceQualifierMissingFix();
    check.setEmblEntryValidationPlanProperty(planProperty);
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoFeatures() {
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
  }

  @Test
  public void testCheck_NoPrimarySource() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu)"));
    entry.addFeature(feature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(0, validationResult.getMessages(Severity.FIX).size());
  }

  @Test
  public void testCheck_withPrimarySource() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    Qualifier organismQualifier =
        qualifierFactory.createQualifier(
            Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
    sourceFeature.addQualifier(organismQualifier);
    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(0, validationResult.getMessages(Severity.FIX).size());
  }

  @Test
  public void testCheck_withPrimarySourcewithUnculturedOrganim() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    sourceFeature.setSingleQualifierValue(
        Qualifier.ORGANISM_QUALIFIER_NAME, "uncultured bacterium");
    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(2, validationResult.getMessages(Severity.FIX).size());
  }

  @Test
  public void testCheck_withPrimarySourcewithMetagenomeOrganim() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    sourceFeature.setSingleQualifierValue(
        Qualifier.ORGANISM_QUALIFIER_NAME, "anaerobic digester metagenome");
    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(2, validationResult.getMessages(Severity.FIX).size());
    assertTrue(
        SequenceEntryUtils.isQualifierAvailable(
            Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME, entry));
    assertTrue(
        SequenceEntryUtils.isQualifierAvailable(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME, entry));
  }

  @Test
  public void testMetagenomeSourceSameAsSourceOrganism() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    Qualifier organismQualifier =
        qualifierFactory.createQualifier(
            Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium animalis D11");
    sourceFeature.addQualifier(organismQualifier);
    Qualifier metagenomeSourceQualifier =
        qualifierFactory.createQualifier(
            Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME, "Fusobacterium animalis D11");
    sourceFeature.addQualifier(metagenomeSourceQualifier);
    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.count("MetagenomeSourceQualifierRemoved", Severity.FIX));
  }

  @Test
  public void testMetagenomeSourceWithNoEnvSampleQual() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    Qualifier organismQualifier =
        qualifierFactory.createQualifier(
            Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
    sourceFeature.addQualifier(organismQualifier);
    Qualifier metagenomeSourceQualifier =
        qualifierFactory.createQualifier(
            Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME,
            "Anaerobic digester metagenome"); // anaerobic
    sourceFeature.addQualifier(metagenomeSourceQualifier);
    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(3, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.count("SourceQualifierMissingFix_2", Severity.FIX));
    assertEquals(1, validationResult.count("QualifierValueChange", Severity.FIX));
  }

  @Test
  public void testMetagenomeSourceWithEnvSampleQual() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    Qualifier organismQualifier =
        qualifierFactory.createQualifier(
            Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
    sourceFeature.addQualifier(organismQualifier);
    Qualifier metagenomeSourceQualifier =
        qualifierFactory.createQualifier(
            Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME, "anaerobic digester metagenome");
    sourceFeature.addQualifier(metagenomeSourceQualifier);

    sourceFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));

    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
  }

  @Test
  public void testCheck_withPrimarySourcewithenvironmentalLineage() {
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    sourceFeature.setSingleQualifierValue(
        Qualifier.ORGANISM_QUALIFIER_NAME, "Sulfurovum sp. enrichment culture clone C5");
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(2, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.count("SourceQualifierMissingFix_4", Severity.FIX));
    assertTrue(
        SequenceEntryUtils.isQualifierAvailable(
            Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME, entry));
  }

  @Test
  public void testCheckSourceWithStrainAndEnvSample() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    Qualifier organismQualifier =
        qualifierFactory.createQualifier(
            Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
    sourceFeature.addQualifier(organismQualifier);
    sourceFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));
    sourceFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.STRAIN_QUALIFIER_NAME));
    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(2, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.count("SourceQualifierMissingFix_6", Severity.FIX));
  }

  @Test
  public void testCheckSourceWithStrainAndEnvSampleAndIsolate() {
    Feature feature = featureFactory.createFeature("tRNA");
    feature.addQualifier(new AnticodonQualifier("(pos:10..12,aa:Glu,seq:tta)"));
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    Qualifier organismQualifier =
        qualifierFactory.createQualifier(
            Qualifier.ORGANISM_QUALIFIER_NAME, "Fusobacterium nucleatum subsp. animalis D11");
    sourceFeature.addQualifier(organismQualifier);
    sourceFeature.addQualifier(
        qualifierFactory.createQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));
    sourceFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.STRAIN_QUALIFIER_NAME));
    sourceFeature.addQualifier(qualifierFactory.createQualifier(Qualifier.ISOLATE_QUALIFIER_NAME));
    entry.addFeature(feature);
    entry.addFeature(sourceFeature);
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.isValid());
    assertEquals(2, validationResult.getMessages(Severity.FIX).size());
    assertEquals(1, validationResult.count("SourceQualifierMissingFix_5", Severity.FIX));
  }
}
