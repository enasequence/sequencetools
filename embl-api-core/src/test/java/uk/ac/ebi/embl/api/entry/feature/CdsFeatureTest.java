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
package uk.ac.ebi.embl.api.entry.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class CdsFeatureTest {

	private Entry entry;
	private CdsFeature cdsFeature;
	private SourceFeature sourceFeature;
	private EntryFactory entryFactory = new EntryFactory();
	private FeatureFactory featureFactory = new FeatureFactory();


	@Before
	public void setUp() throws Exception {
		entry = entryFactory.createEntry();
		cdsFeature = featureFactory.createCdsFeature();
		sourceFeature = featureFactory.createSourceFeature();
		entry.addFeature(cdsFeature);
		entry.addFeature(sourceFeature);
	}

	@Test
	public void testCdsFeature() {
		assertEquals(CdsFeature.CDS_FEATURE_NAME, cdsFeature.getName());
		assertTrue(cdsFeature.getLocations() instanceof Join<?>);
		assertTrue(cdsFeature.getQualifiers().isEmpty());
		assertTrue(cdsFeature.getXRefs().isEmpty());
		assertNotNull(cdsFeature.getLocations());
	}

	@Test
	public void testGetTranslation() {
		assertNull(cdsFeature.getTranslation());
		QualifierFactory factory = new QualifierFactory();
		cdsFeature.addQualifier(factory.createQualifier(
				Qualifier.TRANSLATION_QUALIFIER_NAME, "ttt"));
		assertEquals("ttt", cdsFeature.getTranslation());
	}
	
	@Test
	public void testSetTranslation() {
		assertNull(cdsFeature.getTranslation());
		assertTrue(cdsFeature.getQualifiers(
				Qualifier.TRANSLATION_QUALIFIER_NAME).isEmpty());

		cdsFeature.setTranslation("trans");
		
		assertEquals("trans", cdsFeature.getTranslation());
		assertEquals(1, cdsFeature.getQualifiers(
				Qualifier.TRANSLATION_QUALIFIER_NAME).size());
		assertEquals("trans", cdsFeature.getSingleQualifierValue(
				Qualifier.TRANSLATION_QUALIFIER_NAME));
		
		cdsFeature.setTranslation("trans2");
		
		assertEquals("trans2", cdsFeature.getTranslation());
		assertEquals(1, cdsFeature.getQualifiers(
				Qualifier.TRANSLATION_QUALIFIER_NAME).size());
		assertEquals("trans2", cdsFeature.getSingleQualifierValue(
				Qualifier.TRANSLATION_QUALIFIER_NAME));
	}	

	@Test
	public void testGetProteinAccession() throws ValidationException {
		assertNull(cdsFeature.getProteinAccession());
		QualifierFactory factory = new QualifierFactory();
		cdsFeature.addQualifier(factory.createProteinIdQualifier("BNA00001.3"));
		assertEquals("BNA00001", cdsFeature.getProteinAccession());
		
	}
	
	@Test
	public void testSetProteinAccession() throws ValidationException {
		assertNull(cdsFeature.getProteinAccession());
		assertTrue(cdsFeature.getQualifiers(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME).isEmpty());

		cdsFeature.setSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME, "BAA00001.1");
		
		assertEquals("BAA00001", cdsFeature.getProteinAccession());
		assertEquals(1, cdsFeature.getQualifiers(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME).size());
		assertEquals("BAA00001.1", cdsFeature.getSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME));
		
		cdsFeature.setSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME, "BAA00012.1");
		
		assertEquals("BAA00012", cdsFeature.getProteinAccession());
		assertEquals(1, cdsFeature.getQualifiers(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME).size());
		assertEquals("BAA00012.1", cdsFeature.getSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME));
	}	

	@Test
	public void testGetProteinVersion() throws ValidationException {
		assertNull(cdsFeature.getProteinVersion());
		QualifierFactory factory = new QualifierFactory();
		cdsFeature.addQualifier(factory.createProteinIdQualifier("BAA00001.3"));
		assertEquals(new Integer(3), cdsFeature.getProteinVersion());
	}
	
	@Test
	public void testSetProteinVersion() throws ValidationException {
		assertNull(cdsFeature.getProteinVersion());
		assertTrue(cdsFeature.getQualifiers(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME).isEmpty());

		cdsFeature.setSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME, "BAA00001.1");
		
		assertEquals(new Integer(1), cdsFeature.getProteinVersion());
		assertEquals(1, cdsFeature.getQualifiers(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME).size());
		assertEquals("BAA00001.1", cdsFeature.getSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME));
		
		cdsFeature.setSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME, "BAA00001.2");
		
		assertEquals(new Integer(2), cdsFeature.getProteinVersion());
		assertEquals(1, cdsFeature.getQualifiers(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME).size());
		assertEquals("BAA00001.2", cdsFeature.getSingleQualifierValue(
				Qualifier.PROTEIN_ID_QUALIFIER_NAME));
	}	
	
	@Test
	public void testException() {
		assertFalse(cdsFeature.isException());
		cdsFeature.setException("A");
		assertTrue(cdsFeature.isException());
		assertEquals("A", cdsFeature.getException());
		cdsFeature.setException("B");
		assertTrue(cdsFeature.isException());
		assertEquals("B", cdsFeature.getException());
		cdsFeature.setException(null);
		assertFalse(cdsFeature.isException());
		cdsFeature.setException(null);
		assertFalse(cdsFeature.isException());
	}

	@Test
	public void testPseudo() {
		assertFalse(cdsFeature.isPseudo());
		cdsFeature.setPseudo(true);
		assertTrue(cdsFeature.isPseudo());
		cdsFeature.setPseudo(true);
		assertTrue(cdsFeature.isPseudo());
		cdsFeature.setPseudo(false);
		assertFalse(cdsFeature.isPseudo());
		cdsFeature.setPseudo(false);
		assertFalse(cdsFeature.isPseudo());
	}

	@Test
	public void testCodonStart() throws ValidationException {
		assertNull(cdsFeature.getStartCodon());
		cdsFeature.setStartCodon(1);
		assertEquals(new Integer(1), cdsFeature.getStartCodon());
		cdsFeature.setStartCodon(2);
		assertEquals(new Integer(2), cdsFeature.getStartCodon());
		cdsFeature.setStartCodon(null);
		assertNull(cdsFeature.getStartCodon());
		cdsFeature.setStartCodon(null);
		assertNull(cdsFeature.getStartCodon());
	}	
	
	@Test
	public void testTranslation() {
		assertNull(cdsFeature.getTranslation());
		cdsFeature.setTranslation("A");
		assertEquals("A", cdsFeature.getTranslation());
		cdsFeature.setTranslation("B");
		assertEquals("B", cdsFeature.getTranslation());
		cdsFeature.setTranslation(null);
		assertNull(cdsFeature.getTranslation());
		cdsFeature.setTranslation(null);
		assertNull(cdsFeature.getTranslation());
	}
		
	@Test
	public void testTranslationTable() throws ValidationException {
		assertNull(cdsFeature.getTranslationTable());
		cdsFeature.setTranslationTable(1);
		assertEquals(new Integer(1), cdsFeature.getTranslationTable());
		cdsFeature.setTranslationTable(2);
		assertEquals(new Integer(2), cdsFeature.getTranslationTable());
		cdsFeature.setTranslationTable(null);
		assertNull(cdsFeature.getTranslationTable());
		cdsFeature.setTranslationTable(null);
		assertNull(cdsFeature.getTranslationTable());
	}
	
}
