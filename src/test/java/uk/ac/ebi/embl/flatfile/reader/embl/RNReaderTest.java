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
import uk.ac.ebi.embl.flatfile.reader.embl.RNReader;

public class RNReaderTest extends EmblReaderTest {
	
	public void testRead_ReferenceNumber() throws IOException {
		initLineReader(
				"RN   [  4 ]"
		);
		ValidationResult result = (new RNReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(lineReader.getCache().getReference());
		assertEquals(
				new Integer(4),
				lineReader.getCache().getReference().getReferenceNumber());
	}

	public void testRead_NoReferenceNumber() throws IOException {
		initLineReader(
				"RN    "
		);
		ValidationResult result = (new RNReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(
			lineReader.getCache().getReference().getReferenceNumber());
	}
		
	public void testRead_FormatError1() throws IOException {
		initLineReader(
				"RN   [ dfdfsd ]"
		);
		ValidationResult result = (new RNReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
		assertNull(
				lineReader.getCache().getReference().getReferenceNumber());
	}	
	
	public void testRead_FormatError2() throws IOException {
		initLineReader(
				"RN   34"

		);
		ValidationResult result = (new RNReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
	}	

	public void testRead_FormatError3() throws IOException {
		initLineReader(
				"RN   [34d]"

		);
		ValidationResult result = (new RNReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
	}	
	
	public void testRead_Origin() throws IOException {
		initLineReader(
				"RN   [  4 ]"
		);
		ValidationResult result = (new RNReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(lineReader.getCache().getReference());		
		FlatFileOrigin origin = (FlatFileOrigin)lineReader.getCache().
			getReference().getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(1, origin.getLastLineNumber());	
	}
}
