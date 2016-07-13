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

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.EntryFactory;

public class ThesisTest {

	private Thesis thesis;
	
	@Before
	public void setUp() throws Exception {
		thesis = new Thesis();
	}

	@Test
	public void testThesis() {
		assertNull(thesis.getInstitute());
		assertNull(thesis.getYear());
		Date date = new Date();
		thesis.setInstitute("institute");
		thesis.setYear(date);
		assertEquals(date, thesis.getYear());
		assertEquals("institute", thesis.getInstitute());
	}

	@Test
	public void testHashCode() {
		thesis.hashCode();
		new Thesis("t", new Date(), "i").hashCode();
	}

	@Test
	public void testEquals() {
		assertTrue(thesis.equals(thesis));
		assertTrue(thesis.equals(new Thesis()));
		Thesis thesis2 = new Thesis();
		thesis.setTitle("title");
		assertFalse(thesis.equals(thesis2));
		thesis2.setTitle("title");
		assertTrue(thesis.equals(thesis2));
		thesis.setConsortium("consortium");
		assertFalse(thesis.equals(thesis2));
		thesis2.setConsortium("consortium");
		assertTrue(thesis.equals(thesis2));
		thesis.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertFalse(thesis.equals(thesis2));
		thesis2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(thesis.equals(thesis2));
		thesis.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertFalse(thesis.equals(thesis2));
		thesis2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(thesis.equals(thesis2));	
		thesis.setInstitute("institute");
		assertFalse(thesis.equals(thesis2));
		thesis2.setInstitute("institute");
		assertTrue(thesis.equals(thesis2));	
		thesis.setYear(new Date());
		assertFalse(thesis.equals(thesis2));
		thesis2.setYear(new Date());
		assertTrue(thesis.equals(thesis2));	
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(thesis.equals(new String()));
		assertFalse(thesis.equals(null));
	}
	
	@Test
	public void testToString() {
		assertNotNull(thesis.toString());
		assertNotNull(new Thesis("t", new Date(), "i").toString());
	}
	
	@Test
	public void testCompareTo() {
		assertTrue(thesis.compareTo(thesis) == 0);
		assertTrue(thesis.compareTo(new Thesis()) == 0);
		Thesis thesis2 = new Thesis();
		thesis.setTitle("title");
		// null < not null
		assertTrue(thesis.compareTo(thesis2) > 0);
		thesis2.setTitle("title");
		assertTrue(thesis.compareTo(thesis2) == 0);
		thesis.setConsortium("consortium");
		assertTrue(thesis.compareTo(thesis2) > 0);
		thesis2.setConsortium("consortium");
		assertTrue(thesis.compareTo(thesis2) == 0);
		thesis.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(thesis.compareTo(thesis2) > 0);
		thesis2.addAuthor(
				(new ReferenceFactory()).createPerson("surname", "firstname"));
		assertTrue(thesis.compareTo(thesis2) == 0);
		thesis.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(thesis.compareTo(thesis2) > 0);
		thesis2.addXRef(
				(new EntryFactory()).createXRef("database", "accession"));
		assertTrue(thesis.compareTo(thesis2) == 0);
		thesis.setInstitute("institute");
		assertTrue(thesis.compareTo(thesis2) > 0);
		thesis2.setInstitute("institute");
		assertTrue(thesis.compareTo(thesis2) == 0);
		thesis.setYear(new Date());
		assertTrue(thesis.compareTo(thesis2) > 0);
		thesis2.setYear(new Date());
		assertTrue(thesis.compareTo(thesis2) == 0);
	}	
}
