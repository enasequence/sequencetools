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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import java.util.Collection;


public class FeatureLengthCheckTest
{
	
	private FeatureLengthCheck check;
	
	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		check = new FeatureLengthCheck();
	}
	
	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_noLocation()
	{
		FeatureFactory featureFactory = new FeatureFactory();
		Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
		Feature exonFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
		assertTrue(check.check(intronFeature).isValid());
		assertTrue(check.check(exonFeature).isValid());
	}
	
	@Test
	public void testCheck_invalidLocation()
	{
		FeatureFactory featureFactory = new FeatureFactory();
		Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
		Feature exonFeature = featureFactory.createFeature(Feature.EXON_FEATURE_NAME);
		Order<Location> intronFeatureLocation = new Order<Location>();
		LocationFactory locationFactory = new LocationFactory();
		intronFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 109l));
		Order<Location> exonFeatureLocation = new Order<Location>();
		exonFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 114l));
		intronFeature.setLocations(intronFeatureLocation);
		exonFeature.setLocations(exonFeatureLocation);
		ValidationResult intronResult = check.check(intronFeature);
		assertEquals(1, intronResult.count("FeatureLengthCheck-1", Severity.WARNING));
		Collection<ValidationMessage<Origin>> intronmessages = intronResult.getMessages("FeatureLengthCheck-1", Severity.WARNING);
		assertEquals("\"intron\" usually expected to be at least \"10\" nt long. Please check the accuracy.", intronmessages.iterator().next()
				.getMessage());
		ValidationResult exonResult = check.check(exonFeature);
		assertEquals(1, exonResult.count("FeatureLengthCheck-1", Severity.WARNING));
		Collection<ValidationMessage<Origin>> exonmessages = exonResult.getMessages("FeatureLengthCheck-1", Severity.WARNING);
		assertEquals("\"exon\" usually expected to be at least \"15\" nt long. Please check the accuracy.", exonmessages.iterator().next()
				.getMessage());
		
	}
	
	@Test
	public void testCheck_validLocation()
	{
		FeatureFactory featureFactory = new FeatureFactory();
		Feature intronFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
		Feature exonFeature = featureFactory.createFeature(Feature.INTRON_FEATURE_NAME);
		Order<Location> intronFeatureLocation = new Order<Location>();
		LocationFactory locationFactory = new LocationFactory();
		intronFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 117l));
		Order<Location> exonFeatureLocation = new Order<Location>();
		exonFeatureLocation.addLocation(locationFactory.createLocalRange(100l, 117l));
		intronFeature.setLocations(intronFeatureLocation);
		exonFeature.setLocations(exonFeatureLocation);
		ValidationResult intronResult = check.check(intronFeature);
		assertEquals(0, intronResult.count("FeatureLengthCheck-1", Severity.WARNING));
		ValidationResult exonResult = check.check(exonFeature);
		assertEquals(0, exonResult.count("FeatureLengthCheck-1", Severity.WARNING));
	}
	
}
