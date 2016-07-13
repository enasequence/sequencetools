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
package uk.ac.ebi.embl.api.validation.check.feature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 26-Jan-2009
 * Time: 10:50:52
 * To change this template use File | Settings | File Templates.
 */
public class SimpleFeatureLocationCheckTest {

    private Feature feature;
    private SimpleFeatureLocationCheck check;
    private LocationFactory locationFactory;

    @Before
    public void setUp() {
        FeatureFactory featureFactory = new FeatureFactory();
        feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
        locationFactory = new LocationFactory();

        check = new SimpleFeatureLocationCheck();
        check.setPopulated();
    }

    @Test
    public void testCheck_NoFeature() {
        assertTrue(check.check(null).isValid());
    }

    @Test
    public void testCheck_NoLocation() {
        assertTrue(check.check(feature).isValid());
    }

    @Test
    public void testCheck_InvalidLocation1() {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalBetween(1l, 10l));//not a range
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertEquals(1, validationResult.count("SimpleFeatureLocationCheck-1", Severity.ERROR));
    }

    @Test
    public void testCheck_InvalidLocation2() {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(1l, 10l));
        order.addLocation(locationFactory.createLocalRange(11l, 20l));//a compound location
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertEquals(1, validationResult.count("SimpleFeatureLocationCheck-1", Severity.ERROR));
    }

    @Test
    public void testCheck_InvalidLocation3() {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(1l, 10l));
        order.setComplement(true);//complement is not allowed
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertEquals(1, validationResult.count("SimpleFeatureLocationCheck-1", Severity.ERROR));
    }

    @Test
    public void testCheck_InvalidLocation4() {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(10l, 1l));//location start and end back to front
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertEquals(1, validationResult.count("SimpleFeatureLocationCheck-1", Severity.ERROR));
    }

    @Test
    public void testCheck_Fine1() {
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(1l, 10l));
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void testCheck_Fine2() {
        feature.setName("Not a gap");
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(1l, 10l));
        order.setComplement(true);//would fail if this was a gap
        feature.setLocations(order);
        ValidationResult validationResult = check.check(feature);
        assertTrue(validationResult.isValid());
    }

}
