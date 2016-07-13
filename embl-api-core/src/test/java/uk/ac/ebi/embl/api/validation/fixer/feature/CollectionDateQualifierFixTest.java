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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionDateQualifierFixTest {

	private Feature feature;
	private Qualifier qualifier;
	private CollectionDateQualifierFix check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		QualifierFactory qualifierFactory = new QualifierFactory();
		feature = featureFactory.createFeature("feature");
		qualifier = qualifierFactory.createQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
		feature.addQualifier(qualifier);
		check = new CollectionDateQualifierFix();
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_NoQualifier() {
		
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature1 = featureFactory.createFeature("feature1");
		assertTrue(check.check(feature1).isValid());
	}

	@Test
	public void testCheck_validcollectionDateValue() {
		qualifier.setValue("11-Jun-2012");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0,
				validationResult.count("CollectionDateQualifierFix_ID_1", Severity.FIX));
	}

	@Test
	public void testCheck_invalidCollectionDateValue() {
		qualifier.setValue("1-Jun-2012");
		ValidationResult validationResult = check.check(feature);
		assertEquals(qualifier.getValue(),"01-Jun-2012");
		assertEquals(1,
				validationResult.count("CollectionDateQualifierFix_1", Severity.FIX));
	}
	
	@Test
	public void testCheck_CollectionDateValueFormat() {
		qualifier.setValue("1-04-2012");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0,
				validationResult.count("CollectionDateQualifierFix_ID_1", Severity.FIX));
	}
	

}
