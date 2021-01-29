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
 ******************************************************************************//*

package uk.ac.ebi.embl.api.validation.check.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class AsciiCharacterCheckTest
{

	private Entry entry;
	private AsciiCharacterCheck check;
	private Reference reference1;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		ReferenceFactory referenceFactory = new ReferenceFactory();
		EmblEntryValidationPlanProperty property=new EmblEntryValidationPlanProperty();
		reference1 = referenceFactory.createReference();
		Submission submission = (new ReferenceFactory()).createSubmission(referenceFactory.createPublication());
		reference1.setPublication(submission);
		entry = entryFactory.createEntry();
		check = new AsciiCharacterCheck();
        check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoContent()
	{
		ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
    }

	@Test
	public void testCheck_invalidComment()
	{
		entry.setComment(new Text("�-ketothiolase�-ketothiolase�-ketothiolase�-ketothiolase"));
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("AsciiCharacterCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_invalidDescription()
	{
		
		entry.setDescription(new Text("�-ketothiolase�-ketothiolase�-ketothiolase�-ketothiolase"));
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("AsciiCharacterCheck_1", Severity.ERROR));
	}

	@Test
	public void testCheck_invalidQualifier()
	{
		Feature feature = (new FeatureFactory()).createFeature(Feature.CDS_FEATURE_NAME);
		feature.addQualifier((new QualifierFactory()).createQualifier(Qualifier.NOTE_QUALIFIER_NAME,"�-ketothiolase�-ketothiolase�-ketothiolase�"));
		entry.addFeature(feature);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("AsciiCharacterCheck_1", Severity.ERROR));
	}
	
	@Test
	public void testCheck_invalidReference()
	{
        reference1.getPublication().setTitle("�-ketothiolase�");
		entry.addReference(reference1);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("AsciiCharacterCheck_1", Severity.ERROR));
	}
	
	@Test
	public void testCheck_validDescription()
	{
        reference1.getPublication().setTitle("�-ketothiolase�");
		entry.addReference(reference1);
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("AsciiCharacterCheck_1", Severity.ERROR));
	}

}
*/
