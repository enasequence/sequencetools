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

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class DeprecatedQualifiersCheckTest {

	private Feature feature;
	private DeprecatedQualifiersCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();

		feature = featureFactory.createFeature("feature");

        GlobalDataSets.addTestDataSet(GlobalDataSetFile.DEPRECATED_QUALIFIERS, new DataRow("partial","(null)","N"), new DataRow("specific_host","host","Y"), new DataRow("david","bod","N"));
        check = new DeprecatedQualifiersCheck();
	}

	@After
	public void tearDown() {
		GlobalDataSets.resetTestDataSets();
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifiers() {
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_OtherQualifier() {
		feature.addQualifier("qual1");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck() {
		feature.addQualifier("partial");
		
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("DeprecatedQualifiersCheck", Severity.ERROR));
	}

    @Test
	public void testReplaceCheck() {
		feature.addQualifier("david");

		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("DeprecatedQualifiersCheck-2", Severity.ERROR));
	}

    @Test
	public void testFixCheck() {
		feature.addQualifier("specific_host");

		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("DeprecatedQualifiersCheck-3", Severity.WARNING));
	}

	@Test
	public void testCheck_Message() {
		feature.addQualifier("partial");
		
		ValidationResult result = check.check(feature);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "DeprecatedQualifiersCheck", Severity.ERROR);
		assertEquals("The \"partial\" qualifier is deprecated.",
				messages.iterator().next().getMessage());
	}	

}
