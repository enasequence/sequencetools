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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;

public class QualifierValueRequiredQualifierValueEntryCheckTest {

	private Entry entry;
	private Feature feature1, feature2;
	private QualifierValueRequiredQualifierValueEntryCheck check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		feature1 = featureFactory.createFeature("gene");
		feature2 = featureFactory.createFeature("LTR");
		entry.addFeature(feature1);
		entry.addFeature(feature2);

		DataSetHelper.createAndAdd(FileName.QUALIFIER_VAL_REQUIRED_QUALIFIER_ENTRY, new DataRow("organelle", "mitochondrion", "gene",
				"12S rRNA"));
		check = new QualifierValueRequiredQualifierValueEntryCheck();
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		DataSetHelper.clear();
		check.check(entry);
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifiers() {
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoGene() {
		feature1.setSingleQualifierValue("organelle", "mitochondrion");
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoGeneValue() {
		feature1.setSingleQualifier("gene");
		feature2.setSingleQualifierValue("organelle", "mitochondrion");

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoOrganelle() {
		feature1.setSingleQualifierValue("gene", "12S rRNA");
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_NoOrganelleValue() {
		feature1.setSingleQualifierValue("gene", "12S rRNA");
		feature2.setSingleQualifier("organelle");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count(
				"QualifierValueRequiredQualifierValueEntryCheck",
				Severity.WARNING));
	}

	@Test
	public void testCheck_WrongValue() {
		feature1.setSingleQualifierValue("gene", "12S rRNA");
		feature2.setSingleQualifierValue("organelle", "some value");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count(
				"QualifierValueRequiredQualifierValueEntryCheck",
				Severity.WARNING));
	}

	@Test
	public void testCheck_Message() {
		feature1.setSingleQualifierValue("gene", "12S rRNA");
		feature2.setSingleQualifierValue("organelle", "some value");

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
				"QualifierValueRequiredQualifierValueEntryCheck",
				Severity.WARNING);
		assertEquals(
				"Qualifier \"organelle\" must have one of values \"mitochondrion\" when qualifier \"gene\" has value \"12S rRNA\" in any feature.",
				messages.iterator().next().getMessage());
	}

}
