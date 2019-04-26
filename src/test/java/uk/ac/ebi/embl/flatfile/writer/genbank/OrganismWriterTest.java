/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.flatfile.writer.genbank;
 
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.flatfile.writer.genbank.OrganismWriter;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;

public class OrganismWriterTest extends GenbankWriterTest {

    public void testWrite_Classification() throws IOException, JSONException {
    	FeatureFactory featureFactory = new FeatureFactory();
    	SourceFeature sourceFeature = (SourceFeature) featureFactory.createSourceFeature();
		TaxonFactory taxonFactory = new TaxonFactory();
		JSONObject taxonJasonObject= new JSONObject("{"+
                "\"taxId\": \"2759\","+
                "\"scientificName\": \"Felis catus\","+
                "\"commonName\": \"domestic cat\","+
                "\"formalName\": \"true\","+
                "\"rank\": \"superkingdom\","+
                "\"division\": \"INV\","+
                "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
                "\"geneticCode\": \"1\","+
                "\"mitochondrialGeneticCode\": \"1\","+
                "\"plastIdGeneticCode\": \"11\""+
                  "}");
        Taxon taxon = taxonFactory.createTaxon(taxonJasonObject);
		sourceFeature.setTaxon(taxon);
		entry.addFeature(sourceFeature);    	
        StringWriter writer = new StringWriter();
        assertTrue(new OrganismWriter(entry, sourceFeature, wrapType).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"  ORGANISM  Felis catus\n" +
                "            Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi;\n" +
                "            Mammalia; Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae;\n" +
                "            Felinae; Felis.\n",
                writer.toString());
    }

    public void testWrite_NoClassification() throws IOException {
    	FeatureFactory featureFactory = new FeatureFactory();
    	SourceFeature sourceFeature = (SourceFeature) featureFactory.createSourceFeature();
    	sourceFeature.setTaxon(null);	
    	entry.addFeature(sourceFeature);    	
        StringWriter writer = new StringWriter();
        new OrganismWriter(entry, sourceFeature, wrapType).write(writer);
        //System.out.print(writer.toString());
        assertEquals(
        		"  ORGANISM  unclassified sequences.\n", 
        		writer.toString());
    }
}
