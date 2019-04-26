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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataclassFixTest
{

	private Entry entry;
	private DataclassFix check;
	public EntryFactory entryFactory;
	public FeatureFactory featureFactory;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

		entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();

        DataRow estDataRow=new DataRow("EST","EST","EST");
        DataRow wgsDataRow=new DataRow("WGS","WGS","WGS");

        DataSetHelper.createAndAdd(FileName.KEYWORD_DATACLASS, estDataRow,wgsDataRow);
		check = new DataclassFix();
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_EmptyKeywordandDataclass() throws ValidationEngineException
	{
		ValidationResult result = check.check(entry);
		assertTrue(result.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_NoDataclasswithKeywords() throws ValidationEngineException
	{
		entry.addKeyword(new Text(Entry.WGS_DATACLASS));
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DataclassFix_1", Severity.FIX));
	}

	@Test
	public void testCheck_dataclasswithNoKeywords() throws ValidationEngineException
	{
		entry.setDataClass(Entry.WGS_DATACLASS);;
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DataclassFix_2", Severity.FIX));	
		
	}

	@Test
	public void testCheck_multipledataclassKeywords() throws ValidationEngineException
	{
		entry.addKeyword(new Text(Entry.WGS_DATACLASS));
		entry.addKeyword(new Text(Entry.EST_DATACLASS));
		ValidationResult result=check.check(entry);
		assertTrue(result.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_noDataclasskeyword() throws ValidationEngineException
	{
		entry.addKeyword(new Text("dsfg"));
		ValidationResult result=check.check(entry);
		assertTrue(result.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_nonKeywordDataclass() throws ValidationEngineException
	{
		entry.setDataClass(Entry.PRT_DATACLASS);
		ValidationResult result=check.check(entry);
		assertTrue(result.getMessages(Severity.FIX).isEmpty());
	}
	
	@Test
	public void testCheck_dataclasswithinvalidKeyword() throws ValidationEngineException
	{
		entry.setDataClass(Entry.WGS_DATACLASS);
		entry.addKeyword(new Text(Entry.TSA_DATACLASS));
		ValidationResult result=check.check(entry);
		assertTrue(!result.getMessages(Severity.FIX).isEmpty());
		assertEquals(1, result.count("DataclassFix_2", Severity.FIX));	
	}
	

}
