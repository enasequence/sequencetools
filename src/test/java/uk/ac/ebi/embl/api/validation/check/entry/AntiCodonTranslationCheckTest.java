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

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class AntiCodonTranslationCheckTest {
  private Entry entry;
  private AntiCodonTranslationCheck check, check1;
  public EntryFactory entryFactory;
  public FeatureFactory featureFactory;
  public LocationFactory locationFactory;
  public QualifierFactory qualifierFactory;
  public SequenceFactory sequenceFactory;
  private Feature feature;
  private Qualifier qualifier;
  private Sequence sequence;
  private SourceFeature source;
  private CdsFeature cds;
  private TaxonomyClient taxonClient;

  @Before
  public void setUp() throws SQLException {
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    qualifierFactory = new QualifierFactory();
    locationFactory = new LocationFactory();
    sequenceFactory = new SequenceFactory();
    check = new AntiCodonTranslationCheck();
    EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
    taxonClient = createMock(TaxonomyClient.class);
    property.taxonClient.set(taxonClient);
    check1 = new AntiCodonTranslationCheck();
    check1.setEmblEntryValidationPlanProperty(property);
    entry = entryFactory.createEntry();
    sequence =
        sequenceFactory.createSequenceByte(
            "gtcaagagcgcatcgctgataacgatgaggtcgcaagttcgattcttgctaggcccacca".getBytes());
    feature = featureFactory.createFeature(Feature.tRNA_FEATURE_NAME);
    cds = featureFactory.createCdsFeature();
    source = (SourceFeature) featureFactory.createFeature(Feature.SOURCE_FEATURE_NAME);
    source.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Metacrangonyx repens");
    source.addQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME, "mitochondrion");
    qualifier = qualifierFactory.createQualifier(Qualifier.ANTICODON_QUALIFIER_NAME);
    Join<Location> locationJoin = new Join<Location>();
    locationJoin.addLocation(locationFactory.createLocalRange(Long.valueOf(2), Long.valueOf(10)));
    locationJoin.addLocation(locationFactory.createLocalRange(Long.valueOf(12), Long.valueOf(30)));
    feature.setLocations(locationJoin);
    feature.getLocations().setComplement(false);
    qualifier.setValue("(pos:18..20,aa:Ile)");
    feature.addQualifier(qualifier);
    entry.addFeature(feature);
    entry.addFeature(cds);
    entry.addFeature(source);
    entry.setSequence(sequence);
  }

  @Test
  public void testCheck_AnticodonTranslationwithNoSequence() throws ValidationEngineException {
    ValidationResult result = check1.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("AntiCodonTranslationCheck_2", Severity.ERROR));
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoFeatures() throws ValidationEngineException {
    entry.clearFeatures();
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_NoAnticodon() throws ValidationEngineException {
    feature.removeQualifier(qualifier);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_AnticodonTranslation() throws ValidationEngineException {
    ValidationResult result = check1.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_AnticodonTranslationwithInvalidLocation() throws ValidationEngineException {
    feature.getLocations().getLocations().get(0).setComplement(true);
    feature.getLocations().getLocations().get(1).setComplement(true);
    ValidationResult result = check1.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("AntiCodonTranslationCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_AnticodonTranslationwithInvalidAminoAcid()
      throws ValidationEngineException {
    qualifier.setValue("(pos:4..6,aa:His)");
    ValidationResult result = check1.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("AntiCodonTranslationCheck_1", Severity.ERROR));
  }

  @Test
  public void testCheck_AnticodonTranslationwithSequence() throws ValidationEngineException {
    qualifier.setValue("(pos:18..20,aa:Ile,seq:gat)");
    ValidationResult result = check1.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("AntiCodonTranslationCheck_2", Severity.ERROR));
  }

  @Test
  public void testCheck_AnticodonTranslationwithinvalidSequence() throws ValidationEngineException {
    qualifier.setValue("(pos:18..20,aa:Ile,seq:gtt)");
    ValidationResult result = check1.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("AntiCodonTranslationCheck_2", Severity.ERROR));
  }

  @Test
  public void testCheck_SelenocysteineAnticodonTranslation() throws ValidationEngineException {
    qualifier.setValue("(pos:2..4,aa:SeC,seq:tca)");
    ValidationResult result = check1.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_SelenocysteineAnticodonInvalidSequenceandTranslation()
      throws ValidationEngineException {
    qualifier.setValue("(pos:3..5,aa:SeC,seq:tca)");
    ValidationResult result = check1.check(entry);
    assertFalse(result.isValid());
    assertEquals(1, result.count("AntiCodonTranslationCheck_1", Severity.ERROR));
    assertEquals(1, result.count("AntiCodonTranslationCheck_2", Severity.ERROR));
  }
}
