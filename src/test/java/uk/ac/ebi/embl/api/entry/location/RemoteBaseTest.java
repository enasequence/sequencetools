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
package uk.ac.ebi.embl.api.entry.location;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.location.Base;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteBetween;

public class RemoteBaseTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testBase() {
		Base base = new RemoteBase("x", 1, 2L);
		assertEquals(new Long(2), base.getBeginPosition());
		assertEquals(new Long(2), base.getEndPosition());
		assertEquals(1, base.getLength());
		assertFalse(base.isComplement());
	}	

	@Test
	public void testAccession() {
		RemoteBetween between = new RemoteBetween("x", 1, 2L, 3L);
		assertEquals("x", between.getAccession());
		assertEquals(new Integer(1), between.getVersion());		
	}
	
	@Test
	public void testComplement() {
		Base base = new LocalBase(null, true);
		assertTrue(base.isComplement());
		base.setComplement(false);
		assertFalse(base.isComplement());
		base.setComplement(true);
		assertTrue(base.isComplement());		
	}	
	
	@Test
	public void testBase_Null() {
		Base base = new RemoteBase("x", 1, null);
		assertNull(base.getBeginPosition());
		assertNull(base.getEndPosition());
		assertEquals(0, base.getLength());
	}	

	@Test
	public void testSetBeginPosition() {
		Base base = new RemoteBase("x", 1, null);
		assertNull(base.getBeginPosition());
		assertNull(base.getEndPosition());
		base.setBeginPosition(2L);
		assertEquals(new Long(2), base.getBeginPosition());
		assertEquals(new Long(2), base.getEndPosition());
		assertEquals(1, base.getLength());	
	}

	@Test
	public void testSetEndPosition() {
		Base base = new RemoteBase("x", 1, null);
		assertNull(base.getBeginPosition());
		assertNull(base.getEndPosition());
		base.setEndPosition(2L);
		assertEquals(new Long(2), base.getBeginPosition());
		assertEquals(new Long(2), base.getEndPosition());
		assertEquals(1, base.getLength());
	}
		
	@Test
	public void testSetPosition() {
		Base base = new RemoteBase("x", 1, null);
		assertNull(base.getBeginPosition());
		assertNull(base.getEndPosition());
		base.setBeginPosition(2L);
		base.setEndPosition(2L);
		assertEquals(new Long(2), base.getBeginPosition());
		assertEquals(new Long(2), base.getEndPosition());
		assertEquals(1, base.getLength());
	}

	@Test
	public void testToString() {
		assertNotNull(new RemoteBase("x", 1, null).toString());
		assertNotNull(new RemoteBase("x", 1, 2L).toString());
	}	
	
	@Test
	public void testBaseHashCode() {
		new RemoteBase("x", 1, 2L).hashCode();
	}
	
	@Test
	public void testEquals() {
		Base location1 = new RemoteBase("x", 1, 2L);
		assertTrue(location1.equals(location1));
		Base location2 = new RemoteBase("x", 1, 2L);
		assertTrue(location1.equals(location2));
		assertTrue(location2.equals(location1));
		Base location3 = new RemoteBase("x", 1, 2L);
		assertTrue(location1.equals(location3));
		assertTrue(location3.equals(location1));		
		assertFalse(location1.equals(new RemoteBase("y", 1, 2L)));
		assertFalse(location1.equals(new RemoteBase("x", 2, 2L)));
		assertFalse(location1.equals(new RemoteBase("x", 1, 4L)));
		assertFalse(location1.equals(new RemoteBase("x", 1, 2L, true)));
	}

	@Test
	public void testEquals_WrongObject() {
		assertFalse(new RemoteBase("x", 1, 2L).equals(new String()));
	}	
}
