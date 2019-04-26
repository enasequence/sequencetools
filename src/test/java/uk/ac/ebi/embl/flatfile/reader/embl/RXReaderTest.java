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

import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.RXReader;

public class RXReaderTest extends EmblReaderTest {

	public void testRead_XrefWithSecondaryAccession() throws IOException {
		initLineReader(
			"RX    database ; primary accession ; secondary  accession .\n" +
			"RX    database2 ; primary accession2 ; secondary  accession2 ."
		);
		Publication publication = lineReader.getCache().getPublication();
		ValidationResult result = (new RXReader(lineReader)).read(entry);
		lineReader.readLine();
		ValidationResult result2 = (new RXReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(0, result2.count(Severity.ERROR));
		assertEquals(2, publication.getXRefs().size());
		assertEquals(
				"database",
				publication.getXRefs().get(0).getDatabase());
		assertEquals(
				"primary accession",
				publication.getXRefs().get(0).getPrimaryAccession());
		assertEquals(
				"secondary accession",
				publication.getXRefs().get(0).getSecondaryAccession());
		assertEquals(
				"database2",
				publication.getXRefs().get(1).getDatabase());
		assertEquals(
				"primary accession2",
				publication.getXRefs().get(1).getPrimaryAccession());
		assertEquals(
				"secondary accession2",
				publication.getXRefs().get(1).getSecondaryAccession());
	}

	public void testRead_XrefWithoutSecondaryAccession() throws IOException {
		initLineReader(
			"RX    database ; primary accession "
		);
		Publication publication = lineReader.getCache().getPublication();
		ValidationResult result = (new RXReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(1, publication.getXRefs().size());
		assertEquals(
				"database",
				publication.getXRefs().get(0).getDatabase());
		assertEquals(
				"primary accession",
				publication.getXRefs().get(0).getPrimaryAccession());
		assertNull(
				publication.getXRefs().get(0).getSecondaryAccession());
	}


	public void testRead_XrefNoDatabase() throws IOException {
		initLineReader(
			"RX    ; primary accession ; secondary  accession ."
		);
		Publication publication = lineReader.getCache().getPublication();				
		ValidationResult result = (new RXReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
		assertEquals(0, publication.getXRefs().size());
	}

	public void testRead_XrefNoPrimaryAccession() throws IOException {
		initLineReader(
			"RX    database ;  ; secondary  accession ."
		);
		Publication publication = lineReader.getCache().getPublication();				
		ValidationResult result = (new RXReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
		assertEquals(0, publication.getXRefs().size());
	}

	public void testRead_DOI() throws IOException {
		initLineReader(
			"RX    DOI; 10.1007/s00284-007-9029-0."
		);
		Publication publication = lineReader.getCache().getPublication();				
		ValidationResult result = (new RXReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(1, publication.getXRefs().size());
		assertEquals(
				"DOI",
				publication.getXRefs().get(0).getDatabase());
		assertEquals(
				"10.1007/s00284-007-9029-0",
				publication.getXRefs().get(0).getPrimaryAccession());
		assertNull(
				publication.getXRefs().get(0).getSecondaryAccession());
	}
	
	public void testRead_Origin() throws IOException {
		initLineReader(
				"RX    database ; primary accession ; secondary  accession .\n" +
				"RX    database2 ; primary accession2 ; secondary  accession2 ."
		);
		Publication publication = lineReader.getCache().getPublication();
		ValidationResult result = (new RXReader(lineReader)).read(entry);
		lineReader.readLine();
		ValidationResult result2 = (new RXReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(0, result2.count(Severity.ERROR));
		assertEquals(2, publication.getXRefs().size());		
		FlatFileOrigin origin1 = (FlatFileOrigin)publication.getXRefs().get(0).getOrigin();
		FlatFileOrigin origin2 = (FlatFileOrigin)publication.getXRefs().get(1).getOrigin();
		assertEquals(1, origin1.getFirstLineNumber());
		assertEquals(1, origin1.getLastLineNumber());
		assertEquals(2, origin2.getFirstLineNumber());
		assertEquals(2, origin2.getLastLineNumber());
	}		
}
