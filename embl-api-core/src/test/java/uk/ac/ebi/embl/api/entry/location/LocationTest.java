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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.location.Base;
import uk.ac.ebi.embl.api.entry.location.Between;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocalBetween;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Range;

public class LocationTest {
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRange() {
		Range location1 = new LocalRange(null, null);
		assertNull(location1.getBeginPosition());
		assertNull(location1.getEndPosition());
		assertFalse(location1.isComplement());
		
		Range location2 = new LocalRange(1L, 3L);
		assertEquals(new Long(1), location2.getBeginPosition());
		assertEquals(new Long(3), location2.getEndPosition());
		assertEquals(3L, location2.getLength());
		assertFalse(location2.isComplement());
		
		Range location3 = new LocalRange(1L, 3L, true);
		assertEquals(new Long(1), location3.getBeginPosition());
		assertEquals(new Long(3), location3.getEndPosition());
		assertEquals(3L, location3.getLength());
		assertTrue(location3.isComplement());
	}

	@Test
	public void testBase() {
		Base location1 = new LocalBase(null);
		assertNull(location1.getBeginPosition());
		assertNull(location1.getEndPosition());
		assertEquals(0, location1.getLength());
		assertFalse(location1.isComplement());
		
		Base location2 = new LocalBase(1L);
		assertEquals(new Long(1), location2.getBeginPosition());
		assertEquals(new Long(1), location2.getEndPosition());
		assertEquals(1, location2.getLength());
		assertFalse(location2.isComplement());

		Base location3 = new LocalBase(1L, true);
		assertEquals(new Long(1), location3.getBeginPosition());
		assertEquals(new Long(1), location3.getEndPosition());
		assertEquals(1, location3.getLength());
		assertTrue(location3.isComplement());
	}

	@Test
	public void testBetween() {
		Between location1 = new LocalBetween(null, null);
		assertNull(location1.getBeginPosition());
		assertNull(location1.getEndPosition());
		assertEquals(0, location1.getLength());
		assertFalse(location1.isComplement());
		
		Between location2 = new LocalBetween(1L, 2L);
		assertEquals(new Long(1), location2.getBeginPosition());
		assertEquals(new Long(2), location2.getEndPosition());
		assertEquals(0, location2.getLength());
		assertFalse(location2.isComplement());

		Between location3 = new LocalBetween(1L, 2L, true);
		assertEquals(new Long(1), location3.getBeginPosition());
		assertEquals(new Long(2), location3.getEndPosition());
		assertEquals(0, location3.getLength());
		assertTrue(location3.isComplement());
	}

	@SuppressWarnings("serial")
	@Test
	public void testRangeHashCode() {
		new LocalRange(null, null){}.hashCode();
		new LocalRange(1L, null){}.hashCode();
		new LocalRange(1L, 2L){}.hashCode();
		new LocalRange(null, 2L){}.hashCode();
		new LocalRange(1L, 2L, true){}.hashCode();
		new LocalRange(null, 2L, true){}.hashCode();
		new LocalRange(1L, null, true){}.hashCode();
		new LocalRange(null, null, true){}.hashCode();

		new LocalBase(null){}.hashCode();
		new LocalBase(1L){}.hashCode();
		new LocalBase(1L, true){}.hashCode();
		new LocalBase(null, true){}.hashCode();

		new LocalBetween(null, null){}.hashCode();
		new LocalBetween(1L, null){}.hashCode();
		new LocalBetween(1L, 2L){}.hashCode();
		new LocalBetween(null, 2L){}.hashCode();
		new LocalBetween(1L, 2L, true){}.hashCode();
		new LocalBetween(null, 2L, true){}.hashCode();
		new LocalBetween(1L, null, true){}.hashCode();
		new LocalBetween(null, null, true){}.hashCode();		
	}

	@SuppressWarnings("serial")
	@Test
	public void testToString() {
		new LocalRange(null, null){}.toString();
		new LocalRange(1L, null){}.toString();
		new LocalRange(1L, 2L){}.toString();
		new LocalRange(null, 2L){}.toString();
		new LocalRange(1L, 2L, true){}.toString();
		new LocalRange(null, 2L, true){}.toString();
		new LocalRange(1L, null, true){}.toString();
		new LocalRange(null, null, true){}.toString();

		new LocalBase(null){}.toString();
		new LocalBase(1L){}.toString();
		new LocalBase(1L, true){}.toString();
		new LocalBase(null, true){}.toString();

		new LocalBetween(null, null){}.toString();
		new LocalBetween(1L, null){}.toString();
		new LocalBetween(1L, 2L){}.toString();
		new LocalBetween(null, 2L){}.toString();
		new LocalBetween(1L, 2L, true){}.toString();
		new LocalBetween(null, 2L, true){}.toString();
		new LocalBetween(1L, null, true){}.toString();
		new LocalBetween(null, null, true){}.toString();	
	}
}
