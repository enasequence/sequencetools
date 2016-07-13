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
package uk.ac.ebi.embl.api.taxonomy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.taxonomy.Taxon;
import uk.ac.ebi.embl.api.taxonomy.TaxonFactory;

public class TaxonTest {

	private Taxon taxon;
	
	@Before
	public void setUp() throws Exception {
		taxon = new Taxon();
	}	
	
	@Test
	public void testTaxon() throws JSONException {
		assertNull(taxon.getTaxId());
		assertNull(taxon.getCommonName());
		assertNull(taxon.getDivision());
		assertNull(taxon.getGeneticCode());
		assertNull(taxon.getMitochondrialGeneticCode());
		assertNull(taxon.getPlastIdGeneticCode());
		assertNull(taxon.getRank());
		assertNull(taxon.getScientificName());
		assertNull(taxon.getLineage());
		assertFalse(taxon.isFormal());

		JSONObject taxonJasonObject=new JSONObject("{"+
				 "\"taxId\": \"2\","+
                 "\"scientificName\": \"organism\","+
                 "\"commonName\": \"name\","+
                 "\"formalName\": \"true\","+
                 "\"rank\": \"superkingdom\","+
                 "\"division\": \"INV\","+
                 "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
                 "\"geneticCode\": \"1\","+
                 "\"mitochondrialGeneticCode\": \"1\","+
                 "\"plastIdGeneticCode\": \"11\""+
				 "}");
		Taxon taxon1 = new Taxon(taxonJasonObject);
		assertEquals(new Long(2), taxon1.getTaxId());
		assertEquals("organism", taxon1.getScientificName());
		assertEquals("name", taxon1.getCommonName());
		assertTrue(taxon1.isFormal());
		assertEquals("superkingdom", taxon1.getRank());
		assertEquals("INV", taxon1.getDivision());
		assertEquals("Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;",taxon1.getLineage());
		assertEquals(14, taxon1.getFamilyNames().size());
		assertEquals(new Integer(1), taxon1.getGeneticCode());
		assertEquals(new Integer(1), taxon1.getMitochondrialGeneticCode());
		assertEquals(new Integer(11), taxon1.getPlastIdGeneticCode());
	}

		
	@Test
	public void testEquals() throws JSONException {
		assertTrue(taxon.equals(taxon));
		assertTrue(taxon.equals(new Taxon()));
		
		JSONObject taxonJasonObject1=new JSONObject("{"+
				 "\"taxId\": \"2\","+
                "\"scientificName\": \"organism\","+
                "\"commonName\": \"name\","+
                "\"formalName\": \"true\","+
                "\"rank\": \"superkingdom\","+
                "\"division\": \"INV\","+
                "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
                "\"geneticCode\": \"1\","+
                "\"mitochondrialGeneticCode\": \"1\","+
                "\"plastIdGeneticCode\": \"11\""+
				 "}");
		JSONObject taxonJasonObject2=new JSONObject("{"+
				 "\"taxId\": \"2\","+
               "\"scientificName\": \"organism\","+
               "\"commonName\": \"name\","+
               "\"formalName\": \"true\","+
               "\"rank\": \"superkingdom\","+
               "\"division\": \"INV\","+
               "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
               "\"geneticCode\": \"1\","+
               "\"mitochondrialGeneticCode\": \"1\","+
               "\"plastIdGeneticCode\": \"11\""+
				 "}");
		Taxon taxon1 = new Taxon(taxonJasonObject1);
		Taxon taxon2 = new Taxon(taxonJasonObject2);
		assertTrue(taxon1.equals(taxon2));
		assertTrue(taxon2.equals(taxon1));
		
		JSONObject taxonJasonObject3=new JSONObject("{"+
				 "\"taxId\": \"1\","+
              "\"scientificName\": \"organism\","+
              "\"commonName\": \"name\","+
              "\"formalName\": \"true\","+
              "\"rank\": \"superkingdom\","+
              "\"division\": \"INV\","+
              "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
              "\"geneticCode\": \"1\","+
              "\"mitochondrialGeneticCode\": \"1\","+
              "\"plastIdGeneticCode\": \"11\""+
				 "}");
		assertFalse(taxon1.equals(new Taxon(taxonJasonObject3)));
		
		taxonJasonObject3=new JSONObject("{"+
				 "\"taxId\": \"2\","+
             "\"scientificName\": \"x\","+
             "\"commonName\": \"name\","+
             "\"formalName\": \"true\","+
             "\"rank\": \"superkingdom\","+
             "\"division\": \"INV\","+
             "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
             "\"geneticCode\": \"1\","+
             "\"mitochondrialGeneticCode\": \"1\","+
             "\"plastIdGeneticCode\": \"11\""+
				 "}");
		assertFalse(taxon1.equals(new Taxon(taxonJasonObject3)));
	}
	
	@Test
	public void testEquals_WrongObject() {
		assertFalse(taxon.equals(new String()));
	}	

	@Test
	public void testToString() throws JSONException {
		JSONObject taxonJasonObject2=new JSONObject("{"+
				 "\"taxId\": \"2\","+
              "\"scientificName\": \"organism\","+
              "\"commonName\": \"name\","+
              "\"formalName\": \"true\","+
              "\"rank\": \"superkingdom\","+
              "\"division\": \"INV\","+
              "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
              "\"geneticCode\": \"1\","+
              "\"mitochondrialGeneticCode\": \"1\","+
              "\"plastIdGeneticCode\": \"11\""+
				 "}");
		assertNotNull(taxon.toString());
		assertNotNull(new Taxon(taxonJasonObject2).toString());
	}
	
	
	@Test
	public void testGetLineage_Empty() {
		assertNull(taxon.getLineage());
		assertEquals(0, taxon.getFamilyNames().size());
	}
	
	@Test
	public void testGetLineage() throws JSONException {
		TaxonFactory factory = new TaxonFactory();
		JSONObject taxonJasonObject=new JSONObject("{"+
				 "\"taxId\": \"2\","+
             "\"scientificName\": \"organism\","+
             "\"commonName\": \"name\","+
             "\"lineage\": \"Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;\","+
             "}");
		taxon=factory.createTaxon(taxonJasonObject);

		assertEquals("Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi; Mammalia;Eutheria; Laurasiatheria; Carnivora; Feliformia; Felidae; Felinae; Felis;", taxon.getLineage());
		assertEquals(14,taxon.getFamilyNames().size() );
	}

}
