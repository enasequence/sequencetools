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

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.feature.FeatureLocationFix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeatureLocationFixTest {

	private Feature feature;
	private FeatureLocationFix check;
    public FeatureFactory featureFactory;
    public LocationFactory locationFactory;

    @Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

        featureFactory = new FeatureFactory();
        locationFactory = new LocationFactory();
        feature = featureFactory.createFeature("feature");
        check = new FeatureLocationFix();
    }

    @Test
	public void testCheck_Empty() {
        ValidationResult result = check.check(feature);
        assertTrue(result.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoLocations() {
        Order<Location> order = new Order<Location>();
        ValidationResult validationResult = check.check(this.feature);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_Swap() {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(10l,1l));
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertEquals(1, validationResult.count("FeatureLocationFix", Severity.FIX));
        Location fixedLocation = feature.getLocations().getLocations().get(0);
        assertTrue(fixedLocation.getBeginPosition() == 1);
        assertTrue(fixedLocation.getEndPosition() == 10);
        assertTrue(fixedLocation.isComplement());
    }

    @Test
	public void testCheck_Swap2() {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(10l,1l));
        order.addLocation(locationFactory.createLocalRange(20l,11l));
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertEquals(2, validationResult.count("FeatureLocationFix", Severity.FIX));
        Location fixedLocation = feature.getLocations().getLocations().get(0);
        assertTrue(fixedLocation.getBeginPosition() == 1);
        assertTrue(fixedLocation.getEndPosition() == 10);
        assertTrue(fixedLocation.isComplement());

        Location fixedLocation2 = feature.getLocations().getLocations().get(1);
        assertTrue(fixedLocation2.getBeginPosition() == 11);
        assertTrue(fixedLocation2.getEndPosition() == 20);
        assertTrue(fixedLocation2.isComplement());
    }
    
 }
