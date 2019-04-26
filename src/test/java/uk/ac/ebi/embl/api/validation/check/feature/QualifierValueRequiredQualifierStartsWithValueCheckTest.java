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
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;

public class QualifierValueRequiredQualifierStartsWithValueCheckTest
{

	private Feature feature;
	private QualifierValueRequiredQualifierStartsWithValueCheck check;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("feature");

		DataSetHelper.createAndAdd(FileName.QUALIFIER_VALUE_REQ_QUALIFIER_STARTSWITH_VALUE, new DataRow(	"product","MHC class I antigen","gene","HLA-A"),
				new DataRow("product","MHC class I antigen","gene","HLA-B"));
		check = new QualifierValueRequiredQualifierStartsWithValueCheck();
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
	public void testCheck_withValidGeneNoProduct()
	{
		feature.setSingleQualifierValue("gene", "HLA-Afdg");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_withValidProductNoGene()
	{
		feature.setSingleQualifierValue("product", "MHC class I antigen");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_withInvalidProductandInvalidGene()
	{
		feature.setSingleQualifierValue("product", "antigen");
		feature.setSingleQualifierValue("gene", "HLA-fdg");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_withvalidProductandInvalidGene()
	{
		feature.setSingleQualifierValue("product", "MHC class I antigen");
		feature.setSingleQualifierValue("gene", "HLA-fdg");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_withInvalidProductandvalidGene()
	{
		feature.setSingleQualifierValue("product", "antigen");
		feature.setSingleQualifierValue("gene", "HLA-Bfdg");
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueRequiredQualifierStartsWithValueCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_withvalidProductandvalidGene()
	{
		feature.setSingleQualifierValue("product", "MHC class I antigen");
		feature.setSingleQualifierValue("gene", "HLA-Afdg");
		ValidationResult result = check.check(feature);
		assertEquals(0, result.count("QualifierValueRequiredQualifierStartsWithValueCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_Message() {
		feature.setSingleQualifierValue("product", "antigen");
		feature.setSingleQualifierValue("gene", "HLA-Bfdg");
		ValidationResult result = check.check(feature);
		Collection<ValidationMessage<Origin>> messages = result.getMessages("QualifierValueRequiredQualifierStartsWithValueCheck", Severity.ERROR);
		assertEquals(
				"Qualifier \"product\" must have one of values \"MHC class I antigen\" when qualifier \"gene\" value starts with \"HLA-B\".",
				messages.iterator().next().getMessage());
	}

}
