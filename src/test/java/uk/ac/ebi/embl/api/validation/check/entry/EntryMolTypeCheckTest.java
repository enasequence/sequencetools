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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntryMolTypeCheckTest {

	private Entry entry;
	private EntryMolTypeCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);

		EntryFactory entryFactory = new EntryFactory();

        entry = entryFactory.createEntry();

        Sequence sequence = new SequenceFactory().createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
        entry.setSequence(sequence);

        DataRow regexRow = new DataRow("mol_type","1","TRUE","genomic RNA,viral cRNA,other DNA,other RNA,pre-RNA,transcribed RNA,genomic DNA,unassigned RNA,unassigned DNA");

        GlobalDataSets.addTestDataSet(GlobalDataSetFile.FEATURE_REGEX_GROUPS, regexRow);
        check = new EntryMolTypeCheck();
    }

    @After
    public void tearDown() {
        GlobalDataSets.resetTestDataSets();
    }

    public void testCheck_NoSequence() {
        entry.setSequence(null);
        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());//dont make a fuss, other checks for that
    }

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_BadMolType() {
        entry.getSequence().setMoleculeType("david");
        ValidationResult validationResult = check.check(entry);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("EntryMolTypeCheck", Severity.ERROR));
    }

	@Test
	public void testCheck_Fine() {
        entry.getSequence().setMoleculeType("genomic DNA");
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.isValid());
    }

	@Test
	public void testCheck_Message() {
        entry.getSequence().setMoleculeType("david");
        ValidationResult validationResult = check.check(entry);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("EntryMolTypeCheck", Severity.ERROR));
        assertEquals("The mol_type value \"david\" is not permitted.",validationResult.getMessages().iterator().next().getMessage());
    }

}
