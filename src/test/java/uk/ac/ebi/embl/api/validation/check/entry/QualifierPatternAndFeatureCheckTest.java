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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

public class QualifierPatternAndFeatureCheckTest
{

	private Entry entry;
	private FeatureFactory featureFactory;
	private QualifierFactory qualifierFactory;
	private QualifierPatternAndFeatureCheck check;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		qualifierFactory = new QualifierFactory();
		DataRow qualifier_pattern_feature_row1 = new DataRow(
				"rRNA",
				"product",
				"^(28S ribsoomal RNA-).*$");
		DataRow qualifier_pattern_feature_row2 = new DataRow(
				"tRNA",
				"product",
				"^(transfer RNA-).*$");

		entry = entryFactory.createEntry();
		GlobalDataSets.addTestDataSet(GlobalDataSetFile.QUALIFIER_PATTERN_FEATURE, qualifier_pattern_feature_row1,qualifier_pattern_feature_row2);
		check = new QualifierPatternAndFeatureCheck();
	}

	@After
	public void tearDown() {
		GlobalDataSets.resetTestDataSets();
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoFeatures()
	{
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoQualifiers()
	{

		entry.addFeature(featureFactory.createFeature("tRNA"));
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoRegexQualifierValue()
	{
		Feature feature = featureFactory.createFeature("feature");
		feature.addQualifier(qualifierFactory.createQualifier("product", "cpn60"));
		entry.addFeature(feature);
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_Valid()
	{
		Feature feature = featureFactory.createFeature("tRNA");
		feature.addQualifier(qualifierFactory.createQualifier("product", "transfer RNA-X"));
		entry.addFeature(feature);
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_withregexQualifierValueandnoFeature()
	{

		Feature feature = featureFactory.createFeature("CDS");
		feature.addQualifier(qualifierFactory.createQualifier("product", "transfer RNA-X"));
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("QualifierPatternAndFeatureCheck_1", Severity.ERROR));
	}
}
