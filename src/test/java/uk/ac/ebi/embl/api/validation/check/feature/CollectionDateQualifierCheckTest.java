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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import java.text.ParseException;

import static org.junit.Assert.*;

public class CollectionDateQualifierCheckTest {

	private Feature feature;
	private Qualifier qualifier;

	private CollectionDateQualifierCheck check;
	FeatureFactory featureFactory = new FeatureFactory();
	QualifierFactory qualifierFactory = new QualifierFactory();

	@Before
	public void setUp() {
		feature = featureFactory.createFeature("feature");
		qualifier = qualifierFactory
				.createQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
		feature.addQualifier(qualifier);
		check = new CollectionDateQualifierCheck();
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoDate() {
		feature.removeQualifier(qualifier);
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_invalidDate() {
		qualifier.setValue("INVALID");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));
	}

	@Test
	public void testCheck_invalidDateRange() {
		qualifier.setValue("2004/2003"); // from date is > to date
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));
	}	
	
	@Test
	public void testCheck_futureDate1() {
		qualifier.setValue("2050");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));
	}

	@Test
	public void testCheck_futureDate2() {
		qualifier.setValue("Oct-2050");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));
	}
	
	@Test
	public void testCheck_futureDate3() {
		qualifier.setValue("21-Oct-2050");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));
	}

	@Test
	public void testCheck_InvalidDayMonth() throws ParseException {
	/*	assertTrue(GenericValidator.isDate("2024-03-28", "yyyy-MM-dd", true));
		assertTrue(GenericValidator.isDate("2024-12", "yyyy-MM", true));


		assertTrue(GenericValidator.isDate("2024-mar", "yyyy-MMM", true));

		assertTrue(GenericValidator.isDate("2024", "yyyy", true));
*/
		qualifier.setValue("51-Oct-2022");  //invalid date
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));


		qualifier.setValue("00-Oct-2022");  //invalid date
		validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));

		qualifier.setValue("01-Ott-2022");  //invalid month
		validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));

		qualifier.setValue("01-00-2022");  //invalid month
		validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));

		qualifier.setValue("01-Oct-2022");  //valid date
		validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));

		qualifier.setValue("01-Ott-2022");  //invalid month
		validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));

		qualifier.setValue("2022-10-02");  //valid date
		validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));

		qualifier.setValue("2022-15-01");  //invalid month
		validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));


		qualifier.setValue("00-00-2022");  //invalid month
		validationResult = check.check(feature);
		assertEquals(1, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertFalse(validationResult.isValid());
		assertFalse(check.isValid(qualifier.getValue()));

		qualifier.setValue("2022-10-02");  //valid date
		validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}

	@Test
	public void testCheck_validDate1() {
		qualifier.setValue("21-Oct-1952");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}

	@Test
	public void testCheck_validDate2() {
		qualifier.setValue("Oct-1952");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}

	@Test
	public void testCheck_validDate3() {
		qualifier.setValue("1952");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}

	@Test
	public void testCheck_validDate4() {
		qualifier.setValue("1952-10-21T11:43Z");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}
	
	@Test
	public void testCheck_validDate5() {
		qualifier.setValue("1952-10-21T11Z");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	

	@Test
	public void testCheck_validDate6() {
		qualifier.setValue("1952-10-21");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	

	@Test
	public void testCheck_validDate7() {
		qualifier.setValue("1952-10");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	

	@Test
	public void testCheck_validDate8() {
		qualifier.setValue("21-Oct-1952/15-Feb-1953");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	

	@Test
	public void testCheck_validDate9() {
		qualifier.setValue("Oct-1952/Feb-1953");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	
	
	@Test
	public void testCheck_validDate10() {
		qualifier.setValue("1952/1953");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	

	@Test
	public void testCheck_validDate11() {
		qualifier.setValue("1952-10-21/1953-02-15");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	

	@Test
	public void testCheck_validDate12() {
		qualifier.setValue("1952-10/1953-02");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}	
	
	@Test
	public void testCheck_validDate13() {
		qualifier.setValue("1952-10-21T11:43Z/1952-10-21T17:43Z");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_1", Severity.ERROR));
		assertEquals(0, validationResult.count(
				"CollectionDateQualifierCheck_2", Severity.ERROR));
		assertTrue(validationResult.isValid());
		assertTrue(check.isValid(qualifier.getValue()));
	}		
}
