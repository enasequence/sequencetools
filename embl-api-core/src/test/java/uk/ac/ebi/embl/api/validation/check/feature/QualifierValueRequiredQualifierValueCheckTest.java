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

public class QualifierValueRequiredQualifierValueCheckTest {

	private Feature feature;
	private QualifierValueRequiredQualifierValueCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();

		feature = featureFactory.createFeature("feature");

        DataSetHelper.createAndAdd(FileName.QUALIFIER_VALUE_REQ_QUALIFIER_VALUE, new DataRow("cell_type", "endothelial,stromal","tissue_type", "heart"));
        check = new QualifierValueRequiredQualifierValueCheck();
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		DataSetHelper.clear();
		check.check(feature);
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifiers() {
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoTissueType() {
		feature.setSingleQualifierValue("cell_type", "endothelial");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoTissueTypeValue() {
		feature.setSingleQualifier("tissue_type");
		feature.setSingleQualifierValue("cell_type", "endothelial");

		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_NoCellType() {
		feature.setSingleQualifierValue("tissue_type", "heart");
		ValidationResult result = check.check(feature);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_NoCellTypeValue() {
		feature.setSingleQualifierValue("tissue_type", "heart");
		feature.setSingleQualifier("cell_type");
		
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueRequiredQualifierValueCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Endothelial() {
		feature.setSingleQualifierValue("tissue_type", "heart");
		feature.setSingleQualifierValue("cell_type", "endothelial");

		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_Stromal() {
		feature.setSingleQualifierValue("tissue_type", "heart");
		feature.setSingleQualifierValue("cell_type", "stromal");
		
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_WrongValue() {
		feature.setSingleQualifierValue("tissue_type", "heart");
		feature.setSingleQualifierValue("cell_type", "some value");
		
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueRequiredQualifierValueCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		feature.setSingleQualifierValue("tissue_type", "heart");
		feature.setSingleQualifierValue("cell_type", "some value");
		
		ValidationResult result = check.check(feature);
		Collection<ValidationMessage<Origin>> messages = result.getMessages("QualifierValueRequiredQualifierValueCheck", Severity.ERROR);
		assertEquals(
				"Qualifier \"cell_type\" must have one of values \"endothelial, stromal\" when qualifier \"tissue_type\" has value \"heart\".",
				messages.iterator().next().getMessage());
	}

}
