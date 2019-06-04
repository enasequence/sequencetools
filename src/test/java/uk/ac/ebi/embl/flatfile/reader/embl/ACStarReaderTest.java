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

import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.ACStarReader;

public class ACStarReaderTest extends EmblReaderTest {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testRead_Accession() throws IOException {
		initLineReader(
				"AC * _CNS12NF0"
		);
		ValidationResult result = (new ACStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("CNS12NF0", entry.getSubmitterAccession());
		assertNull(entry.getSubmitterWgsVersion());
	}	

	public void testRead_Accession2() throws IOException {
		initLineReader(
				"AC * _CNS12NF0.1"
		);
		ValidationResult result = (new ACStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("CNS12NF0.1", entry.getSubmitterAccession());
		assertNull(entry.getSubmitterWgsVersion());
	}	

	public void testRead_Accession3() throws IOException {
		initLineReader(
				"AC * CNS12NF0.1"
		);
		ValidationResult result = (new ACStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("CNS12NF0.1", entry.getSubmitterAccession());
		assertNull(entry.getSubmitterWgsVersion());
	}

	public void testRead_Accession4() throws IOException {
		initLineReader(
				"AC * CNS12NF0;"
		);
		ValidationResult result = (new ACStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("CNS12NF0", entry.getSubmitterAccession());
		assertNull(entry.getSubmitterWgsVersion());
	}
	
	public void testRead_WgsVersion() throws IOException {
		initLineReader(
				"AC * _CNS12NF0.1 5"
		);
		ValidationResult result = (new ACStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("CNS12NF0.1", entry.getSubmitterAccession());
		assertEquals(new Integer(5), entry.getSubmitterWgsVersion());
	}		

	public void testRead_WgsVersion2() throws IOException {
		initLineReader(
				"AC * _CNS12NF0 5"
		);
		ValidationResult result = (new ACStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("CNS12NF0", entry.getSubmitterAccession());
		assertEquals(new Integer(5), entry.getSubmitterWgsVersion());
	}		

	public void testRead_WgsVersion3() throws IOException {
		initLineReader(
				"AC * CNS12NF0 5"
		);
		ValidationResult result = (new ACStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("CNS12NF0", entry.getSubmitterAccession());
		assertEquals(new Integer(5), entry.getSubmitterWgsVersion());
	}

}
