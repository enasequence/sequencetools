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
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.SingleSourceQualifiersCheck;

public class SingleSourceQualifiersCheckTest {

	private Entry entry;
	private Feature source1, source2;
	private FeatureFactory featureFactory;
	private SingleSourceQualifiersCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();

		entry = entryFactory.createEntry();
		source1 = featureFactory.createFeature(Feature.SOURCE_FEATURE_NAME);
		source2 = featureFactory.createFeature(Feature.SOURCE_FEATURE_NAME);
		entry.addFeature(source1);
		entry.addFeature(source2);

		DataRow dataRow = new DataRow("focus");

		DataSet dataSet = new DataSet();
		dataSet.addRow(dataRow);
		GlobalDataSets.add(FileName.SINGLE_SOURCE_QUALIFIER, dataSet);

		check = new SingleSourceQualifiersCheck();
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() {
		DataSetHelper.clear();
		check.check(entry);
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoQualifiers() {
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_OtherQualifier() {
		source1.setSingleQualifier("qual1");
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_Single() {
		source1.setSingleQualifier("focus");
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_Double_SameSource() {
		source1.addQualifier("focus");
		source1.addQualifier("focus");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("SingleSourceQualifiersCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Double() {
		source1.setSingleQualifier("focus");
		source2.setSingleQualifier("focus");

		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("SingleSourceQualifiersCheck", Severity.ERROR));
	}

	@Test
	public void testCheck_Message() {
		source1.setSingleQualifier("focus");
		source2.setSingleQualifier("focus");

		ValidationResult result = check.check(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages(
                "SingleSourceQualifiersCheck", Severity.ERROR);
		assertEquals(
				"Qualifier focus cannot exist in more than one source feature.",
				messages.iterator().next().getMessage());
	}

}
