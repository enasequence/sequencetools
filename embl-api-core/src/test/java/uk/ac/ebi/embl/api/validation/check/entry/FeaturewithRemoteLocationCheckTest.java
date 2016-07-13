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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class FeaturewithRemoteLocationCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private FeaturewithRemoteLocationCheck check;
	private EmblEntryValidationPlanProperty property;
	private LocationFactory locationFactory;

	@Before
	public void setUp() throws SQLException {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		locationFactory=new LocationFactory();
		entry = entryFactory.createEntry();
		property=new EmblEntryValidationPlanProperty();
		check = new FeaturewithRemoteLocationCheck();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoFeatures() throws ValidationEngineException {
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_featurewithRemoteLocationandNodbConnection() throws ValidationEngineException {
		
		RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1,10L,20L);
		RemoteRange remoteRange2 = locationFactory.createRemoteRange("A00001", 1,10L,20L);
		Join<Location> join = new Join<Location>();
		join.addLocation(remoteRange1);
		join.addLocation(remoteRange2);
		Feature feature=featureFactory.createFeature("STS");
		feature.setLocations(join);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("FeaturewithRemoteLocationCheck-1", Severity.WARNING));
	}

	@Test
	public void testCheck_featurewithLocalLocationandNodbConnection() throws ValidationEngineException {
		
		LocalRange localRange1 = locationFactory.createLocalRange(10L, 20L);
		LocalRange localRange2 = locationFactory.createLocalRange(10L,20L);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		join.addLocation(localRange2);
		Feature feature=featureFactory.createFeature("STS");
		feature.setLocations(join);
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("FeaturewithRemoteLocationCheck-1", Severity.WARNING));
	}

	@Test
	public void testCheck_featurewithvalidRemoteLocationwithdbConnection() throws SQLException, ValidationEngineException {
		
		property.enproConnection.set(createMock(Connection.class));
		RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1,10L,20L);
		Join<Location> join = new Join<Location>();
		join.addLocation(remoteRange1);
		Feature feature=featureFactory.createFeature("STS");
		feature.setLocations(join);
		entry.addFeature(feature);
		check.setEmblEntryValidationPlanProperty(property);
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(30L);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("FeaturewithRemoteLocationCheck-2", Severity.ERROR));
	}
	@Test
	public void testCheck_featurewithInvalidRemoteLocationwithdbConnection() throws SQLException, ValidationEngineException {
		
		property.enproConnection.set(createMock(Connection.class));
		RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1,10L,20L);
		Join<Location> join = new Join<Location>();
		join.addLocation(remoteRange1);
		Feature feature=featureFactory.createFeature("STS");
		feature.setLocations(join);
		entry.addFeature(feature);
		check.setEmblEntryValidationPlanProperty(property);
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSequenceLength("A00001.1")).andReturn(10L);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("FeaturewithRemoteLocationCheck-2", Severity.ERROR));
	}
	
	@Test
	public void testCheck_featurewithLocalLocationwithdbConnection() throws SQLException, ValidationEngineException {
		property.enproConnection.set(createMock(Connection.class));
		LocalRange localRange1 = locationFactory.createLocalRange(10L, 20L);
		LocalRange localRange2 = locationFactory.createLocalRange(10L,20L);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		join.addLocation(localRange2);
		Feature feature=featureFactory.createFeature("STS");
		feature.setLocations(join);
		entry.addFeature(feature);
		check.setEmblEntryValidationPlanProperty(property);
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("FeaturewithRemoteLocationCheck-1", Severity.WARNING));
	}
}
