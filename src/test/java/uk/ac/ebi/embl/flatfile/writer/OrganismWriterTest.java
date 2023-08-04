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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.StringWriter;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblOrganismWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblWriterTest;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;

public class OrganismWriterTest extends EmblWriterTest {

  public void testWrite_Taxon() throws IOException, JSONException {
    FeatureFactory featureFactory = new FeatureFactory();
    SourceFeature sourceFeature = (SourceFeature) featureFactory.createSourceFeature();
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
    TaxonFactory taxonFactory = new TaxonFactory();
    Taxon taxon = taxonFactory.createTaxon(taxonJasonObject);
    sourceFeature.setTaxon(taxon);
    entry.addFeature(sourceFeature);
    StringWriter writer = new StringWriter();
    new EmblOrganismWriter(entry, sourceFeature, wrapType).write(writer);
    // System.out.print(writer.toString());
    assertEquals(
        "OS   Felis catus (domestic cat)\n"
            + "OC   Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;\n"
            + "OC   Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis.\n",
        writer.toString());
  }

  public void testWrite_TwoTaxons() throws IOException, JSONException {
    FeatureFactory featureFactory = new FeatureFactory();
    SourceFeature sourceFeature = (SourceFeature) featureFactory.createSourceFeature();
    SourceFeature sourceFeature2 = (SourceFeature) featureFactory.createSourceFeature();
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
    JSONObject taxonJasonObject2 =
        new JSONObject(
            "{"
                + "\"scientificName\": \"Cactus\","
                + "\"commonName\": \"domestic plant\","
                + "\"lineage\": \"Eagle;BirdMetazoa;\","
                + "}");
    Taxon taxon = taxonFactory.createTaxon(taxonJasonObject);
    Taxon taxon2 = taxonFactory.createTaxon(taxonJasonObject2);

    sourceFeature.setTaxon(taxon);
    sourceFeature2.setTaxon(taxon2);
    entry.addFeature(sourceFeature);
    entry.addFeature(sourceFeature2);
    StringWriter writer = new StringWriter();
    assertTrue(new EmblOrganismWriter(entry, sourceFeature, wrapType).write(writer));
    assertTrue(new EmblOrganismWriter(entry, sourceFeature2, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "OS   Felis catus (domestic cat)\n"
            + "OC   Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;\n"
            + "OC   Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis.\n"
            + "OS   Cactus (domestic plant)\n"
            + "OC   Eagle; BirdMetazoa.\n",
        writer.toString());
  }

  public void testWrite_NoTaxon() throws IOException {
    FeatureFactory featureFactory = new FeatureFactory();
    SourceFeature sourceFeature = (SourceFeature) featureFactory.createSourceFeature();
    sourceFeature.setScientificName("Felis catus");
    entry.addFeature(sourceFeature);
    StringWriter writer = new StringWriter();
    assertTrue(new EmblOrganismWriter(entry, sourceFeature, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("OS   Felis catus\n" + "OC   unclassified sequences.\n", writer.toString());
  }
}
