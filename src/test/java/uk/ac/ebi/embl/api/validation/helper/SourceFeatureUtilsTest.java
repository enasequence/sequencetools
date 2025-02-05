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
package uk.ac.ebi.embl.api.validation.helper;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class SourceFeatureUtilsTest {

  private TaxonomyClient taxonomyClient = new TaxonomyClient();

  @Test
  public void constructSourceFeature() {
    String sampleId = "ERS4477947";
    SourceFeature source =
        new SourceFeatureUtils().constructSourceFeature(getSample(sampleId), new TaxonomyClient());
    assertEquals(7, source.getQualifiers().size()); // 6 from sample + organism qualifier
    assertEquals(
        "2020-03-09",
        source.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME).getValue());
    assertEquals(
        "Spain:Valencia",
        source.getSingleQualifier(Qualifier.GEO_LOCATION_QUALIFIER_NAME).getValue());
    // expects direction (N) added to latitude, and longitude with existing direction returned as it
    // is - both cases checked
    assertEquals(
        "39.47 N 0.38 E", source.getSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME).getValue());
    assertEquals(
        "Homo sapiens", source.getSingleQualifier(Qualifier.HOST_QUALIFIER_NAME).getValue());
    assertEquals(
        "hCoV-19/Spain/Valencia27/2020",
        source.getSingleQualifier(Qualifier.ISOLATE_QUALIFIER_NAME).getValue());
    assertEquals(
        "Severe acute respiratory syndrome coronavirus 2",
        source.getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME).getValue());
    assertEquals("GISAID123", source.getSingleQualifier(Qualifier.NOTE_QUALIFIER_NAME).getValue());
    Taxon taxon = source.getTaxon();
    assertEquals(Long.valueOf(2697049), taxon.getTaxId());
    assertEquals("Severe acute respiratory syndrome coronavirus 2", taxon.getScientificName());
  }

  // This to do comment was moved here from its previous spot before the refactor.
  // TODO: ask about lat lon unit
  private Sample getSample(String sampleId) {
    Sample sample = new Sample();
    sample.setSraSampleId(sampleId);
    sample.setName("hCoV-19/Spain/Valencia27/2020");
    sample.setOrganism("Severe acute respiratory syndrome coronavirus 2");
    sample.setTaxId(2697049);

    List<Attribute> attributes = new ArrayList<>();
    attributes.add(
        new Attribute("sample_description", "hCoV-19/Spain/Valencia27/2020", null, null, null));
    attributes.add(new Attribute("collection date", "2020-03-09", null, null, null)); // 1
    attributes.add(new Attribute("geographic location (latitude)", "39.47", null, null, null)); // 3
    attributes.add(
        new Attribute("geographic location (longitude)", "0.38 E", null, null, null)); // 3
    attributes.add(
        new Attribute(
            "geographic location (region and locality)", "Valencia", null, null, null)); // 2
    attributes.add(new Attribute("host common name", "Human", null, null, null));
    attributes.add(
        new Attribute("geographic location (country and/or sea)", "Spain", null, null, null)); // 2
    attributes.add(new Attribute("host subject id", "18218863", null, null, null));
    attributes.add(new Attribute("host age", "81", null, null, null));
    attributes.add(new Attribute("host health state", "not provided", null, null, null));
    attributes.add(new Attribute("host sex", "male", null, null, null));
    attributes.add(new Attribute("host scientific name", "Homo sapiens", null, null, null)); // 4
    attributes.add(
        new Attribute("isolate", "hCoV-19/Spain/Valencia27/2020", null, null, null)); // 5
    attributes.add(new Attribute("GISAID Accession ID", "GISAID123", null, null, null)); // 6
    attributes.add(
        new Attribute(
            "isolation source host-associated", "Nasopharyngeal exudate", null, null, null));
    attributes.add(new Attribute("ENA-CHECKLIST", "ERC000033", null, null, null));

    sample.setAttributes(attributes);

    return sample;
  }

  @Test
  public void testAddQualifiersWithCommonAttributes() {

    List<Attribute> attributes =
        List.of(
            new Attribute("collection date", "2020-03-09", null, null, null),
            new Attribute("geographic location (latitude)", "39.47 N", null, null, null),
            new Attribute("geographic location (longitude)", "0.38 E", null, null, null),
            new Attribute("geographic location (country and/or sea)", "Spain", null, null, null),
            new Attribute(
                "geographic location (region and locality)", "Valencia", null, null, null),
            new Attribute("host common name", "Human", null, null, null),
            new Attribute("host scientific name", "Homo sapiens", null, null, null),
            new Attribute("environment (material)", "QCRA", null, null, null),
            // Test no value qualifier
            new Attribute(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME, "", null, null, null),
            new Attribute(Qualifier.STRAIN_QUALIFIER_NAME, "", null, null, null));

    Sample sample = createSampleWithAttributes(attributes);
    SourceFeature sourceFeature =
        new SourceFeatureUtils().constructSourceFeature(sample, taxonomyClient);

    assertEquals(
        "2020-03-09",
        sourceFeature.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME).getValue());
    assertEquals(
        "Spain:Valencia",
        sourceFeature.getSingleQualifier(Qualifier.GEO_LOCATION_QUALIFIER_NAME).getValue());
    assertEquals(
        "QCRA",
        sourceFeature.getSingleQualifier(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).getValue());
    assertNotNull(sourceFeature.getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));
    assertNull(
        sourceFeature.getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME).getValue());
    assertNull(sourceFeature.getSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME));
    assertNull(sourceFeature.getSingleQualifier(Qualifier.HOST_QUALIFIER_NAME));
    assertNull(sourceFeature.getSingleQualifier(Qualifier.STRAIN_QUALIFIER_NAME));
  }

  @Test
  public void testAddQualifiersWithCovid19Attributes() {
    List<Attribute> attributes =
        List.of(
            new Attribute("collection date", "2020-03-09", null, null, null),
            new Attribute("geographic location (latitude)", "39.47", null, null, null),
            new Attribute("geographic location (longitude)", "0.38 E", null, null, null),
            new Attribute("geographic location (country and/or sea)", "Spain", null, null, null),
            new Attribute(
                "geographic location (region and locality)", "Valencia", null, null, null),
            new Attribute("host scientific name", "Homo sapiens", null, null, null),
            new Attribute("GISAID Accession ID", "GISAID123", null, null, null),
            new Attribute("metagenome_source", "metagenome value", null, null, null),
            new Attribute("environmental_sample", "", null, null, null));

    Sample sample = createSampleWithAttributes(attributes);
    sample.setTaxId(2697049); // Covid-19 Tax ID
    SourceFeature sourceFeature =
        new SourceFeatureUtils().constructSourceFeature(sample, taxonomyClient);
    assertEquals(
        "2020-03-09",
        sourceFeature.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME).getValue());
    assertEquals(
        "Spain:Valencia",
        sourceFeature.getSingleQualifier(Qualifier.GEO_LOCATION_QUALIFIER_NAME).getValue());
    assertEquals(
        "39.47 N 0.38 E",
        sourceFeature.getSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME).getValue());
    assertEquals(
        "Homo sapiens", sourceFeature.getSingleQualifier(Qualifier.HOST_QUALIFIER_NAME).getValue());
    assertEquals(
        "GISAID123", sourceFeature.getSingleQualifier(Qualifier.NOTE_QUALIFIER_NAME).getValue());
    assertEquals(
        "metagenome value",
        sourceFeature.getSingleQualifier(Qualifier.METAGENOME_SOURCE_QUALIFIER_NAME).getValue());
    assertNotNull(sourceFeature.getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME));
    assertNull(
        sourceFeature.getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME).getValue());
  }

  @Test
  public void testAddQualifiersWithInvalidAttributes() {
    List<Attribute> attributes =
        List.of(
            new Attribute("collection date", "Invalid Date", null, null, null),
            new Attribute("geographic location (latitude)", "Invalid Latitude", null, null, null),
            new Attribute(
                "geographic location (longitude)", "Invalid Longitude", null, null, null));

    Sample sample = createSampleWithAttributes(attributes);
    SourceFeature sourceFeature =
        new SourceFeatureUtils().constructSourceFeature(sample, taxonomyClient);
    assertNull(sourceFeature.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME));
    assertNull(sourceFeature.getSingleQualifier(Qualifier.GEO_LOCATION_QUALIFIER_NAME));
    assertNull(sourceFeature.getSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME));
  }

  @Test
  public void testIsolationSource() {

    List<Attribute> attributes =
        List.of(
            new Attribute("environment (material)", "QCRA", null, null, null),
            new Attribute("isolation_source", "mantle tissue", null, null, null));

    Sample sample = createSampleWithAttributes(attributes);
    SourceFeature sourceFeature =
        new SourceFeatureUtils().constructSourceFeature(sample, taxonomyClient);

    assertEquals(
        "mantle tissue",
        sourceFeature.getSingleQualifier(Qualifier.ISOLATION_SOURCE_QUALIFIER_NAME).getValue());
  }

  @Test
  public void testAttributesWithNullKeyAndValue() {

    List<Attribute> attributes =
        List.of(
            new Attribute("collection date", null, null, null, null),
            new Attribute(null, "mantle tissue", null, null, null));

    Sample sample = createSampleWithAttributes(attributes);
    SourceFeature sourceFeature =
        new SourceFeatureUtils().constructSourceFeature(sample, taxonomyClient);

    assertNull(sourceFeature.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME));
    assertEquals(1, sourceFeature.getQualifiers().size());
  }

  @Test
  public void testEnforceQualifierFromSample() {

    FeatureFactory featureFactory = new FeatureFactory();
    SourceFeature sourceFeature = featureFactory.createSourceFeature();
    sourceFeature.addQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME,"2025-02-05");
    assertEquals( sourceFeature.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME).getValue(),"2025-02-05");
    //Enforce qualifier value from sample
    new SourceFeatureUtils().enforceQualifierFromSample(sourceFeature, Qualifier.COLLECTION_DATE_QUALIFIER_NAME,"2020-02-10");
    assertEquals( sourceFeature.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME).getValue(),"2020-02-10");
  }

  private Sample createSampleWithAttributes(List<Attribute> attributes) {
    Sample sample = new Sample();
    sample.setOrganism("Test Organism");
    sample.setAttributes(attributes);
    return sample;
  }
}
