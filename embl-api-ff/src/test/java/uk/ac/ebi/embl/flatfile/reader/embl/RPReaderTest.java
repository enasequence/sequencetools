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

import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.RPReader;

public class RPReaderTest extends EmblReaderTest {
	
	public void testRead_ReferenceNumber() throws IOException {
		initLineReader(
				"RP   1-34"
		);
		ValidationResult result = (new RPReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(lineReader.getCache().getReference());		
		assertEquals(1, lineReader.getCache().getReference().getLocations().
				getLocations().size());
		LocalRange location1 = lineReader.getCache().getReference().getLocations().
			getLocations().get(0);
		assertEquals(new Long(1), location1.getBeginPosition());
		assertEquals(new Long(34), location1.getEndPosition());
	}

	public void testRead_ReferenceNumber2() throws IOException {
		initLineReader(
				"RP   1-34,\n" +
				"RP   34-44"
		);
		ValidationResult result = (new RPReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(lineReader.getCache().getReference());		
		assertEquals(2, lineReader.getCache().getReference().getLocations().
				getLocations().size());
		LocalRange location1 = lineReader.getCache().getReference().getLocations().
			getLocations().get(0);
		LocalRange location2 = lineReader.getCache().getReference().getLocations().
			getLocations().get(1);
		assertEquals(new Long(1), location1.getBeginPosition());
		assertEquals(new Long(34), location1.getEndPosition());
		assertEquals(new Long(34), location2.getBeginPosition());
		assertEquals(new Long(44), location2.getEndPosition());
	}

	public void testRead_Origin() throws IOException {
		initLineReader(
				"RP   1-34,\n" +
				"RP   34-44"
		);
		ValidationResult result = (new RPReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(lineReader.getCache().getReference());		
		FlatFileOrigin origin = (FlatFileOrigin)lineReader.getCache().
			getReference().getLocations().getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(2, origin.getLastLineNumber());
	}	
	
}
