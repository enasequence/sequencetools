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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class SourceQualifierPatternAndFeatureCheckTest {

	private Entry entry;
	private SourceFeature source;

	private FeatureFactory featureFactory;
	private QualifierFactory qualifierFactory;
	private SourceQualifierPatternAndFeatureCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		qualifierFactory = new QualifierFactory();

		entry = entryFactory.createEntry();
		source = featureFactory.createSourceFeature();
		entry.addFeature(source);

		GlobalDataSets.addTestDataSet(GlobalDataSetFile.SOURCE_QUALIFIER_PATTERN_FEATURE, new DataRow("LTR","note",".*endogenous retrovirus$"));
		check = new SourceQualifierPatternAndFeatureCheck();
	}

	@After
	public void tearDown() {
		GlobalDataSets.resetTestDataSets();
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoNoteQualifier() {
		entry.addFeature(featureFactory.createFeature("LTR"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoQualifierValue() {
		source.addQualifier(qualifierFactory.createQualifier("note"));
		entry.addFeature(featureFactory.createFeature("LTR"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_Valid() {
		source.addQualifier(qualifierFactory.createQualifier("note",
				"X endogenous retrovirus"));
		entry.addFeature(featureFactory.createFeature("LTR"));

		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoFeature() {
		source.addQualifier(qualifierFactory.createQualifier("note",
				"X endogenous retrovirus"));

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("SourceQualifierPatternAndFeatureCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_WrongFeature() {
		source.addQualifier(qualifierFactory.createQualifier("note",
				"X endogenous retrovirus"));
		entry.addFeature(featureFactory.createFeature("LTX"));

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("SourceQualifierPatternAndFeatureCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		source.addQualifier(qualifierFactory.createQualifier("note",
				"X endogenous retrovirus"));

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "SourceQualifierPatternAndFeatureCheck", Severity.ERROR);
		assertEquals(
				"Feature \"LTR\" is required when qualifier \"note\" matches pattern \".*endogenous retrovirus$\".",
				messages.iterator().next().getMessage());
	}

}
