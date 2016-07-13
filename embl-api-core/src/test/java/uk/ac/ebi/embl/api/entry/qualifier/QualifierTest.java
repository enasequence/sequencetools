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
package uk.ac.ebi.embl.api.entry.qualifier;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class QualifierTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testQualifier() {
		Qualifier qual1 = new Qualifier(null);
		assertNull(qual1.getName());
		assertNull(qual1.getValue());
		
		Qualifier qual2 = new Qualifier("qual");
		assertEquals("qual", qual2.getName());
		assertNull(qual2.getValue());
		
		Qualifier qual3 = new Qualifier("qual", "value");
		assertEquals("qual", qual3.getName());
		assertEquals("value", qual3.getValue());
	}
	
	@Test
	public void testIsValueQuoted() {
		Qualifier qual1 = new Qualifier("sex");
		assertTrue(qual1.isValueQuoted());

		Qualifier qual2 = new Qualifier("trans_table");
		assertFalse(qual2.isValueQuoted());
	}	

	@Test
	public void testEquals() {
		Qualifier qual1 = new Qualifier("x", "1");
		assertTrue(qual1.equals(qual1));
		
		Qualifier qual2 = new Qualifier("x", "1");
		assertTrue(qual1.equals(qual2));
		assertTrue(qual2.equals(qual1));
		
		assertFalse(qual1.equals(new Qualifier(null)));
		assertFalse(qual1.equals(new Qualifier("x")));
		assertFalse(qual1.equals(new Qualifier("X", "1")));
		assertFalse(qual1.equals(new Qualifier("y", "1")));
		assertFalse(qual1.equals(new Qualifier("x", "2")));
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(new Qualifier(null).equals(new String()));
	}
	
	@Test
	public void testToString() {
		assertNotNull(new Qualifier(null).toString());
		assertNotNull(new Qualifier("x").toString());
		assertNotNull(new Qualifier("x", "1").toString());
	}

}
