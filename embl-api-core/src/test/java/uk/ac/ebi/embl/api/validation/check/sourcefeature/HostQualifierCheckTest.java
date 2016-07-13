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

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.HostQualifierCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HostQualifierCheckTest
{
	private SourceFeature source;
	private HostQualifierCheck check;
	private EmblEntryValidationPlanProperty planProperty;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		check=new HostQualifierCheck();
		planProperty=new EmblEntryValidationPlanProperty();
		planProperty.taxonHelper.set(new TaxonHelperImpl());
		check.setEmblEntryValidationPlanProperty(planProperty);
		FeatureFactory featureFactory = new FeatureFactory();
		source = featureFactory.createSourceFeature();
	}

	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoHost()
	{
		assertTrue(check.check(source).isValid());
	}
	
	@Test
	public void testCheck_HostwithnoCommonNameValue()
	{
		source.addQualifier(Qualifier.HOST_QUALIFIER_NAME, "xxx");
		assertTrue(check.check(source).isValid());
    }

	@Test
	public void testCheck_hostWithCommonName()
	{
		source.addQualifier(Qualifier.HOST_QUALIFIER_NAME, "Cofan woodlizard");
		ValidationResult result = check.check(source);
		assertTrue(!result.isValid());
		assertEquals(1, result.count("HostQualifierCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_sourceWithScientificName()
	{
		source.addQualifier(Qualifier.HOST_QUALIFIER_NAME, "Homo sapiens");
		ValidationResult result = check.check(source);
		assertTrue(result.isValid());
		assertEquals(0, result.count("HostQualifierCheck_1", Severity.ERROR));
	}
	
}
