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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QualifierValueNotQualifierCheckTest {

	private Feature feature;
	private QualifierValueNotQualifierCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();

		feature = featureFactory.createFeature("feature");

		DataRow dataRow = new DataRow("macronuclear", "organelle", "mitochondrion");
        GlobalDataSets.addTestDataSet(GlobalDataSetFile.QUALIFIER_VALUE_NOT_QUALIFIER, dataRow);
		check = new QualifierValueNotQualifierCheck();
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
	public void testCheck_NoOrganelle() {
		feature.setSingleQualifier("macronuclear");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoMacronuclear() {
		feature.setSingleQualifierValue("sub_clone", "value");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoOrganelleValue() {
		feature.setSingleQualifier("sub_clone");
		feature.setSingleQualifier("macronuclear");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck() {
		feature.addQualifier("organelle", "mitochondrion");
		feature.addQualifier("macronuclear");

		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueNotQualifierCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_MacronuclearValue() {
		feature.addQualifier("organelle", "mitochondrion");
		feature.addQualifier("macronuclear", "some value");

		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueNotQualifierCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		feature.addQualifier("organelle", "mitochondrion");
		feature.addQualifier("macronuclear", "some value");

		ValidationResult result = check.check(feature);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "QualifierValueNotQualifierCheck", Severity.ERROR);
		assertEquals(
				"Qualifier \"macronuclear\" must not exist when qualifier \"organelle\" has value \"mitochondrion\".",
				messages.iterator().next().getMessage());
	}

}
