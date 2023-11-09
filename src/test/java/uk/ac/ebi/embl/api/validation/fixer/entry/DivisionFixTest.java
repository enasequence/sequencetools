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

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;

public class DivisionFixTest {
  private DivisionFix divisionFix;
  public EntryFactory entryFactory;
  public FeatureFactory featureFactory;
  public SourceFeature sourceFeature;
  public Taxon taxon;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();

    sourceFeature = featureFactory.createSourceFeature();

    TaxonFactory taxonFactory = new TaxonFactory();
    JSONObject taxonJasonObject =
        new JSONObject(
            "{"
                + "\"taxId\": \"2759\","
                + "\"scientificName\": \"Felis catus\","
                + "\"commonName\": \"domestic cat\","
                + "\"formalName\": \"true\","
                + "\"rank\": \"superkingdom\","
                + "\"division\": \"INV\","
                + "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","
                + "\"geneticCode\": \"1\","
                + "\"mitochondrialGeneticCode\": \"1\","
                + "\"plastIdGeneticCode\": \"11\""
                + "}");
    taxon = taxonFactory.createTaxon(taxonJasonObject);

    divisionFix = new DivisionFix();
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(divisionFix.check(null).isValid());
  }

  @Test
  public void testCheck_NoPrimarySourceFeature() throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    assertTrue(divisionFix.check(entry).isValid());
  }

  @Test
  public void testCheck_entryWithValidDivision() throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    entry.setDivision("PHG");
    assertTrue(divisionFix.check(entry).isValid());
  }

  @Test
  public void testCheck_entryWithTransgenicFix() throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    sourceFeature.setTransgenic(true);
    entry.addFeature(sourceFeature);
    assertNull(entry.getDivision());
    ValidationResult result = divisionFix.check(entry);
    assertFalse(result.getMessages(Severity.FIX).isEmpty());
    assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
    assertEquals("TGN", entry.getDivision());
  }

  @Test
  public void testCheck_entryWithEnvironmentalSampleQualifierName()
      throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    sourceFeature.setSingleQualifier("environmental_sample");
    entry.addFeature(sourceFeature);
    assertNull(entry.getDivision());
    ValidationResult result = divisionFix.check(entry);
    assertFalse(result.getMessages(Severity.FIX).isEmpty());
    assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
    assertEquals("ENV", entry.getDivision());
  }

  @Test
  public void testCheck_entrySourceFeatureWithTaxId() throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    sourceFeature.setTaxon(taxon);
    entry.addFeature(sourceFeature);
    assertNull(entry.getDivision());
    ValidationResult result = divisionFix.check(entry);
    assertFalse(result.getMessages(Severity.FIX).isEmpty());
    assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
    assertEquals("INV", entry.getDivision());
  }

  @Test
  public void testCheck_entrySourceFeatureWithScientificName() throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    taxon.setTaxId(null); // Set taxId to null for getting division by ScientificName
    sourceFeature.setTaxon(taxon);
    entry.addFeature(sourceFeature);
    assertNull(entry.getDivision());
    ValidationResult result = divisionFix.check(entry);
    assertFalse(result.getMessages(Severity.FIX).isEmpty());
    assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
    assertEquals("MAM", entry.getDivision());
  }

  @Test
  public void testCheck_entrySourceFeatureWithInvalidTaxIdAndScientificName()
      throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    taxon.setTaxId(null);
    taxon.setScientificName("INVALID NAME");
    sourceFeature.setTaxon(taxon);
    entry.addFeature(sourceFeature);
    assertNull(entry.getDivision());
    ValidationResult result = divisionFix.check(entry);
    assertFalse(result.getMessages(Severity.FIX).isEmpty());
    assertEquals(1, result.count("DivisionFix_2", Severity.FIX)); // Set division to XXX
    assertEquals("XXX", entry.getDivision());
  }

  @Test
  public void testCheck_entryWithNCBIValidationScope() throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    divisionFix.setEmblEntryValidationPlanProperty(getProperty(ValidationScope.NCBI));
    entry.setDivision("PHG");
    assertTrue(divisionFix.check(entry).isValid());
    assertEquals("PHG", entry.getDivision());
  }

  @Test
  public void testCheck_entryWithNonNCBIValidationScope() throws ValidationEngineException {
    Entry entry = entryFactory.createEntry();
    sourceFeature.setTaxon(taxon);
    entry.addFeature(sourceFeature);
    divisionFix.setEmblEntryValidationPlanProperty(
        getProperty(ValidationScope.ASSEMBLY_CHROMOSOME));
    entry.setDivision("PHG");
    assertTrue(divisionFix.check(entry).isValid());
    assertEquals("INV", entry.getDivision());
  }

  private EmblEntryValidationPlanProperty getProperty(ValidationScope validationScope) {
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    property.validationScope.set(validationScope);
    return property;
  }
}
