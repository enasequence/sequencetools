/*******************************************************************************
 * Copyright 2012-13 EMBL-EBI, Hinxton outstation
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

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class ReferenceCheckTest
{

	private Entry entry;
	private ReferenceCheck check;
	private Reference reference1, reference2;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		ReferenceFactory referenceFactory = new ReferenceFactory();
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		reference1 = referenceFactory.createReference();
		reference2 = referenceFactory.createReference();
		Submission submission = (new ReferenceFactory()).createSubmission(referenceFactory.createPublication());
		Unpublished unpub = (new ReferenceFactory()).createUnpublished();
		reference1.setPublication(submission);
		reference2.setPublication(unpub);
		entry = entryFactory.createEntry();
		check = new ReferenceCheck();
        check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoReferences()
	{
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("ReferenceCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_NoSubmission()
	{
		entry.addReference(reference2);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("ReferenceCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_Submission()
	{
		entry.addReference(reference1);
		entry.addReference(reference2);
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_validDataclass()
	{
		entry.setDataClass("STD");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("DataclassCheck1", Severity.ERROR));
	}

}
