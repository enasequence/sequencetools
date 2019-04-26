/*
 * # Copyright 2012-2012 EMBL-EBI, Hinxton outstation
 *
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 *
# http://www.apache.org/licenses/LICENSE-2.0
 *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */

package uk.ac.ebi.embl.api.validation.fixer.feature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.feature.QualifierCheck;
import uk.ac.ebi.embl.api.validation.fixer.feature.ObsoleteFeatureFix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QualifierWithinQualifierFixTest
{
	
	private Feature feature;
	private Qualifier qualifier;
	private QualifierWithinQualifierFix check;
	
	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		QualifierFactory qualifierFactory = new QualifierFactory();
		feature = featureFactory.createFeature("feature");
		qualifier = qualifierFactory.createQualifier("note");
		DataRow dataRow1 = new DataRow("EC_number", "N", "Y", "Y", null, 38, null);
		DataRow dataRow2 = new DataRow("cell_type", "N", "Y", "Y", null, 39, null);
		DataRow dataRow3 = new DataRow("focus", "Y", "Y", "N", null, 6, null);

		DataSetHelper.createAndAdd(FileName.FEATURE_QUALIFIER_VALUES, dataRow1,dataRow2,dataRow3);
		check = new QualifierWithinQualifierFix();
	}
	
	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_featurewithNoQualifier()
	{
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_NoQualifierwithinQualifier()
	{
		qualifier.setValue("DNA polymerase III, beta subunit");
		feature.addQualifier(qualifier);
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(Severity.FIX));// number of fixes
																// 0
	}
	
	@Test
	public void testCheck_Valuequalifierwithinqualifier()
	{
		qualifier.setValue("DNA polymerase III, beta subunit; /EC_number='2.7.7.7'");
		feature.addQualifier(qualifier);
		ValidationResult validationResult = check.check(feature);
		assertEquals(2, validationResult.count(Severity.FIX));// number of fixes
																// 0
	}
	
	@Test
	public void testCheck_noQuoteValueQualifierwithinQualifier()
	{
		qualifier.setValue("DNA polymerase III, beta subunit; /EC_number=2.7.7.7");
		feature.addQualifier(qualifier);
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("QualifierWithinQualifierFix_1", Severity.FIX));
		assertEquals(1, validationResult.count("QualifierWithinQualifierFix_2", Severity.FIX));
	}
	
	@Test
	public void testCheck_noValuequalifierwithinqualifier()
	{
		qualifier.setValue("hypothetical protein; /focus");
		feature.addQualifier(qualifier);
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("QualifierWithinQualifierFix_1", Severity.FIX));
		assertEquals(1, validationResult.count("QualifierWithinQualifierFix_2", Severity.FIX));
	}
	
	@Test
	public void testCheck_invalidqualifierwithinqualifier()
	{
		qualifier.setValue("hypothetical protein; /ec=2.7.7.7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count("QualifierWithinQualifierFix_1", Severity.FIX));
		assertEquals(0, validationResult.count("QualifierWithinQualifierFix_2", Severity.FIX));
		
	}
	
}
