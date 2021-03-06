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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QualifierValueFixTest
{

	private Feature feature;
	private FeatureFactory featureFactory;
	private QualifierValueFix check;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		featureFactory = new FeatureFactory();
		DataRow dataRow1 = new DataRow("country","East Timor","Timor-Leste");
		DataRow dataRow2 = new DataRow("country","UK","United Kingdom");
		GlobalDataSets.addTestDataSet(GlobalDataSetFile.QUALIFIER_VALUE_TO_FIX_VALUE, dataRow1,dataRow2);
		feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		check = new QualifierValueFix();
	}

	@After
	public void tearDown() {
		GlobalDataSets.resetTestDataSets();
	}

	@Test
	public void testCheck_no_cv_fqual_fix_values()
	{
		ValidationResult result = check.check(feature);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_no_feaure()
	{
		ValidationResult result = check.check(null);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_ValueWithDoubleQuotes()
	{
		feature.addQualifier(Qualifier.NOTE_QUALIFIER_NAME,"Salmonel \"las\" enterica subsp.");
		ValidationResult result = check.check(feature);
		 assertEquals(1, result.count("QualifierValueFix_3", Severity.FIX));
		
	}
	@Test
	public void testCheck_altitudeValue()
	{
		feature.addQualifier(Qualifier.ALTITUDE_QUALIFIER_NAME,"-3283m.");
		ValidationResult result = check.check(feature);
		 assertEquals(1, result.count("QualifierValueFix_1", Severity.FIX));
		 assertEquals("-3283m", feature.getSingleQualifierValue(Qualifier.ALTITUDE_QUALIFIER_NAME));
	}

	@Test
	public void testCheck_altitudeValueComma()
	{
		feature.addQualifier(Qualifier.ALTITUDE_QUALIFIER_NAME,"-3,283m.");
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueFix_1", Severity.FIX));
		assertEquals("-3283m", feature.getSingleQualifierValue(Qualifier.ALTITUDE_QUALIFIER_NAME));
	}
	
	@Test
	public void testCheck_qualifierValueFix()
	{
		feature.addQualifier(Qualifier.COUNTRY_QUALIFIER_NAME,"UK");
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueFix_1", Severity.FIX));
		assertEquals("United Kingdom", feature.getSingleQualifierValue(Qualifier.COUNTRY_QUALIFIER_NAME));

	}

}
