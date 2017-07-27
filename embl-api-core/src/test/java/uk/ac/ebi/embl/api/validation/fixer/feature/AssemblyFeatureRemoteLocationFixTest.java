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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssemblyFeatureRemoteLocationFixTest {

	private Feature feature;
	private AssemblyFeatureRemoteLocationFix check;
    public FeatureFactory featureFactory;
    public LocationFactory locationFactory;
    private EntryDAOUtils entryDAOUtils;

    @Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

        featureFactory = new FeatureFactory();
        locationFactory = new LocationFactory();
        feature = featureFactory.createFeature("feature");
    	entryDAOUtils=createMock(EntryDAOUtils.class);
        check = new AssemblyFeatureRemoteLocationFix();
    }

    @Test
	public void testCheck_Empty() throws ValidationEngineException {
        ValidationResult result = check.check(feature);
        assertTrue(result.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_NoFeature() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoLocations() throws ValidationEngineException {
        Order<Location> order = new Order<Location>();
        ValidationResult validationResult = check.check(this.feature);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }
	
	@Test
	public void testCheck_NoRemoteLocation() throws ValidationEngineException {
		 Order<Location> order = new Order<Location>();
	     order.addLocation(locationFactory.createLocalRange(10l,1l));
	     feature.setLocations(order);
        ValidationResult validationResult = check.check(this.feature);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_validremoteAccession() throws ValidationEngineException, SQLException {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createRemoteRange("A00001", 1, 11l, 22l));
        feature.setLocations(order);
        expect(entryDAOUtils.getAssemblyEntryAccession("A00001")).andReturn("A00001.1");
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
        ValidationResult validationResult = check.check(feature);
        assertEquals(0, validationResult.count("AssemblyFeatureRemoteLocationFix", Severity.FIX));
       }

	@Test
	public void testCheck_invalidremoteAccession() throws ValidationEngineException, SQLException {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createRemoteRange("dfghhj", 1, 11l, 22l));
        feature.setLocations(order);
        expect(entryDAOUtils.getAssemblyEntryAccession("dfghhj")).andReturn("A00001.1");
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
        ValidationResult validationResult = check.check(feature);
        assertEquals(1, validationResult.count("AssemblyFeatureRemoteLocationFix", Severity.FIX));
        CompoundLocation<Location> location=feature.getLocations();
         RemoteLocation l=(RemoteLocation) location.getLocations().get(0);
         assertEquals("A00001",l.getAccession());
         assertEquals(new Integer(1),l.getVersion());

       }
    
 }
