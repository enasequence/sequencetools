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

public class NcRNAQualifierValueAndQualifierPatternCheckTest {

	private Feature feature;
	private FeatureFactory featureFactory;
	private NcRNAQualifierValueAndQualifierPatternCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("ncRNA");

		DataRow dataRow = new DataRow("product", "^(microRNA miR-).+$", "ncRNA_class", "miRNA");
        DataSetHelper.createAndAdd(FileName.NCRNA_QUALIFIER_VAL_QUALIFIER_PATTERN, dataRow);
		check = new NcRNAQualifierValueAndQualifierPatternCheck();
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		GlobalDataSets.clear();
		check.check(feature);
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NotNcRNA() {
		assertTrue(check.check(featureFactory.createFeature("feature")).isValid());
	}
	
	@Test
	public void testCheck_NoQualifiers() {
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoNcRNA_class() {
		feature.addQualifier("product", "microRNA miR-val");

		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoNcRNA_classValue() {
		feature.addQualifier("ncRNA_class");
		feature.addQualifier("product", "microRNA miR-val");
		
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_NoProduct() {
		feature.addQualifier("ncRNA_class", "miRNA");

		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoProductValue() {
		feature.addQualifier("ncRNA_class", "miRNA");
		feature.addQualifier("product");

		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("NcRNAQualifierValueAndQualifierPatternCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_WrongProductValue() {
		feature.addQualifier("ncRNA_class", "miRNA");
		feature.addQualifier("product", "value");
		
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("NcRNAQualifierValueAndQualifierPatternCheck", Severity.ERROR));
	}
	
	@Test
	public void testCheck_OK() {
		feature.addQualifier("ncRNA_class", "miRNA");
		feature.addQualifier("product", "microRNA miR-val");

		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_Message() {
		feature.addQualifier("ncRNA_class", "miRNA");
		feature.addQualifier("product", "x");

		ValidationResult result = check.check(feature);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "NcRNAQualifierValueAndQualifierPatternCheck", Severity.ERROR);
		assertEquals(
				"Qualifier \"product\" must have value which matches the pattern \"^(microRNA miR-).+$\" when qualifier \"ncRNA_class\" has value \"miRNA\".",
				messages.iterator().next().getMessage());
	}

}
