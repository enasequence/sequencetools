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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.IOException;

import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.ACReader;

public class ACReaderTest extends EmblReaderTest {

	public void testRead_PrimaryAccession() throws IOException {
		initLineReader(
			"AC     A00001;"
		);
		ValidationResult result = (new ACReader(lineReader)).read(entry);
		assertEquals(
				"A00001",
				entry.getPrimaryAccession());
		assertEquals(0, result.count(Severity.ERROR));
		initLineReader(
			"AC     A00001"
		);
		result = (new ACReader(lineReader)).read(entry);
		assertEquals(
				"A00001",
				entry.getPrimaryAccession());
		assertEquals(0, result.count(Severity.ERROR));
	}

	public void testRead_NoPrimaryAccession() throws IOException {
		initLineReader(
			"AC     ;"
		);
		ValidationResult result = (new ACReader(lineReader)).read(entry);
		assertNull(entry.getPrimaryAccession());
		assertEquals(0, result.count(Severity.ERROR));
		initLineReader(
				"AC     ; A00002"
		);
		result = (new ACReader(lineReader)).read(entry);
		assertNull(entry.getPrimaryAccession());
		assertEquals(1, entry.getSecondaryAccessions().size());
		assertEquals(
				"A00002",
				entry.getSecondaryAccessions().get(0).getText());
		assertEquals(0, result.count(Severity.ERROR));

        FlatFileOrigin origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(1, origin.getLastLineNumber());

    }
	
	public void testRead_SecondaryAccessionWithPrimaryAccession() throws IOException {
		initLineReader(
			"AC     A00001; \n" +
			"AC     A00002; A00003; \n"  +
			"AC     A00004; A00005;"
		);
		ValidationResult result = (new ACReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				"A00001",
				entry.getPrimaryAccession());

		assertEquals(4, entry.getSecondaryAccessions().size());
		assertEquals("A00002", entry.getSecondaryAccessions().get(0).getText());
		assertEquals("A00003", entry.getSecondaryAccessions().get(1).getText());
		assertEquals("A00004", entry.getSecondaryAccessions().get(2).getText());
		assertEquals("A00005", entry.getSecondaryAccessions().get(3).getText());
        FlatFileOrigin origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());


    }

	public void testRead_SecondaryAccessionWithoutPrimaryAccession() throws IOException {
		initLineReader(
			"AC     ; \n" +
			"XX\n" +
			"AC     A00002; A00003; \n"  +
			"\n" +
			"AC     A00004; A00005;"
		);
		ValidationResult result = (new ACReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getPrimaryAccession());
		assertEquals(4, entry.getSecondaryAccessions().size());
		assertEquals("A00002", entry.getSecondaryAccessions().get(0).getText());
		assertEquals("A00003", entry.getSecondaryAccessions().get(1).getText());
		assertEquals("A00004", entry.getSecondaryAccessions().get(2).getText());
		assertEquals("A00005", entry.getSecondaryAccessions().get(3).getText());
        FlatFileOrigin origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(5, origin.getLastLineNumber());
    }
	
	public void testRead_XXX() throws IOException {
		initLineReader(
			"AC     XXX;"
		);
		ValidationResult result = (new ACReader(lineReader)).read(entry);
		assertNull(entry.getPrimaryAccession());
		assertEquals(0, entry.getSecondaryAccessions().size());
		assertEquals(0, result.count(Severity.ERROR));
	}	
}
