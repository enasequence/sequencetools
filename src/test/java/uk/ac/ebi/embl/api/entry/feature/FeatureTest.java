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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;

public class FeatureTest {

	private Feature feature;
	
	@Before
	public void setUp() throws Exception {
		feature = new Feature("feat", false);
	}

	@Test
	public void testFeature() {
		assertEquals("feat", feature.getName());
		assertTrue(feature.getLocations() instanceof Order<?>);		
		assertTrue(feature.getQualifiers().isEmpty());
		assertTrue(feature.getXRefs().isEmpty());
		assertNotNull(feature.getLocations());
		
		Feature feat2 = new Feature("feat2", true);
		feat2.getLocations().setLeftPartial(true);
		feat2.getLocations().setRightPartial(true);
		feat2.getLocations().setComplement(true);
		assertEquals("feat2", feat2.getName());
		assertTrue(feat2.getLocations() instanceof Join<?>);
		assertTrue(feat2.getQualifiers().isEmpty());
		assertTrue(feat2.getXRefs().isEmpty());
		assertNotNull(feat2.getLocations());
		assertTrue(feat2.getLocations().isLeftPartial());		
		assertTrue(feat2.getLocations().isRightPartial());		
		assertTrue(feat2.getLocations().isComplement());		
	}

	@Test
	public void testGetQualifierByName() {
		assertTrue(feature.getQualifiers(null).isEmpty());
		assertTrue(feature.getQualifiers("q").isEmpty());
		
		QualifierFactory factory = new QualifierFactory(); 
		feature.addQualifier(factory.createQualifier("q"));
		assertEquals(1, feature.getQualifiers("q").size());
		assertEquals(0, feature.getQualifiers("z").size());
		
		feature.addQualifier(factory.createQualifier("a"));
		assertEquals(1, feature.getQualifiers("q").size());
		assertEquals(1, feature.getQualifiers("a").size());
		assertEquals(0, feature.getQualifiers("z").size());
		
		feature.addQualifier(factory.createQualifier("q", "1"));
		assertEquals(2, feature.getQualifiers("q").size());
		assertEquals(1, feature.getQualifiers("a").size());
		assertEquals(0, feature.getQualifiers("z").size());
		
		feature.removeQualifier(factory.createQualifier("q"));
		assertEquals(1, feature.getQualifiers("q").size());
		assertEquals(1, feature.getQualifiers("a").size());
		assertEquals(0, feature.getQualifiers("z").size());
	}

	@Test
	public void testGetSingleQualifier() {
		assertNull(feature.getSingleQualifier(null));
		assertNull(feature.getSingleQualifier("q"));
		
		QualifierFactory factory = new QualifierFactory();
		feature.addQualifier(factory.createQualifier("q"));
		assertNotNull(feature.getSingleQualifier("q"));
		assertNull(feature.getSingleQualifier("z"));
		
		feature.addQualifier(factory.createQualifier("a"));
		assertNotNull(feature.getSingleQualifier("q"));
		assertNotNull(feature.getSingleQualifier("a"));
		assertNull(feature.getSingleQualifier("z"));		
		
		feature.addQualifier(factory.createQualifier("q", "1"));
		assertNotNull(feature.getSingleQualifier("q"));
		assertNotNull(feature.getSingleQualifier("a"));
		assertNull(feature.getSingleQualifier("z"));		
	}

	@Test
	public void testSingleQualifierValue() {
		assertNull(feature.getSingleQualifierValue(null));
		assertNull(feature.getSingleQualifierValue("q"));
		
		QualifierFactory factory = new QualifierFactory();
		feature.addQualifier(factory.createQualifier("q"));
		assertNull(feature.getSingleQualifierValue("q"));
		
		feature.removeQualifier(factory.createQualifier("q"));
		feature.addQualifier(factory.createQualifier("q", "1"));
		assertEquals("1", feature.getSingleQualifierValue("q"));

		feature.setSingleQualifierValue("q", "2");
		assertEquals("2", feature.getSingleQualifierValue("q"));

		assertNull(feature.getSingleQualifierValue("z"));
		feature.setSingleQualifierValue("z", "1");
		assertEquals("1", feature.getSingleQualifierValue("z"));
	}

	@Test
	public void testGetLocations() {
		assertTrue(feature.getLocations() instanceof Order<?>);
		assertTrue(new Feature("f", true).getLocations() instanceof Join<?>);
	}

