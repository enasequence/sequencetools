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
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;

public class EC_numberFormatCheckTest
{
	
	private Feature feature;
	private EC_numberFormatCheck check;
	
	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("feature");
		check = new EC_numberFormatCheck();
	}
	
	@Test
	public void testCheck_NoFeature() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_NoQualifiers() throws ValidationEngineException
	{
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_NoEcnumber() throws ValidationEngineException
	{
		feature.setSingleQualifier("qual1");
		feature.setSingleQualifier("qual2");
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_invalidEcnumber() throws SQLException, ValidationEngineException
	{
		
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.i");
		ValidationResult result=check.check(feature);
		assertTrue(!result.isValid());
		assertEquals(1, result.count("EC_numberFormatCheck", Severity.ERROR));

	}
	
	@Test
	public void testCheck_invalidEcnumber1() throws ValidationEngineException 
	{
		
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "-.6.9.9");
		ValidationResult result=check.check(feature);
		assertTrue(!result.isValid());
		assertEquals(1, result.count("EC_numberFormatCheck", Severity.ERROR));

	}
	
	@Test
	public void testCheck_validEcnumber() throws SQLException, ValidationEngineException
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.-");
		ValidationResult result=check.check(feature);
		assertTrue(result.isValid());
		assertEquals(0, result.count("EC_numberFormatCheck", Severity.ERROR));
		
	}
	
	@Test
	public void testCheck_validEcnumber1() throws SQLException, ValidationEngineException
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.n");
		ValidationResult result=check.check(feature);
		assertTrue(result.isValid());
		assertEquals(0, result.count("EC_numberFormatCheck", Severity.ERROR));
		
	}
	
    @Test
	public void testCheck_validEcnumber2() throws SQLException, ValidationEngineException
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.5");
		ValidationResult result=check.check(feature);
		assertTrue(result.isValid());
		assertEquals(0, result.count("EC_numberFormatCheck", Severity.ERROR));
		
	}
	
	@Test
	public void testCheck_validEcnumber3() throws SQLException, ValidationEngineException
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.-.-.-");
		ValidationResult result=check.check(feature);
		assertTrue(result.isValid());
		assertEquals(0, result.count("EC_numberFormatCheck", Severity.ERROR));
		
	}
	
	@Test
	public void testCheck_validEcnumber4() throws SQLException, ValidationEngineException
	{
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.6.1.n234");
		ValidationResult result=check.check(feature);
		assertTrue(result.isValid());
		assertEquals(0, result.count("EC_numberFormatCheck", Severity.ERROR));
		
	}
}
