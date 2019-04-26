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

public class QualifierAndRequiredQualifierinFeatureCheckTest {

	private Feature feature;
	private QualifierAndRequiredQualifierinFeatureCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();

		feature = featureFactory.createFeature("feature");

		DataRow dataRow = new DataRow("chromosome,segment,organelle", "map");

        DataSetHelper.createAndAdd(FileName.QUALIFIER_REQUIRED_QUALIFIER_IN_FEATURE, dataRow);
		check = new QualifierAndRequiredQualifierinFeatureCheck();
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
	public void testCheck_NoRequiredQualifiers() {
		feature.setSingleQualifier("map");
		
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("QualifierAndRequiredQualifierinFeatureCheck2", Severity.ERROR));
	}

	@Test
	public void testCheck_NoMap() {
		feature.setSingleQualifier("chromosome");
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_OnlyOneOfRequired() {
		feature.addQualifier("map");
		feature.addQualifier("chromosome");

		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_AllRequired() {
		feature.addQualifier("map");
		feature.addQualifier("chromosome");
		feature.addQualifier("segment");
		feature.addQualifier("organelle");
		
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_Message() {
		feature.addQualifier("map");

		ValidationResult result = check.check(feature);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "QualifierAndRequiredQualifierinFeatureCheck2", Severity.ERROR);
		assertEquals(
				"One of qualifiers \"chromosome, segment, organelle\" must exist when qualifier \"map\" exists within the same feature.",
				messages.iterator().next().getMessage());
	}

}
