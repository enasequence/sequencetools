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

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GapFeatureLocationsCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private LocationFactory locationFactory;
	private QualifierFactory qualifierFactory;
	private GapFeatureLocationsCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		SequenceFactory sequenceFactory = new SequenceFactory();
		featureFactory = new FeatureFactory();
        qualifierFactory = new QualifierFactory();
        locationFactory = new LocationFactory();
		entry = entryFactory.createEntry();
		Sequence sequence= sequenceFactory.createSequence();
		sequence.setSequence(ByteBuffer.wrap("agagagagagagannnnnnnagagagagagagagagagagagag".getBytes()));
        entry.setSequence(sequence);
        check = new GapFeatureLocationsCheck();
	}

	@Test
	public void testCheck_NoEntry() {
        ValidationResult result = check.check(null);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_NoFeatures() {
        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
	}

    @Test
	public void testCheck_NoSequence() {
        entry.setSequence(null);
        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_NoLocations() {
        Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
        Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
        entry.addFeature(feature1);
        ValidationResult result1 = check.check(entry);
        assertTrue(result1.isValid());
        assertEquals(0, result1.getMessages().size());
	}

    @Test
    public void testCheck_GapLocationwithAdjacentBasen() {

        Feature feature1 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
        Order<Location> location1 = new Order<Location>();
        location1.addLocation(locationFactory.createLocalRange(13l,18l));
        feature1.setLocations(location1);
                
        Feature feature2 = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
        Order<Location> location2= new Order<Location>();
        location2.addLocation(locationFactory.createLocalRange(15l,20l));
        feature2.setLocations(location2);
        entry.addFeature(feature1);
        entry.addFeature(feature2);

        ValidationResult result = check.check(entry);
        assertTrue(!result.isValid());
        assertEquals(2, result.count(GapFeatureLocationsCheck.MESSAGE_ID, Severity.ERROR));
        Feature feature3 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
        Order<Location> location3 = new Order<Location>();
        location3.addLocation(locationFactory.createLocalRange(13l,18l));
        feature3.setLocations(location3);
                
        Feature feature4 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
        Order<Location> location4= new Order<Location>();
        location4.addLocation(locationFactory.createLocalRange(15l,20l));
        feature4.setLocations(location4);
        entry.addFeature(feature4);
        entry.addFeature(feature4);

        ValidationResult result1 = check.check(entry);
        assertTrue(!result1.isValid());
        assertEquals(4, result1.count(GapFeatureLocationsCheck.MESSAGE_ID, Severity.ERROR));//assembly_gaps also considered as gaps
    }

    @Test
    public void testCheck_GapLocationwithoutAdjacentBasen() {

        Feature feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
        Order<Location> location = new Order<Location>();
        location.addLocation(locationFactory.createLocalRange(14l,20l));
        feature.setLocations(location);

        entry.addFeature(feature);

        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
        assertEquals(0, result.getMessages().size());
        Feature feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
        Order<Location> location1= new Order<Location>();
        location1.addLocation(locationFactory.createLocalRange(14l,20l));
        feature1.setLocations(location);

        entry.addFeature(feature1);

        ValidationResult result1= check.check(entry);
        assertTrue(result1.isValid());
        assertEquals(0, result1.getMessages().size());
    }

    
}
