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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EC_numberfromProductValueFixTest
{

	private Feature feature;
	private Qualifier qualifier;
	private EC_numberfromProductValueFix check;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		QualifierFactory qualifierFactory = new QualifierFactory();
		feature = featureFactory.createFeature("feature");
		qualifier = qualifierFactory.createQualifier("product");
		feature.addQualifier(qualifier);
		check = new EC_numberfromProductValueFix();
	}

	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_productwithEC_number()
	{
		qualifier.setValue("DNA polymerase III, beta subunit; ec=2.7.7.7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_1", Severity.FIX));
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_2", Severity.FIX));
	}

	@Test
	public void testCheck_productwithNoEC_number()
	{
		qualifier.setValue("DNA polymerase III, beta subunit");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(Severity.FIX));//number of fixes 0
	}

	@Test
	public void testCheck_productwithInvalidEC_number()
	{
		qualifier.setValue("DNA polymerase III, beta subunit;7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(Severity.FIX));//number of fixes 0
	}
	@Test
	public void testCheck_featurewithEc_number()
	{
		qualifier.setValue("DNA polymerase III, beta subunit; ec=2.7.7.7");
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME,"2.7.7.7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_1", Severity.FIX));
		assertEquals(0, validationResult.count("EC_numberfromProductValueFix_2", Severity.FIX));
	}
	
	@Test
	public void testCheck_unknownProductwithEc_number()
	{
		qualifier.setValue("DNA polymerase III,unknown beta subunit; ec=2.7.7.7");
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME,"2.7.7.7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_1", Severity.FIX));
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_3", Severity.FIX));
	}
	
	@Test
	public void testCheck_hypotheticalProductwithEc_number()
	{
		qualifier.setValue("DNA polymerase III,hypothetical protein beta subunit; ec=2.7.7.7");
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME,"2.7.7.7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_1", Severity.FIX));
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_3", Severity.FIX));
	}
	@Test
	public void testCheck_featurewithInvalidEc_number()
	{
		qualifier.setValue("hypothetical protein; ec=2.7.7.7");
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME,"2.7.7.7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_1", Severity.FIX));
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_3", Severity.FIX));
	}
	@Test
	public void testCheck_productwithInvalidEc_number()
	{
		qualifier.setValue("hypothetical protein; ec=2.7.7.7");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("EC_numberfromProductValueFix_1", Severity.FIX));
	}
	
}
