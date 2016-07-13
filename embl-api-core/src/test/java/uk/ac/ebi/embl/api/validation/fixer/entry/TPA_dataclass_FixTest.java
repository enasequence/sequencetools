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
import uk.ac.ebi.embl.api.validation.*;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TPA_dataclass_FixTest {
	private EntryFactory entryFactory;
	private TPA_dataclass_Fix check;

	@Before
	public void setUp() throws Exception {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		entryFactory = new EntryFactory();
		check = new TPA_dataclass_Fix();
	}

	@Test
	public void testCheck_NoEntry() {
		ValidationResult result = check.check(null);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_NoDE() {
		Entry entry = entryFactory.createEntry();
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_DEwithoutTPAKeywords() {
		Entry entry = entryFactory.createEntry();
		entry.setDescription(new Text(
				" Sequence 1 from Patent sdf EP20080846682."));
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_DEwithTPAKeywords() {
		Entry entry = entryFactory.createEntry();
		entry.setDescription(new Text(
				"TPA_reasm: Sequence of 16S-23S rRNA spacer region (ITS1) of strain 439"));
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(1, result.getMessages().size());

		Collection<ValidationMessage<Origin>> messages = result.getMessages(
				"TPA_dataclass_Fix", Severity.FIX);

		assertEquals(
				"TPA Description line changed to \"TPA: Sequence of 16S-23S rRNA spacer region (ITS1) of strain 439\"",
				messages.iterator().next().getMessage());

	}

	@Test
	public void testCheck_DEwithValidDescription() {
		Entry entry = entryFactory.createEntry();
		entry.setDescription(new Text(
				" TPA: Sequence of 16S-23S rRNA spacer region (ITS1) of strain 439"));
		ValidationResult result = check.check(entry);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

}
