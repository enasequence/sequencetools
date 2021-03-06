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
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class FeatureLocationCheckTest
{
	
	private FeatureLocationCheck check;
	
	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		check = new FeatureLocationCheck();
	}
	
	@Test
	public void testCheck_NoFeature() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_noLocation() throws ValidationEngineException
	{
		FeatureFactory featureFactory = new FeatureFactory();
		Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
	    ValidationResult result=check.check(intronFeature);
		assertEquals(1, result.count("FeatureLocationCheck-1", Severity.ERROR));
	}
	
	
	
	@Test
	public void testCheck_invalidOrderLocation() throws ValidationEngineException
	{
		FeatureFactory featureFactory = new FeatureFactory();
		Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
		Order<Location> intronFeatureLocation = new Order<Location>();
		LocationFactory locationFactory = new LocationFactory();
		intronFeatureLocation.addLocation(locationFactory.createLocalRange(120l, 108l));
		intronFeature.setLocations(intronFeatureLocation);
		ValidationResult intronResult = check.check(intronFeature);
		assertEquals(1, intronResult.count("FeatureLocationCheck-3", Severity.ERROR));
	}

}
