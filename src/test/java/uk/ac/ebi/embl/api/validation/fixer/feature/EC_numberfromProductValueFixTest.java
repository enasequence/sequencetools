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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EC_numberfromProductValueFixTest
{

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
	public void testEcNumberExtraction() {
		checkEcNumberAndProduct("UDP-3-O-[3-hydroxymyristoyl] / 3-hydroxyacyl-[acyl-carrier-protein] dehydratase [ec:3.5.1.108,4.2.1.123]",
				"UDP-3-O-[3-hydroxymyristoyl] / 3-hydroxyacyl-[acyl-carrier-protein] dehydratase",2, "3.5.1.108", "4.2.1.123");
		checkEcNumberAndProduct("UDP-3-O-[3-hydroxymyristoyl] N-acetylglucosamine deacetylase / 3-hydroxyacyl-[acyl-carrier-protein] dehydratase [ec:3.5.1.1084.2.1.123]",
				"UDP-3-O-[3-hydroxymyristoyl] N-acetylglucosamine deacetylase / 3-hydroxyacyl-[acyl-carrier-protein] dehydratase",1, "3.5.1.108");
		checkEcNumberAndProduct("UDP- dehydratase [ec:3.5.1.104.2.1.123]", "UDP- dehydratase",1, "3.5.1.104");
		checkEcNumberAndProduct("dsjhdjkssjsskj9827867807&^&*%^&^&*()- 12.34.2.n", "dsjhdjkssjsskj9827867807&^&*%^&^&*()-", 1,"12.34.2.n");
		checkEcNumberAndProduct("mksfkds~~*[ec]kjks[EC=1.2.3.567]", "mksfkds~~*[ec]kjks",1,"1.2.3.567");
		checkEcNumberAndProduct("mksfkds~~*[ec]kjks[EC=1.2.3.567.678]","mksfkds~~*[ec]kjks.678]",1,"1.2.3.567");
		checkEcNumberAndProduct("mksfkds~~*[ec]kjks[EC=1.-.3.-]", "mksfkds~~*[ec]kjks", 1,"1.-.3.-");
		checkEcNumberAndProduct("mksfkds~kjks1.-.3.-]", "mksfkds~kjks", 1,"1.-.3.-");
		checkEcNumberAndProduct("mksfkds~kjks 1.-.3.n1]", "mksfkds~kjks", 1,"1.-.3.n1");
		checkEcNumberAndProduct("mksfkds[ec:1.2.3]kjks[EC=1.2.3.567.678]","mksfkds[ec:1.2.3]kjks.678]",1,"1.2.3.567");
		checkEcNumberAndProduct("mksfkds[1.2.3.4]kjks[ec:1.2.3.567.678]","mksfkdskjks.678]",2,"1.2.3.4","1.2.3.567");
		checkEcNumberAndProduct("mksfkds1.2.3.4kjks[ec:1.2.3.567]","mksfkdskjks",2,"1.2.3.4","1.2.3.567");
		checkEcNumberAndProduct("mksfkdskjks[EC=2.3.567]","mksfkdskjks[EC=2.3.567]",0,"nothing");
		checkEcNumberAndProduct("mksfkdskjks[EC=.2.3.567]","mksfkdskjks[EC=.2.3.567]",0,"nothing");
		checkEcNumberAndProduct("DNA polymerase III, beta subunit; ec=2.7.7.7","DNA polymerase III, beta subunit;",1,"2.7.7.7");
	}

	private void checkEcNumberAndProduct(String product, String expectedProduct, int noOfEcsExpected, String... ecNumbers) {

		ImmutablePair<String, List<String>> ecNumberProductL = check.getEcNumberAndProduct(product);

		assertEquals(expectedProduct, ecNumberProductL.left);
		assertEquals(noOfEcsExpected, ecNumberProductL.right.size());
		for (int i = 0; i < noOfEcsExpected; i++) {
			assertEquals(ecNumbers[i], ecNumberProductL.right.get(i));
		}
	}

	private Feature feature;
	private Qualifier qualifier;
	private EC_numberfromProductValueFix check;


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
