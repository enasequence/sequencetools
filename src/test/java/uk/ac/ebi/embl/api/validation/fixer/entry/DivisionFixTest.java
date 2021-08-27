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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DivisionFixTest
{

	private Entry entry;
	private DivisionFix check;
	public EntryFactory entryFactory;
	public FeatureFactory featureFactory;
	public SourceFeature sourceFeature;
	public Taxon taxon;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

		entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		sourceFeature = featureFactory.createSourceFeature();

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
		taxon = taxonFactory.createTaxon(taxonJasonObject);
		
		check = new DivisionFix();
	}

	@After
	public void tearDown() {
		GlobalDataSets.resetTestDataSets();
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoPrimarySourceFeature() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_entryWithValidDivision() throws ValidationEngineException {
		entry.setDivision("PHG");
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_entryWithTransgenicFix() throws ValidationEngineException {
		sourceFeature.setTransgenic(true);
		entry.addFeature(sourceFeature);
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
	}
	
	@Test
	public void testCheck_entryWithEnvironmentalSampleQualifierName() throws ValidationEngineException {
		sourceFeature.setSingleQualifier("environmental_sample");
		entry.addFeature(sourceFeature);
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_entrySourceFeatureWithTaxId() throws ValidationEngineException {
		sourceFeature.setTaxon(taxon);
		entry.addFeature(sourceFeature);
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_entrySourceFeatureWithScientificName() throws ValidationEngineException {
		taxon.setTaxId(null); // Set taxId to null for getting division by ScientificName
		sourceFeature.setTaxon(taxon);
		entry.addFeature(sourceFeature);
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DivisionFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_entrySourceFeatureWithInvalidTaxIdAndScientificName() throws ValidationEngineException {
		taxon.setTaxId(null); 
		taxon.setScientificName("INVALID NAME");
		sourceFeature.setTaxon(taxon);
		entry.addFeature(sourceFeature);
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DivisionFix_2", Severity.FIX)); // Set division to XXX
	}
}
