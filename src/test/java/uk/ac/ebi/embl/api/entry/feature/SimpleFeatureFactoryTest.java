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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;

public class SimpleFeatureFactoryTest {

	private FeatureFactory factory;
	
	@Before
	public void setUp() throws Exception {
		factory = new FeatureFactory();
	}

	@Test
	public void testCreateFeature() {
		Feature result = factory.createFeature("feat", true);
		assertEquals("feat", result.getName());
		assertNotNull(result.getLocations());
		assertTrue(result.getLocations().getLocations().isEmpty());
		assertTrue(result.getLocations() instanceof Join<?>);
		assertFalse(result.getLocations().isComplement());
		assertFalse(result.getLocations().isLeftPartial());
		assertFalse(result.getLocations().isRightPartial());
	}

	@Test
	public void testCreateFeature_Locations() {
		Feature result = factory.createFeature("feat", false);
		result.getLocations().setLeftPartial(true);
		result.getLocations().setRightPartial(false);
		result.getLocations().setComplement(true);
		assertEquals("feat", result.getName());
		assertNotNull(result.getLocations());
		assertTrue(result.getLocations().getLocations().isEmpty());
		assertTrue(result.getLocations() instanceof Order<?>);
		assertTrue(result.getLocations().isLeftPartial());
		assertFalse(result.getLocations().isRightPartial());
		assertTrue(result.getLocations().isComplement());
	}

	@Test
	public void testCreateFeature_Source() {
		Feature result = factory.createFeature(
				SourceFeature.SOURCE_FEATURE_NAME, false);
		assertEquals(SourceFeature.SOURCE_FEATURE_NAME, result.getName());
		assertNotNull(result.getLocations());
		assertTrue(result.getLocations().getLocations().isEmpty());
		assertTrue(result.getLocations() instanceof Order<?>);
		
		//source
		assertTrue(result instanceof SourceFeature);
	}
	
	@Test
	public void testCreateFeature_Cds() {
		Feature result = factory.createFeature(CdsFeature.CDS_FEATURE_NAME, 
				true);
		assertEquals(CdsFeature.CDS_FEATURE_NAME, result.getName());
		assertNotNull(result.getLocations());
		assertTrue(result.getLocations().getLocations().isEmpty());
		assertTrue(result.getLocations() instanceof Join<?>);
		
		//source
		assertTrue(result instanceof CdsFeature);
	}
	
	@Test
	public void testGetLocationFactory() {
		assertNotNull(factory.getLocationFactory());
	}

	@Test
	public void testCreateQualifier() {
		QualifierFactory factory = new QualifierFactory();
		Qualifier qualifier1 = factory.createQualifier("name");
		assertEquals("name", qualifier1.getName());
		assertNull(qualifier1.getValue());
		
		Qualifier qualifier2 = factory.createQualifier("name2", "val");
		assertEquals("name2", qualifier2.getName());
		assertEquals("val", qualifier2.getValue());
	}

}
