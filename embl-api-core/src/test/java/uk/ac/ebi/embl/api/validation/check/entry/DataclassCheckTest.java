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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;

public class DataclassCheckTest {

	private Entry entry;
	private FeatureFactory featureFactory;
	private DataclassCheck check;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		DataSet dataclassDataSet = new DataSet();
		DataSet keyworddataclassDataSet = new DataSet();
		dataclassDataSet.addRow(new DataRow("STD"));
		dataclassDataSet.addRow(new DataRow("XXX"));
		dataclassDataSet.addRow(new DataRow("WGS"));
		keyworddataclassDataSet.addRow(new DataRow("WGS","WGS","WGS"));
		keyworddataclassDataSet.addRow(new DataRow("CON","CON","CON"));
		check = new DataclassCheck(dataclassDataSet,keyworddataclassDataSet);
	}

	@Test(expected = NullPointerException.class)
	public void testCheck_NoDataSet() throws ValidationEngineException {
		check = new DataclassCheck();
		entry.setDataClass("STD");
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoDataclass() throws ValidationEngineException {
		entry.setDataClass("XXX");
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_invalidDataclass() throws ValidationEngineException {
		entry.setDataClass("qsd");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("DataclassCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_validDataclass() throws ValidationEngineException {
		entry.setDataClass("STD");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("DataclassCheck1", Severity.ERROR));
	}

	@Test
	public void testCheck_validMasterDatclass() throws ValidationEngineException {
		entry.addMasterWgsAccession(new Text("AAAF01000000-AAAF01000015"));
		entry.setDataClass("SET");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("DataclassCheck2", Severity.ERROR));
	}

	@Test
	public void testCheck_validMasterDatclass2() throws ValidationEngineException {
		entry.setDataClass("SET");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("DataclassCheck2", Severity.ERROR));
	}
	
	@Test
	public void testCheck_invalidKeywordDatclass() throws ValidationEngineException {
		entry.addKeyword(new Text("CON"));
		entry.setDataClass("WGS");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("DataclassCheck3", Severity.ERROR));
	}

	@Test
	public void testCheck_invalidAccessionDatclass() throws ValidationEngineException {
		entry.setPrimaryAccession("ABCD01000001");
		entry.setDataClass("STD");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("DataclassCheck4", Severity.ERROR));
	}
	@Test
	public void testCheck_validAccessionDatclass() throws ValidationEngineException {
		entry.setPrimaryAccession("ABCD01000001");
		entry.setDataClass("WGS");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("DataclassCheck4", Severity.ERROR));
	}
}
