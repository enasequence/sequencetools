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

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class PCRPrimersQualifierCheckTest {

	private Feature feature;
	private Qualifier qualifier;

	private PCRPrimersQualifierCheck check;
	FeatureFactory featureFactory = new FeatureFactory();
	QualifierFactory qualifierFactory = new QualifierFactory();

	@Before
	public void setUp() {
		feature = featureFactory.createFeature("feature");
		qualifier = qualifierFactory
				.createQualifier(Qualifier.PCR_PRIMERS_QUALIFIER_NAME);
		feature.addQualifier(qualifier);
		DataRow dataRow = new DataRow("a,c,g,t,m,r,w,s,y,k,v,h,d,b,n");
		DataSetHelper.createAndAdd(FileName.NUCLEOTIDE_CODE, dataRow);
		check = new PCRPrimersQualifierCheck();
	}

	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifier() {
		feature.removeQualifier(qualifier);
		assertTrue(check.check(feature).isValid());
	}

	@Test
	public void testCheck_invalidFormat() {
		qualifier
				.setValue("fwd_name: fwd_name:, fwd_seq:gacgtcgccggtgacggcaccaccac, rev_name:H1595(Cpn60)+M13(48R), rev_seq: cgacggtcgccgaagcccggggcctt");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("PCRPrimersQualifierCheck_1",
				Severity.ERROR));
		assertTrue(!validationResult.isValid());
	}

	@Test
	public void testCheck_invalidNucleotide() {
		qualifier
				.setValue("fwd_name: H1594(Cpn60)+M13(-40F), fwd_seq:gaxgtcgccggtgacggcaccaccac, rev_name:H1595(Cpn60)+M13(48R), rev_seq: cgacggtcgccgaagcccggggcctt");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("PCRPrimersQualifierCheck_4",
				Severity.ERROR));
		assertTrue(!validationResult.isValid());
	}

	@Test
	public void testCheck_sequencewithopenAngleBracket() {
		qualifier
				.setValue("fwd_name: H1594(Cpn60)+M13(-40F), fwd_seq:gag<gtcgccggtgacggcaccaccac, rev_name:H1595(Cpn60)+M13(48R), rev_seq: cgacggtcgccgaagcccggggcctt");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("PCRPrimersQualifierCheck_2",
				Severity.ERROR));
		assertTrue(!validationResult.isValid());
	}

	@Test
	public void testCheck_sequencewithclosedAngleBracket() {
		qualifier
				.setValue("fwd_name: H1594(Cpn60)+M13(-40F), fwd_seq:gag>gtcgccggtgacggcaccaccac, rev_name:H1595(Cpn60)+M13(48R), rev_seq: cgacggtcgccgaagcccggggcctt");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("PCRPrimersQualifierCheck_2",
				Severity.ERROR));
		assertEquals(1, validationResult.count("PCRPrimersQualifierCheck_3",
				Severity.ERROR));
		assertTrue(!validationResult.isValid());
	}

	@Test
	public void testCheck_valid() {
		qualifier
				.setValue("fwd_name: H1594(Cpn60)+M13(-40F), fwd_seq:gaggtcgccggtgacggcaccaccac, rev_name:H1595(Cpn60)+M13(48R), rev_seq: cgacggtcgccgaagcccggggcctt");
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count("PCRPrimersQualifierCheck_2",
				Severity.ERROR));
		assertEquals(0, validationResult.count("PCRPrimersQualifierCheck_3",
				Severity.ERROR));
		assertTrue(validationResult.isValid());
	}

}
