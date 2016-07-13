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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.embl.STStarReader;

public class STStarReaderTest extends EmblReaderTest {
	
	public void testRead_Public() throws IOException {
		initLineReader(
				"ST * public\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				Entry.Status.PUBLIC,
				entry.getStatus());
	}

	public void testRead_Cancelled() throws IOException {
		initLineReader(
				"ST * cancelled\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				Entry.Status.CANCELLED,
				entry.getStatus());
	}

	public void testRead_Draft() throws IOException {
		initLineReader(
				"ST * draft\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				Entry.Status.DRAFT,
				entry.getStatus());
	}

	public void testRead_Killed() throws IOException {
		initLineReader(
				"ST * killed\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				Entry.Status.KILLED,
				entry.getStatus());
	}

	public void testRead_Private() throws IOException {
		initLineReader(
				"ST * private 10-SEP-1998\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				Entry.Status.PRIVATE,
				entry.getStatus());
		assertEquals(
				FlatFileUtils.getDay("10-SEP-1998"),
				entry.getHoldDate());		
	}

	public void testRead_Suppressed() throws IOException {
		initLineReader(
				"ST * suppressed\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				Entry.Status.SUPPRESSED,
				entry.getStatus());
	}	

	public void testRead_InvalidStatus() throws IOException {
		initLineReader(
				"ST * ddsuppressed\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
		assertEquals(1, result.count("ST.1", Severity.ERROR));
		assertNull(
				entry.getStatus());
	}

	public void testRead_InvalidHoldDate() throws IOException {
		initLineReader(
				"ST * suppressed graa\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
		assertEquals(1, result.count("FF.2", Severity.ERROR));
		assertEquals(
				Entry.Status.SUPPRESSED,
				entry.getStatus());
		assertNull(
				entry.getHoldDate());
	}
	
	public void testRead_XXX() throws IOException {
		initLineReader(
				"ST * private XXX\n"
		);
		ValidationResult result = (new STStarReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				Entry.Status.PRIVATE,
				entry.getStatus());
		assertNull(entry.getHoldDate());		
	}	
}
