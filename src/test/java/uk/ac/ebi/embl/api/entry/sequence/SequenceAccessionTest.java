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
package uk.ac.ebi.embl.api.entry.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.sequence.SequenceAccession;


public class SequenceAccessionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreation() {
		SequenceAccession id = new SequenceAccession("BN00001.3");
		assertEquals("BN00001", id.getAccession());
		assertEquals(new Integer(3), id.getVersion());

		id = new SequenceAccession("BN00001");
		assertEquals("BN00001", id.getAccession());
		assertNull(id.getVersion());
	}

	@Test
	public void testCreation_NoSeparator() {
		SequenceAccession id = new SequenceAccession("BN00001");
		assertEquals("BN00001", id.getAccession());
		assertNull(id.getVersion());
	}
	
	@Test(expected=AccessionFormatException.class)
	public void testCreation_WrongVersion() {
		new SequenceAccession("BN00001.x3");
	}

	@Test(expected=AccessionFormatException.class)
	public void testCreation_NoAccession() {
		new SequenceAccession(".3");
	}
	
	@Test(expected=AccessionFormatException.class)
	public void testCreation_Empty() {
		new SequenceAccession("");
	}

	@Test(expected=AccessionFormatException.class)
	public void testCreation_Null() {
		new SequenceAccession(null);
	}
	
	@Test
	public void testParseAccession() {
		assertEquals("BN00001", 
				SequenceAccession.parseAccession("BN00001.3"));
	}

	@Test
	public void testParseAccession_WrongVersion() {
		assertEquals("BN00001", 
				SequenceAccession.parseAccession("BN00001.x3"));
	}

	@Test
	public void testParseAccession_NoSeparator() {
		assertEquals("BN00001", SequenceAccession.parseAccession("BN00001"));
	}
	
	@Test(expected=AccessionFormatException.class)
	public void testParseAccession_NoAccession() {
		SequenceAccession.parseAccession(".3");
	}

	@Test(expected=AccessionFormatException.class)
	public void testParseAccession_Empty() {
		SequenceAccession.parseAccession("");
	}

	@Test(expected=AccessionFormatException.class)
	public void testParseAccession_Null() {
		SequenceAccession.parseAccession(null);
	}

	@Test
	public void testParseVersion() {
		assertEquals(new Integer(3),
				SequenceAccession.parseVersion("B001.3"));		
		assertEquals(new Integer(3), SequenceAccession.parseVersion(".3"));		
	}

	@Test
	public void testParseVersion_NoSeparator() {
		assertNull(SequenceAccession.parseVersion("B001"));		
	}

	@Test
	public void testParseVersion_Empty() {
		assertNull(SequenceAccession.parseVersion(""));		
	}

	@Test
	public void testParseVersion_Null() {
		assertNull(SequenceAccession.parseVersion(null));		
	}

	@Test(expected=AccessionFormatException.class)
	public void testParseVersion_Negative() {
		SequenceAccession.parseVersion(".-1");		
	}
	
	@Test(expected=AccessionFormatException.class)
	public void testParseVersion_WrongValue() {
		SequenceAccession.parseVersion(".x3");		
	}
	
	@Test
	public void testEqualsObject() {
		assertTrue(new SequenceAccession("B001", 3).equals(
				new SequenceAccession("B001", 3)));
	}

	@Test
	public void testEqualsObject_DiffAccession() {
		assertFalse(new SequenceAccession("A001", 3).equals(
				new SequenceAccession("B001", 3)));
	}

	@Test
	public void testEqualsObject_DiffVersion() {
		assertFalse(new SequenceAccession("B001", 1).equals(
				new SequenceAccession("B001", 3)));
	}
	
	@Test
	public void testEqualsObject_Diff() {
		assertFalse(new SequenceAccession("A001", 1).equals(
				new SequenceAccession("B001", 3)));
	}
	
	@Test
	public void testEqualsObject_WrongObject() {
		assertFalse(new SequenceAccession("B001", 3).equals(new String()));
	}
	
	@Test
	public void testToString() {
		assertEquals("B001.3", new SequenceAccession("B001", 3).toString());
		assertEquals("B001", new SequenceAccession("B001", null).toString());
		assertEquals(".3", new SequenceAccession(null, 3).toString());
		assertEquals("", new SequenceAccession(null, null).toString());
	}

	@Test
	public void testCompareTo() {
		assertEquals(0, new SequenceAccession("B001", 3).compareTo(
				new SequenceAccession("B001", 3)));		

		assertEquals(-1, new SequenceAccession("A001", 3).compareTo(
				new SequenceAccession("B001", 3)));		
		assertEquals(1, new SequenceAccession("B001", 3).compareTo(
				new SequenceAccession("A001", 3)));
		
		assertEquals(-1, new SequenceAccession("B001", 1).compareTo(
				new SequenceAccession("B001", 3)));		
		assertEquals(1, new SequenceAccession("B001", 5).compareTo(
				new SequenceAccession("A001", 3)));
		
		assertEquals(-1, new SequenceAccession("A001", 1).compareTo(
				new SequenceAccession("B001", 3)));		
		assertEquals(-1, new SequenceAccession("A001", 5).compareTo(
				new SequenceAccession("B001", 3)));
		assertEquals(1, new SequenceAccession("C001", 1).compareTo(
				new SequenceAccession("B001", 3)));		
		assertEquals(1, new SequenceAccession("C001", 5).compareTo(
				new SequenceAccession("B001", 3)));
	}

}
