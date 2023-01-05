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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClientImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HostQualifierFixTest
{

	private SourceFeature source;
	private HostQualifierFix check;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		source = featureFactory.createSourceFeature();
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.taxonClient.set(new TaxonomyClientImpl());
		check = new HostQualifierFix();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoSource()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoHost()
	{
		assertTrue(check.check(source).isValid());
	}
	
	@Test
	public void testCheck_hostwithNovalue()
	{
		assertTrue(check.check(source).isValid());
	}

	@Test
	public void testCheck_hostWithCommonName()
	{
		source.addQualifier(Qualifier.HOST_QUALIFIER_NAME, "eubacteria");
		ValidationResult result = check.check(source);
		assertEquals("Bacteria",source.getSingleQualifier(Qualifier.HOST_QUALIFIER_NAME).getValue());
		assertEquals(1, result.count("HostQualifierFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_hostWithNoCommonName()
	{
		source.addQualifier(Qualifier.HOST_QUALIFIER_NAME, "eubacter");
		ValidationResult result = check.check(source);
		assertTrue(result.isValid());
		assertEquals(0, result.count("HostQualifierFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_sourceWithNoTaxon()
	{
		source.addQualifier(Qualifier.HOST_QUALIFIER_NAME, "eubacter");
		ValidationResult result = check.check(source);
		assertTrue(result.isValid());
		assertEquals(0, result.count("HostQualifierFix_1", Severity.FIX));
	}

}
