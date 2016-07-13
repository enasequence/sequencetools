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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Base;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;

public class CompoundLocationTest {

	@SuppressWarnings("unchecked")
	private CompoundLocation location; 
	
	@SuppressWarnings({ "unchecked", "serial" })
	@Before
	public void setUp() throws Exception {
		location = new CompoundLocation(){};
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Test
	public void testCompoundLocation() {
		CompoundLocation location1 = new CompoundLocation(){};
		assertTrue(location1.getLocations().isEmpty());
		assertFalse(location1.isLeftPartial());
		assertFalse(location1.isRightPartial());

		CompoundLocation location2 = new CompoundLocation(true, true){};
		assertTrue(location2.getLocations().isEmpty());
		assertTrue(location2.isLeftPartial());
		assertTrue(location2.isRightPartial());
	}

	@Test
	public void testGetLocations() {
		assertTrue(location.getLocations().isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=UnsupportedOperationException.class)
	public void testGetLocations_UnmodifiableList() {
		location.getLocations().add(null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAddLocation() {
		Base base = new LocalBase(2L);
		assertTrue(location.addLocation(base));
		assertEquals(base, location.getLocations().get(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddLocations() {
		Base base = new LocalBase(2L);
		assertTrue(location.addLocations(Arrays.asList(base)));
		assertEquals(base, location.getLocations().get(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddLocations_Null() {
		assertFalse(location.addLocations(null));
		assertTrue(location.getLocations().isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveLocation() {
		Base locationX = new LocalBase(2L);
		Base locationY = new LocalBase(3L);
		
		assertFalse(location.removeLocation(locationX));
		
		location.addLocation(locationX);
		assertEquals(1, location.getLocations().size());
		
		assertFalse(location.removeLocation(locationY));
		assertEquals(1, location.getLocations().size());
		
		assertTrue(location.removeLocation(locationX));
		assertEquals(0, location.getLocations().size());
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testAddLocation_RemoteRange() {
		CompoundLocation<RemoteRange> location = 
			new CompoundLocation<RemoteRange>(){};
		RemoteRange range = new LocationFactory().createRemoteRange(
				"x", 1, 1L, 3L);
		assertTrue(location.addLocation(range));
		assertEquals(range, location.getLocations().get(0));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testAddLocations_RemoteRange() {
		CompoundLocation<RemoteRange> location = 
			new CompoundLocation<RemoteRange>(){};
		RemoteRange range = new LocationFactory().createRemoteRange(
				"x", 1, 1L, 3L);
		Collection<RemoteRange> ranges = 
			new ArrayList<RemoteRange>();
		ranges.add(range);
		assertTrue(location.addLocations(ranges));
		assertEquals(range, location.getLocations().get(0));
	}

	@SuppressWarnings("serial")
	@Test
	public void testGetLength() {
		RemoteRange range = new LocationFactory().createRemoteRange(
				"x", 1, 1L, 3L);
		RemoteRange range2 = new LocationFactory().createRemoteRange(
				"x", 1, 1L, 3L);
		Collection<RemoteRange> ranges = 
			new ArrayList<RemoteRange>();
		ranges.add(range);
		ranges.add(range2);
		CompoundLocation<RemoteRange> location =
			new CompoundLocation<RemoteRange>(){};
		assertTrue(location.addLocations(ranges));
		assertEquals(range, location.getLocations().get(0));
		assertEquals(range2, location.getLocations().get(1));
		assertEquals(6, location.getLength());
	}

	@Test
	public void testGetRelativePosition1() {
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createCdsFeature();
		LocationFactory locationFactory = new LocationFactory();		
		LocalRange localRange = locationFactory.createLocalRange(5L, 35L);
		feature.getLocations().addLocation(localRange);
		assertEquals(new Long(1L), feature.getLocations().getRelativePosition(5L));
		assertEquals(new Long(31L), feature.getLocations().getRelativePosition(35L));
		localRange.setComplement(true);
		assertEquals(new Long(31L), feature.getLocations().getRelativePosition(5L));
		assertEquals(new Long(1L), feature.getLocations().getRelativePosition(35L));
		feature.getLocations().setComplement(true);
		assertEquals(new Long(1L), feature.getLocations().getRelativePosition(5L));
		assertEquals(new Long(31L), feature.getLocations().getRelativePosition(35L));
        //test outside range
        assertEquals(null, feature.getLocations().getRelativePosition(55L));
        assertEquals(null, feature.getLocations().getRelativePosition(2L));
	}

	@Test
	public void testGetRelativePosition2() {
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createCdsFeature();
		LocationFactory locationFactory = new LocationFactory();		
		LocalRange localRange1 = locationFactory.createLocalRange(1L, 35L);
		feature.getLocations().addLocation(localRange1);
		LocalRange localRange2 = locationFactory.createLocalRange(101L, 135L);
		feature.getLocations().addLocation(localRange2);		
		assertEquals(new Long(1L), feature.getLocations().getRelativePosition(1L));
		assertEquals(new Long(35L), feature.getLocations().getRelativePosition(35L));
		assertEquals(new Long(36L), feature.getLocations().getRelativePosition(101L));
		assertEquals(new Long(70L), feature.getLocations().getRelativePosition(135L));
		localRange1.setComplement(true);
		assertEquals(new Long(35L), feature.getLocations().getRelativePosition(1L));
		assertEquals(new Long(1L), feature.getLocations().getRelativePosition(35L));
		assertEquals(new Long(36L), feature.getLocations().getRelativePosition(101L));
		assertEquals(new Long(70L), feature.getLocations().getRelativePosition(135L));
		localRange2.setComplement(true);
		assertEquals(new Long(35L), feature.getLocations().getRelativePosition(1L));
		assertEquals(new Long(1L), feature.getLocations().getRelativePosition(35L));
		assertEquals(new Long(70L), feature.getLocations().getRelativePosition(101L));
		assertEquals(new Long(36L), feature.getLocations().getRelativePosition(135L));
		feature.getLocations().setComplement(true);
		assertEquals(new Long(36L), feature.getLocations().getRelativePosition(1L));
		assertEquals(new Long(70L), feature.getLocations().getRelativePosition(35L));
		assertEquals(new Long(1L), feature.getLocations().getRelativePosition(101L));
		assertEquals(new Long(35L), feature.getLocations().getRelativePosition(135L));
	}

	@Test
	public void testGetRelativeBeginPosition() {
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createCdsFeature();
		LocationFactory locationFactory = new LocationFactory();		
		LocalRange localRange1 = locationFactory.createLocalRange(1L, 35L);
		feature.getLocations().addLocation(localRange1);
		LocalRange localRange2 = locationFactory.createLocalRange(101L, 135L);
		feature.getLocations().addLocation(localRange2);
		assertEquals(new Long(1), feature.getLocations().getRelativeBeginPosition(localRange1));
		assertEquals(new Long(35), feature.getLocations().getRelativeEndPosition(localRange1));
		assertEquals(new Long(36), feature.getLocations().getRelativeBeginPosition(localRange2));
		assertEquals(new Long(70), feature.getLocations().getRelativeEndPosition(localRange2));		
	}		
	
	@Test
	public void testToString() {
		assertNotNull(location.toString());
	}

}
