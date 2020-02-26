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
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccessionFixTest {

	private Entry entry;
	private AccessionFix check;
	public EntryFactory entryFactory;
	
	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		check = new AccessionFix();
	}

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoEntryName() {
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_validEntryName() {
		entry.setSubmitterAccession("entryname");
		ValidationResult validationResult = check.check(entry);
		assertEquals(0, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_inValidEntryName() {
		entry.setSubmitterAccession("_entryname;");
		ValidationResult validationResult = check.check(entry);
		assertEquals("entryname", entry.getSubmitterAccession());
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
	}

	@Test
	public void testCheck_inValidEntryName1() {
		entry.setSubmitterAccession("entryname;");
		ValidationResult validationResult = check.check(entry);
		assertEquals(1, validationResult.getMessages(Severity.FIX).size());
	}

}