	@Test
	public void testGetQualifiers() {
		assertEquals(0, feature.getQualifiers().size());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGetQualifiers_UnmodifiableList() {
		feature.getQualifiers().add(null);
	}
	
	@Test
	public void testAddQualifier() {
		QualifierFactory factory = new QualifierFactory();
		Qualifier qual = factory.createQualifier("q", "1");
		assertTrue(feature.addQualifier(qual));
		assertEquals(qual, feature.getQualifiers().get(0));
	}

	@Test
	public void testAddQualifiers() {
		QualifierFactory factory = new QualifierFactory();
		Qualifier qual = factory.createQualifier("q", "1");
		assertTrue(feature.addQualifiers(Arrays.asList(qual)));
		assertEquals(qual, feature.getQualifiers().get(0));
	}

	@Test
	public void testAddQualifiers_Null() {
		assertFalse(feature.addQualifiers(null));
		assertTrue(feature.getQualifiers().isEmpty());
	}
	
	@Test
	public void testRemoveQualifier() {
		QualifierFactory factory = new QualifierFactory();		
		Qualifier qualX = factory.createQualifier("x", "1");
		Qualifier qualY = factory.createQualifier("y", "1");
		assertFalse(feature.removeQualifier(qualX));
		
		feature.addQualifier(qualX);
		assertEquals(1, feature.getQualifiers().size());
		
		assertFalse(feature.removeQualifier(qualY));
		assertEquals(1, feature.getQualifiers().size());
		
		assertTrue(feature.removeQualifier(qualX));
		assertEquals(0, feature.getQualifiers().size());
	}

	@Test
	public void testGetXRefs() {
		assertEquals(0, feature.getXRefs().size());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetXRefs_UnmodifiableList() {
		feature.getXRefs().add(null);
	}	

	@Test
	public void testAddXRef() {
		XRef xRef = new EntryFactory().createXRef("db", "pa", "sa");
		assertTrue(feature.addXRef(xRef));
		assertEquals(xRef, feature.getXRefs().get(0));
	}

	@Test
	public void testAddXRefs() {
		XRef xRef = new EntryFactory().createXRef("db", "pa", "sa");
		assertTrue(feature.addXRefs(Arrays.asList(xRef)));
		assertEquals(xRef, feature.getXRefs().get(0));
	}

	@Test
	public void testAddXRefs_Null() {
		assertFalse(feature.addXRefs(null));
		assertTrue(feature.getXRefs().isEmpty());
	}
	
	@Test
	public void testRemoveXRef() {
		EntryFactory factory = new EntryFactory();
		XRef xRef = factory.createXRef("db", "pa", "sa");
		XRef x = factory.createXRef("x", "x", "x");
		assertFalse(feature.removeXRef(xRef));
		
		feature.addXRef(xRef);
		assertEquals(1, feature.getXRefs().size());
		
		assertFalse(feature.removeXRef(x));
		assertEquals(1, feature.getXRefs().size());
		
		assertTrue(feature.removeXRef(xRef));
		assertEquals(0, feature.getXRefs().size());
	}
	
	@Test
	public void testHashCode() {
		feature.hashCode();
	}

	@Test
	public void testEquals() {
		assertTrue(feature.equals(feature));
		
		Feature feature1 = new Feature("x", true);
		Feature feature2 = new Feature("x", true);
		assertTrue(feature1.equals(feature2));
		assertTrue(feature2.equals(feature1));
		
		assertFalse(feature1.equals(new Feature("y", true)));
		assertFalse(feature1.equals(new Feature("x", false)));
		
		//qualifiers
		QualifierFactory factory = new QualifierFactory();	
		feature1.addQualifier(factory.createQualifier("q"));
		assertFalse(feature1.equals(feature2));

		feature2.addQualifier(factory.createQualifier("q"));
		assertTrue(feature1.equals(feature2));
		
		feature2.addQualifier(factory.createQualifier("q", "1"));
		assertFalse(feature1.equals(feature2));
		
		feature1.addQualifier(factory.createQualifier("q", "1"));
		assertTrue(feature1.equals(feature2));
		
		//xrefs
		EntryFactory entryFactory = new EntryFactory();
		feature1.addXRef(entryFactory.createXRef("a", "b", "c"));
		assertFalse(feature1.equals(feature2));
		
		feature2.addXRef(entryFactory.createXRef("a", "b", "c"));
		assertTrue(feature1.equals(feature2));
		
		feature2.addXRef(entryFactory.createXRef("x", "y", "z"));
		assertFalse(feature1.equals(feature2));
		
		feature1.addXRef(entryFactory.createXRef("x", "y", "z"));
		assertTrue(feature1.equals(feature2));

		//locations
		LocationFactory locationFactory = new LocationFactory();
		feature1.getLocations().addLocation(locationFactory.createLocalBase(1L));
		assertFalse(feature1.equals(feature2));
		
		feature2.getLocations().addLocation(locationFactory.createLocalBase(2L));
		assertFalse(feature1.equals(feature2));

		feature2.getLocations().getLocations().get(0).setBeginPosition(1L);		
		feature2.getLocations().addLocation(locationFactory.createLocalBase(2L));
		feature1.getLocations().addLocation(locationFactory.createLocalBase(2L));
		assertTrue(feature1.equals(feature2));
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(feature.equals(new String()));
	}
	
	@Test
	public void testToString() {
		assertNotNull(feature.toString());
	}

}
