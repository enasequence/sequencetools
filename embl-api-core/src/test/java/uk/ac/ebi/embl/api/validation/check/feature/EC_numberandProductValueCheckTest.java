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
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;

public class EC_numberandProductValueCheckTest
{
	
	private Feature feature;
	private EC_numberandProductValueCheck check;
	
	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("feature");
		check = new EC_numberandProductValueCheck();
	}
	
	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_NoQualifiers()
	{
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_OtherQualifier()
	{
		feature.setSingleQualifier("qual1");
		feature.setSingleQualifier("qual2");
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_EcnumberQualifier()
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.15");
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_validProductandEcnumberQualifier()
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.15");
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "expressed protein");
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_unknownProductandEcnumberQualifier()
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.15");
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "unknown");
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("EC_numberandProductValueCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_unknownProductQualifier()
	{
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "unknown");
		ValidationResult result = check.check(feature);
		assertEquals(0, result.count("EC_numberandProductValueCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_ProductQualifierwithEcnumberValue()
	{
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "expressed protein ;ec=3.6.1.15");
		ValidationResult result = check.check(feature);
		assertEquals(0, result.count("EC_numberandProductValueCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_unknownProductQualifierwithEcnumberValue()
	{
		feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "unknown;ec=3.6.1.15");
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("EC_numberandProductValueCheck", Severity.ERROR));
	}
	
}
