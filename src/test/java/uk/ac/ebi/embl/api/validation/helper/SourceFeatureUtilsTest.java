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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class SourceFeatureUtilsTest {

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
}
