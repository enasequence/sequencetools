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
package uk.ac.ebi.embl.api.validation.check.gff3;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.gff3.GFF3Record;
import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryMolTypeCheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StartEndCheckTest {

	private GFF3RecordSet entry;
	private StartEndCheck check;

	@Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.GFF3_VALIDATION_BUNDLE);

        entry = new GFF3RecordSet();

        check = new StartEndCheck();
    }

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_BadLocations() {
        entry.getRecords().clear();
        GFF3Record record = new GFF3Record();
        record.setStart(10);
        record.setEnd(1);
        entry.addRecord(record);
        ValidationResult validationResult = check.check(entry);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("StartEndCheck", Severity.ERROR));
    }

	@Test
	public void testCheck_Fine() {
        entry.getRecords().clear();
        GFF3Record record = new GFF3Record();
        record.setStart(1);
        record.setEnd(10);
        entry.addRecord(record);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.isValid());
    }

}
