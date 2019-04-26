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

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 26-Jan-2009 Time: 10:50:52 To
 * change this template use File | Settings | File Templates.
 */
public class FeatureQualifiersRequiredCheckTest {

	private Feature feature;
	private FeatureQualifiersRequiredCheck check;

	@Before
	public void setUp() {
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("misc_feature");

		DataSetHelper.createAndAdd(FileName.FEATURE_REQUIRE_QUALIFIERS,new DataRow("misc_feature") );

		check = new FeatureQualifiersRequiredCheck();
		check.setPopulated();
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	// check for feature which require at least one
	// qualifier:feature-require-qualifiers.tsv
	@Test
	public void testCheck_NoRecomendedQualifier() {
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("misc_feature");
		ValidationResult validationResult = check.check(feature);
		assertTrue(!validationResult.isValid());
		assertEquals(1, validationResult.count(
				"FeatureQualifiersRequiredCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Fine() {
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("operon");
		feature.addQualifier("operon");
		feature.addQualifier("operon");
		ValidationResult validationResult = check.check(feature);
		assertTrue(validationResult.isValid());
	}
}
