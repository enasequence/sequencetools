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
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExclusiveQualifierTransformToNoteQualifierFixTest {

	private Feature feature;
	private Qualifier qualifier1,qualifier2;
	private ExclusiveQualifierTransformToNoteQualifierFix check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		DataRow dataRow1 = new DataRow(Qualifier.PRODUCT_QUALIFIER_NAME,Qualifier.PSEUDO_QUALIFIER_NAME);
		DataRow dataRow2 = new DataRow(Qualifier.PRODUCT_QUALIFIER_NAME,Qualifier.PSEUDOGENE_QUALIFIER_NAME);

		FeatureFactory featureFactory = new FeatureFactory();
		QualifierFactory qualifierFactory = new QualifierFactory();
		feature = featureFactory.createFeature("feature");
		qualifier1 = qualifierFactory.createQualifier(Qualifier.PRODUCT_QUALIFIER_NAME,"25S rRNA");
		qualifier2=qualifierFactory.createQualifier(Qualifier.PSEUDO_QUALIFIER_NAME);
		DataSetHelper.createAndAdd(FileName.EXCLUSIVE_QUALIFIERS_TO_REMOVE, dataRow1,dataRow2);
		check = new ExclusiveQualifierTransformToNoteQualifierFix();
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_noExclusiveQualifiers() {
		ValidationResult validationResult = check.check(feature);
		assertEquals(0,
				validationResult.count("ExclusiveQualifierToNoteQualifierFix", Severity.FIX));
	}

	// check for ec_number deleted value
	@Test
	public void testCheck_noRemoveQualifier() {
		feature.addQualifier(qualifier2);
		ValidationResult validationResult = check.check(feature);
		assertEquals(0,
				validationResult.count("ExclusiveQualifierToNoteQualifierFix", Severity.FIX));
	}

	@Test
	public void testCheck_withExclusiveandNoteQualifiers() {
		feature.addQualifier(qualifier1);
		feature.addQualifier(qualifier2);
		feature.addQualifier(Qualifier.NOTE_QUALIFIER_NAME,"note1");
		feature.addQualifier(Qualifier.NOTE_QUALIFIER_NAME,"note2");
		ValidationResult validationResult = check.check(feature);
		assertEquals("note1;25S rRNA",
				feature.getSingleQualifier(Qualifier.NOTE_QUALIFIER_NAME).getValue());
		assertEquals(1,
				validationResult.count("ExclusiveQualifierTransformToNoteQualifierFix", Severity.FIX));
	}
	
	@Test
	public void testCheck_withExclusiveandNoNoteQualifiers() {
		feature.addQualifier(qualifier1);
		feature.addQualifier(qualifier2);
		ValidationResult validationResult = check.check(feature);
		assertEquals("25S rRNA",
				feature.getSingleQualifier(Qualifier.NOTE_QUALIFIER_NAME).getValue());
		assertEquals(1,
				validationResult.count("ExclusiveQualifierTransformToNoteQualifierFix", Severity.FIX));
	}

}
