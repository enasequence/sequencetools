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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.feature.QualifierValueRequiredQualifierValueCheck;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.SourceFeatureQualifierCheck;

public class SourceFeatureQualifierCheckTest {
	private Entry entry;
	private SourceFeature source,source1;
	private FeatureFactory featureFactory;
	private QualifierFactory qualifierFactory;
	private SourceFeatureQualifierCheck check;
	private Feature feature;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		qualifierFactory = new QualifierFactory();
		DataSet dataSet = new DataSet();
		entry = entryFactory.createEntry();
		source = featureFactory.createSourceFeature();
		
		entry.addFeature(source);
		
		dataSet.addRow(new DataRow("strain,isolate,clone", "gene", ".*rRNA$"));
		check = new SourceFeatureQualifierCheck(dataSet);
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		check = new SourceFeatureQualifierCheck();
		check.check(entry);
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_SourceWithNoRequiredsQualifier() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		source.addQualifier("sub_species");
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		source.addQualifier("strain");
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_NoSourceFeature() {
		entry.removeFeature(source);
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_NoGene() {
		source.addQualifier(qualifierFactory
				.createQualifier("strain", "BALB/c"));
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}
	
	@Test
	public void testCheck_MultipleSourceDifferentOrganism() {
		source1 = featureFactory.createSourceFeature();
		entry.addFeature(source1);
		source.addQualifier(qualifierFactory.createQualifier("organism",
				"Homo sapiens"));
		source1.addQualifier(qualifierFactory.createQualifier("organism",
				"Cloning vector pBeloBAC11"));
		source.setFocus(false);
		source1.setFocus(false);
		source.setTransgenic(false);
		source1.setTransgenic(false);
		ValidationResult result = check.check(entry);
		assertEquals(1,
				result.count("SourceFeatureQualifierCheck2", Severity.ERROR));
	}

	@Test
	public void testCheck_GeneWithNoPattern() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "b");
		source.addQualifier(qualifierFactory
				.createQualifier("strain", "BALB/c"));
		ValidationResult result = check.check(entry);
		assertEquals(0,
				result.count("SourceFeatureQualifierCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		feature = featureFactory.createFeature("gene");
		feature.addQualifier("gene", "10S rRNA");
		entry.addFeature(feature);
		source.addQualifier("sub_species");
		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
				"SourceFeatureQualifierCheck1", Severity.ERROR);
		assertEquals(
				"Any of the qualifiers \"strain, isolate, clone\" must exist in the Source feature if there is an rRNA gene.",
				messages.iterator().next().getMessage());
	}

}
