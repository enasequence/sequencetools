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
package uk.ac.ebi.embl.api.entry.reference;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ReferenceTest {
	
	private Reference reference;

	@Before
	public void setUp() throws Exception {
		reference = new Reference(null, null);
	}

	@Test
	public void testReference() {
		assertNull(reference.getPublication());
		assertNull(reference.getReferenceNumber());
		assertNotNull(reference.getLocations());
		reference.setReferenceNumber(1);
		Publication publication = new Publication();
		reference.setPublication(publication);
		assertEquals(new Integer(1), reference.getReferenceNumber());
		assertEquals(publication, reference.getPublication());
	}

	@Test
	public void testHashCode() {
		reference.hashCode();
		new Reference(new Unpublished("x"), 2).hashCode();
	}	
	
	@Test
	public void testEquals() {
		assertTrue(reference.equals(reference));
		assertTrue(reference.equals(new Reference()));
		Reference reference2 = new Reference();
		reference.setReferenceNumber(1);
		assertFalse(reference.equals(reference2));
		reference2.setReferenceNumber(1);
		assertTrue(reference.equals(reference2));
		reference.setComment("comment");
		assertFalse(reference.equals(reference2));
		reference2.setComment("comment");
		assertTrue(reference.equals(reference2));
		Publication publication = new Publication();
		reference.setPublication(publication);
		assertFalse(reference.equals(reference2));
		reference2.setPublication(publication);
		assertTrue(reference.equals(reference2));
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(new Reference(null, null).equals(new String()));
	}
	
	@Test
	public void testToString() {
		assertNotNull(reference.toString());
		assertNotNull(new Reference(new Unpublished("x"), 1).toString());
	}
	
	@Test
	public void testCompareTo() {
		assertTrue(reference.compareTo(reference) == 0);
		assertTrue(reference.compareTo(new Reference()) == 0);
		Reference reference2 = new Reference();
		reference.setReferenceNumber(1);
		assertTrue(reference.compareTo(reference2) > 0);
		reference2.setReferenceNumber(1);
		assertTrue(reference.compareTo(reference2) == 0);
		reference.setComment("comment");
		assertTrue(reference.compareTo(reference2) > 0);
		reference2.setComment("comment");
		assertTrue(reference.compareTo(reference2) == 0);
		Publication publication = new Publication();
		reference.setPublication(publication);
		assertTrue(reference.compareTo(reference2) > 0);
		reference2.setPublication(publication);
		assertTrue(reference.compareTo(reference2) == 0);
	}	
}
