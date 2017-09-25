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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnnotationOnlySequenceFixTest
{

	private AnnotationOnlySequenceFix check;
	private EntryDAOUtils entryDAOUtils;
	private EntryFactory entryFactory;
	private Entry entry;
	SequenceFactory sequenceFactory;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryFactory = new EntryFactory();
		sequenceFactory=new SequenceFactory();
		entry = entryFactory.createEntry();
		entryDAOUtils=createMock(EntryDAOUtils.class);
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		property.analysis_id.set("ERZ0001");
		check = new AnnotationOnlySequenceFix();
		check.setEmblEntryValidationPlanProperty(property);
   }

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoAnalysisIDAndObjectName() throws ValidationEngineException
	{
		assertTrue(check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_EntrywithnoSequence() throws SQLException, ValidationEngineException, IOException
	{
		entry.setSequence(new SequenceFactory().createSequence());
		entry.setSubmitterAccession("chr1");
		expect(entryDAOUtils.isAssemblyLevelExists(check.getEmblEntryValidationPlanProperty().analysis_id.get(),2)).andReturn(true);
		expect(entryDAOUtils.getPrimaryAcc(check.getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(), 2)).andReturn("A00001");
		expect(entryDAOUtils.getDataclass(check.getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(), 2)).andReturn(null);
		expect(entryDAOUtils.getSequence("A00001")).andReturn("gttttgtttgatggagaattgcgcagaggggttatatctgcgtgaggatctgt".getBytes());
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
      	ValidationResult result=check.check(entry);
        assertTrue(result.isValid());
        assertTrue(null==entry.getDataClass());
        assertTrue("gttttgtttgatggagaattgcgcagaggggttatatctgcgtgaggatctgt".equals(new String(entry.getSequence().getSequenceByte())));
        assertEquals(1,result.count("AnnotationOnlySequenceFix", Severity.FIX));
	}
	
	@Test
	public void testCheck_EntrywithnodatabaseRecord() throws SQLException, ValidationEngineException, IOException
	{
		entry.setSequence(sequenceFactory.createSequence());
		entry.setSubmitterAccession("chr1");
		expect(entryDAOUtils.isAssemblyLevelExists(check.getEmblEntryValidationPlanProperty().analysis_id.get(),2)).andReturn(true);
		expect(entryDAOUtils.getPrimaryAcc(check.getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(), 2)).andReturn(null);
		expect(entryDAOUtils.getDataclass(check.getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(), 2)).andReturn(null);
		expect(entryDAOUtils.getSequence("A0001")).andReturn(null);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
       	ValidationResult result=check.check(entry);
       	assertTrue(result.isValid());
     
	}
	
}
