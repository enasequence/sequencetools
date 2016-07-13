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
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;

public class QualifierValueNotQualifierPatternCheckTest {

	private Feature feature;
	private QualifierValueNotQualifierPatternCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("feature");

		DataRow dataRow = new DataRow("organism", ".+\\s(var.)\\s", "(null)", "cultivar");
        DataSet dataSet = new DataSet();
        dataSet.addRow(dataRow);
		check = new QualifierValueNotQualifierPatternCheck(dataSet);
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		check = new QualifierValueNotQualifierPatternCheck();
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
	public void testCheck_NoCultivar() {
		feature.setSingleQualifierValue("organism", "X var. zebrafish");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoOrganism() {
		feature.setSingleQualifierValue("cultivar", "zebrafish");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NoOrganismValue() {
		feature.setSingleQualifierValue("cultivar", "zebrafish");
		feature.setSingleQualifier("organism");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_NotPattern() {
		feature.setSingleQualifierValue("cultivar", "X var. zebrafish X");
		feature.setSingleQualifier("organism");
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck() {
		feature.setSingleQualifierValue("cultivar", "zebrafish");
		feature.setSingleQualifierValue("organism", "X var. zebrafish");

		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierValueNotQualifierPatternCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		feature.setSingleQualifierValue("cultivar", "zebrafish");
		feature.setSingleQualifierValue("organism", "X var. zebrafish");
		
		ValidationResult result = check.check(feature);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "QualifierValueNotQualifierPatternCheck", Severity.ERROR);
		assertEquals(
				"Qualifier organism must not have value which matches the pattern .+\\s(var.)\\s + <value> +  where <value> is a value of qualifier cultivar.",
				messages.iterator().next().getMessage());
	}

}
