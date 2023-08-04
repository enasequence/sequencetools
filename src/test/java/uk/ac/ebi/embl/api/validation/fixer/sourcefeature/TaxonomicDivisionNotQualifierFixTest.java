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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;

public class TaxonomicDivisionNotQualifierFixTest {

  private Entry entry;
  private SourceFeature sourceFeature;
  FeatureFactory featureFactory;
  private TaxonomicDivisionNotQualifierFix check;
  private EmblEntryValidationPlanProperty property;
  private TaxonomyClient taxonomyClient;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    sourceFeature = featureFactory.createSourceFeature();
    property = new EmblEntryValidationPlanProperty();
    taxonomyClient = createMock(TaxonomyClient.class);
    property.taxonClient.set(taxonomyClient);

    DataRow dataRow1 = new DataRow(Qualifier.LAT_LON_QUALIFIER_NAME, "HUM");
    DataRow dataRow2 = new DataRow("dev_stage", "PRO");

    GlobalDataSets.addTestDataSet(
        GlobalDataSetFile.TAXONOMIC_DIVISION_NO_QUALIFIER, dataRow1, dataRow2);
    check = new TaxonomicDivisionNotQualifierFix();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
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
  public void testCheck_NoPrimaryFeature() {
    entry.addFeature(featureFactory.createFeature(Feature.CDS_FEATURE_NAME));
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoDivision() throws SQLException {
    TaxonFactory taxonFactory = new TaxonFactory();
    Taxon taxon = taxonFactory.createTaxon();
    sourceFeature.setScientificName("Homo sapiens");
    entry.addFeature(sourceFeature);
    expect(taxonomyClient.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
    replay(taxonomyClient);
    property.taxonClient.set(taxonomyClient);
    check.setEmblEntryValidationPlanProperty(property);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoDivisionQualifier() throws SQLException {
    TaxonFactory taxonFactory = new TaxonFactory();
    Taxon taxon = taxonFactory.createTaxon();
    sourceFeature.setScientificName("Homo sapiens");
    entry.addFeature(sourceFeature);
    expect(taxonomyClient.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
    replay(taxonomyClient);
    property.taxonClient.set(taxonomyClient);
    sourceFeature.setSingleQualifierValue(Qualifier.LAT_LON_QUALIFIER_NAME, "Akio Tani");
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_DivisionNotQualifier() throws SQLException {
    TaxonFactory taxonFactory = new TaxonFactory();
    Taxon taxon = taxonFactory.createTaxon();
    taxon.setDivision("HUM");
    sourceFeature.setScientificName("Homo sapiens");
    entry.addFeature(sourceFeature);
    expect(taxonomyClient.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
    replay(taxonomyClient);
    property.taxonClient.set(taxonomyClient);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("TaxonomicDivisionNotQualifierFix", Severity.ERROR));
  }

  @Test
  public void testCheck_DivisionInvalidQualifier() throws SQLException {
    TaxonFactory taxonFactory = new TaxonFactory();
    Taxon taxon = taxonFactory.createTaxon();
    taxon.setDivision("HUM");
    sourceFeature.setScientificName("Homo sapiens");
    entry.addFeature(sourceFeature);
    expect(taxonomyClient.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
    replay(taxonomyClient);
    property.taxonClient.set(taxonomyClient);
    check.setEmblEntryValidationPlanProperty(property);
    sourceFeature.setSingleQualifierValue(Qualifier.LAT_LON_QUALIFIER_NAME, "Akio Tani");
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("TaxonomicDivisionNotQualifierFix", Severity.FIX));
  }

  @Test
  public void testCheck_DivisionValidQualifier() throws SQLException {
    TaxonFactory taxonFactory = new TaxonFactory();
    Taxon taxon = taxonFactory.createTaxon();
    taxon.setDivision("HUM");
    sourceFeature.setScientificName("Homo sapiens");
    entry.addFeature(sourceFeature);
    expect(taxonomyClient.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
    replay(taxonomyClient);
    property.taxonClient.set(taxonomyClient);
    check.setEmblEntryValidationPlanProperty(property);
    Feature feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
    feature.setSingleQualifierValue(Qualifier.CITATION_QUALIFIER_NAME, "Akio Tani");
    ValidationResult result = check.check(entry);
    assertEquals(0, result.count("TaxonomicDivisionNotQualifierCheck_1", Severity.ERROR));
  }
}
