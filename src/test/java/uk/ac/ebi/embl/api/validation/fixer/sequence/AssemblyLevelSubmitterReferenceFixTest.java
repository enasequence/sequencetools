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
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.fixer.entry.AssemblyLevelSubmitterReferenceFix;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssemblyLevelSubmitterReferenceFixTest
{

	private AssemblyLevelSubmitterReferenceFix check;
	private EraproDAOUtils eraproDAOUtils;
	private EntryFactory entryFactory;
	private Entry entry;
	Reference reference=null;

	@Before
	public void setUp() throws SQLException, UnsupportedEncodingException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryFactory = new EntryFactory();
		ReferenceFactory referenceFactory= new ReferenceFactory();
		Publication publication=new Publication();
		
		reference=referenceFactory.createReference();
		Person person =null;
		person = referenceFactory.createPerson(
				EntryUtils.concat(" ", EntryUtils.convertNonAsciiStringtoAsciiString("surname"),  EntryUtils.convertNonAsciiStringtoAsciiString("middle_initials")),
				EntryUtils.convertNonAsciiStringtoAsciiString("first_name"));
		
		publication.addAuthor(person);
		Submission submission = referenceFactory.createSubmission(publication);				
		submission.setSubmitterAddress(EntryUtils.concat(", ", EntryUtils.convertNonAsciiStringtoAsciiString("center_name"),
				EntryUtils.convertNonAsciiStringtoAsciiString("laboratory_name"), EntryUtils.convertNonAsciiStringtoAsciiString("address"), EntryUtils.convertNonAsciiStringtoAsciiString("country")));
		Date date = new Date();
		submission.setDay(date);
		publication = submission;
		reference.setPublication(publication);
		reference.setReferenceNumber(1);
		
		entry = entryFactory.createEntry();
		Sequence sequence=new SequenceFactory().createSequence();
		sequence.setLength(12);
		entry.setSequence(sequence);
		eraproDAOUtils=createMock(EraproDAOUtils.class);
		Connection con=createMock(Connection.class);
		
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		property.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		property.analysis_id.set("ERZ0001");
		property.eraproConnection.set(con);
		check = new AssemblyLevelSubmitterReferenceFix();
		check.setEmblEntryValidationPlanProperty(property);
   }

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_EntrywithwitReference() throws SQLException, ValidationEngineException, IOException
	{
		expect(eraproDAOUtils.getSubmitterReference(check.getEmblEntryValidationPlanProperty().analysis_id.get())).andReturn(reference);
		replay(eraproDAOUtils);
		check.setEraproDAOUtils(eraproDAOUtils);
		ValidationResult result=check.check(entry);
        assertTrue(result.isValid());
        assertTrue(reference.equals(entry.getReferences().get(0)));
        assertEquals(1,result.count("AssemblyLevelSubmitterReferenceFix_1", Severity.FIX));
	}
	
	@Test
	public void testCheck_EntrywithnoSubmitterReference() throws SQLException, ValidationEngineException, IOException
	{
		expect(eraproDAOUtils.getSubmitterReference(check.getEmblEntryValidationPlanProperty().analysis_id.get())).andReturn(null);
		replay(eraproDAOUtils);
		ValidationResult result=check.check(entry);
		check.setEraproDAOUtils(eraproDAOUtils);
        assertTrue(result.isValid());
        assertEquals(0,entry.getReferences().size());
        assertEquals(0,result.count("AssemblyLevelSubmitterReferenceFix_1", Severity.FIX));
 	}
	
}
