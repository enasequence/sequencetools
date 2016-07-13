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
package uk.ac.ebi.embl.api.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class EntryTest {

	private Entry entry; 
	
	@Before
	public void setUp() {
		this.entry = new Entry();		
	}

	@Test
	public void testGetSecondaryAccessions() {
		assertEquals(0, entry.getSecondaryAccessions().size());
	}

	@Test
	public void testAddSecondaryAccession() {
		assertTrue(entry.addSecondaryAccession(new Text("accession")));
		assertEquals("accession", entry.getSecondaryAccessions().get(0).getText());
	}

	@Test
	public void testAddSecondaryAccessions() {
		assertTrue(entry.addSecondaryAccessions(Arrays.asList(new Text("accession"))));
		assertEquals("accession", entry.getSecondaryAccessions().get(0).getText());
	}

	@Test
	public void testAddSecondaryAccessions_Null() {
		assertFalse(entry.addSecondaryAccessions(null));
		assertTrue(entry.getSecondaryAccessions().isEmpty());
	}
	
	@Test
	public void testRemoveSecondaryAccession() {
		assertFalse(entry.removeSecondaryAccession(new Text("accession")));
		
		entry.addSecondaryAccession(new Text("accession"));
		assertEquals(1, entry.getSecondaryAccessions().size());
		
		assertFalse(entry.removeSecondaryAccession(new Text("x")));
		assertEquals(1, entry.getSecondaryAccessions().size());
		
		assertTrue(entry.removeSecondaryAccession(new Text("accession")));
		assertEquals(0, entry.getSecondaryAccessions().size());
	}

	@Test
	public void testGetKeywords() {
		assertEquals(0, entry.getKeywords().size());
	}

	@Test
	public void testAddKeyword() {
		assertTrue(entry.addKeyword(new Text("keyword")));
		assertEquals("keyword", entry.getKeywords().get(0).getText());
	}

	@Test
	public void testAddKeywords() {
		assertTrue(entry.addKeywords(Arrays.asList(new Text("keyword"))));
		assertEquals("keyword", entry.getKeywords().get(0).getText());
	}

	@Test
	public void testAddKeywords_Null() {
		assertFalse(entry.addKeywords(null));
		assertTrue(entry.getKeywords().isEmpty());
	}

	@Test
	public void testRemoveKeyword() {
		assertFalse(entry.removeKeyword(new Text("keyword")));
		
		entry.addKeyword(new Text("keyword"));
		assertEquals(1, entry.getKeywords().size());
		
		assertFalse(entry.removeKeyword(new Text("x")));
		assertEquals(1, entry.getKeywords().size());
		
		assertTrue(entry.removeKeyword(new Text("keyword")));
		assertEquals(0, entry.getKeywords().size());
	}

	@Test
	public void testGetProjectAccessions() {
		assertEquals(0, entry.getProjectAccessions().size());
	}
	
	@Test
	public void testAddProjectAccession() {
		assertTrue(entry.addProjectAccession(new Text("accession")));
		assertEquals("accession", entry.getProjectAccessions().get(0).getText());
	}

	@Test
	public void testAddProjectAccessions() {
		assertTrue(entry.addProjectAccessions(Arrays.asList(new Text("accession"))));
		assertEquals("accession", entry.getProjectAccessions().get(0).getText());
	}

	@Test
	public void testAddProjectAccessions_Null() {
		assertFalse(entry.addProjectAccessions(null));
		assertTrue(entry.getProjectAccessions().isEmpty());
	}
	
	@Test
	public void testRemoveProjectAccession() {
		assertFalse(entry.removeProjectAccession(new Text("accession")));
		
		entry.addProjectAccession(new Text("accession"));
		assertEquals(1, entry.getProjectAccessions().size());
		
		assertFalse(entry.removeProjectAccession(new Text("x")));
		assertEquals(1, entry.getProjectAccessions().size());
		
		assertTrue(entry.removeProjectAccession(new Text("accession")));
		assertEquals(0, entry.getProjectAccessions().size());
	}

	@Test
	public void testGetFeatures() {
		assertEquals(0, entry.getFeatures().size());
	}
	
	@Test
	public void testAddFeature() {
		Feature feature = new FeatureFactory().createFeature("x", false);
		assertTrue(entry.addFeature(feature));
		assertEquals(feature, entry.getFeatures().get(0));
	}

	@Test
	public void testAddFeatures() {
		Feature feature = new FeatureFactory().createFeature("x", false);
		assertTrue(entry.addFeatures(Arrays.asList(feature)));
		assertEquals(feature, entry.getFeatures().get(0));
	}

	@Test
	public void testAddFeatures_Null() {
		assertFalse(entry.addFeatures(null));
		assertTrue(entry.getFeatures().isEmpty());
	}
	
	@Test
	public void testRemoveFeature() {
		Feature feature = new FeatureFactory().createFeature("feature", 
				false);
		Feature featureX = new FeatureFactory().createFeature("x", false);
		assertFalse(entry.removeFeature(feature));
		
		entry.addFeature(feature);
		assertEquals(1, entry.getFeatures().size());
		
		assertFalse(entry.removeFeature(featureX));
		assertEquals(1, entry.getFeatures().size());
		
		assertTrue(entry.removeFeature(feature));
		assertEquals(0, entry.getFeatures().size());
	}

	@Test
	public void testGetReferences() {
		assertEquals(0, entry.getReferences().size());
	}
	
	@Test
	public void testAddReference() {
		Reference reference = new ReferenceFactory().createReference(
				new ReferenceFactory().createArticle("t", "j"), 1);
		assertTrue(entry.addReference(reference));
		assertEquals(reference, entry.getReferences().get(0));
	}

	@Test
	public void testAddReferences() {
		Reference reference = new ReferenceFactory().createReference(
				new ReferenceFactory().createArticle("t", "j"), 1);
		assertTrue(entry.addReferences(Arrays.asList(reference)));
		assertEquals(reference, entry.getReferences().get(0));
	}

	@Test
	public void testAddReferences_Null() {
		assertFalse(entry.addReferences(null));
		assertTrue(entry.getReferences().isEmpty());
	}
	
	@Test
	public void testRemoveReference() {
		ReferenceFactory factory = new ReferenceFactory();
		Reference refX = factory.createReference(
				factory.createArticle("x", "j"), 1);
		Reference refY = factory.createReference(
				factory.createArticle("y", "j"), 1);
		assertFalse(entry.removeReference(refX));
		
		entry.addReference(refX);
		assertEquals(1, entry.getReferences().size());
		
		assertFalse(entry.removeReference(refY));
		assertEquals(1, entry.getReferences().size());
		
		assertTrue(entry.removeReference(refX));
		assertEquals(0, entry.getReferences().size());
	}

	@Test
	public void testGetXRefs() {
		assertEquals(0, entry.getXRefs().size());
	}
	
	@Test
	public void testAddXRef() {
		XRef xRef = new XRef("db", "pa", "sa");
		assertTrue(entry.addXRef(xRef));
		assertEquals(xRef, entry.getXRefs().get(0));
	}

	@Test
	public void testAddXRefs() {
		XRef xRef = new XRef("db", "pa", "sa");
		assertTrue(entry.addXRefs(Arrays.asList(xRef)));
		assertEquals(xRef, entry.getXRefs().get(0));
	}

	@Test
	public void testAddXRefs_Null() {
		assertFalse(entry.addXRefs(null));
		assertTrue(entry.getXRefs().isEmpty());
	}
	
	@Test
	public void testRemoveXRef() {
		XRef xRef = new XRef("db", "pa", "sa");
		XRef x = new XRef("x", "x", "x");
		assertFalse(entry.removeXRef(xRef));
		
		entry.addXRef(xRef);
		assertEquals(1, entry.getXRefs().size());
		
		assertFalse(entry.removeXRef(x));
		assertEquals(1, entry.getXRefs().size());
		
		assertTrue(entry.removeXRef(xRef));
		assertEquals(0, entry.getXRefs().size());
	}
	
	@Test
	public void testAssemblyEntry() {
		assertTrue(entry.getAssemblies().isEmpty());
		assertNull(entry.getDescription().getText());		
	}
	
	@Test
	public void testGetAssemblies() {
		assertEquals(0, entry.getAssemblies().size());
	}
	
	@Test
	public void testAddAssembly() {
		Assembly assembly = new EntryFactory().createAssembly(
				"x", 1, 1L, 3L, false, 2L, 4L);
		assertTrue(entry.addAssembly(assembly));
		assertEquals(assembly, entry.getAssemblies().get(0));
	}
	
	@Test
	public void testAddAssemblies() {
		Assembly assembly = new EntryFactory().createAssembly(
				"x", 1, 1L, 3L, false, 2L, 4L);
		assertTrue(entry.addAssemblies(Arrays.asList(assembly)));
		assertEquals(assembly, entry.getAssemblies().get(0));
	}
	
	@Test
	public void testAddAssemblies_Null() {
		assertFalse(entry.addAssemblies(null));
		assertTrue(entry.getAssemblies().isEmpty());
	}
	
	@Test
	public void testRemoveAssembly() {
		Assembly assemblyX = new EntryFactory().createAssembly(
				"x", 1, 1L, 3L, false, 2L, 4L);
		Assembly assemblyY = new EntryFactory().createAssembly(
				"y", 1, 1L, 3L, false, 2L, 4L);
		
		assertFalse(entry.removeAssembly(assemblyY));
		
		entry.addAssembly(assemblyY);
		assertEquals(1, entry.getAssemblies().size());
		
		assertFalse(entry.removeAssembly(assemblyX));
		assertEquals(1, entry.getAssemblies().size());
		
		assertTrue(entry.removeAssembly(assemblyY));
		assertEquals(0, entry.getAssemblies().size());
	}	
	
	@Test
	public void testHashCode() {
		entry.hashCode();
		new Entry().hashCode();
	}
	
	@Test
	public void testEqualsObject() {
		Entry e1 = new Entry();
		Entry e2 = new Entry();
		Entry e3 = new Entry();
		e1.setDescription(new Text("A"));
		e2.setDescription(new Text("A"));
		e3.setDescription(new Text("B"));
		
		assertTrue(e1.equals(e1));
		assertTrue(e1.equals(e2));
		assertTrue(e2.equals(e1));
		
		assertFalse(e1.equals(e3));
		assertFalse(e3.equals(e1));
	}
	
	@Test
	public void testEqualsObject_WrongObject() {
		assertFalse(new Entry().equals(new String()));
	}
	
/*	@Test
	public void testCompareTo() {
		Entry e1 = new Entry();
		Entry e2 = new Entry();

		e1.setId("B001");
		e2.setId("b001");

		assertTrue(e1.compareTo(e2) < 0);
		assertTrue(e2.compareTo(e1) > 0);
		
		e2.setId("B001");
		assertEquals(0, e1.compareTo(e2));
		
		e2.setId("C001");
		assertTrue(e1.compareTo(e2) < 0);
		assertTrue(e2.compareTo(e1) > 0);

		e2.setId("A001");
		assertTrue(e1.compareTo(e2) > 0);
		assertTrue(e2.compareTo(e1) < 0);
	}*/

}
