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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;

import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.DRReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;

public class DblinkReaderTest extends GenbankReaderTest {

	public void testRead_XrefWithSingleline() throws IOException {
		initLineReader(
			"DBLINK      BioProject: PRJNA28847\n"
		);
		ValidationResult result = (new DblinkReader(lineReader)).read(entry);
		lineReader.readLine();
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(1, entry.getXRefs().size());
		assertEquals(
				"BioProject",
				entry.getXRefs().get(0).getDatabase());
		assertEquals(
				"PRJNA28847",
				entry.getXRefs().get(0).getPrimaryAccession());
		}

	public void testRead_XrefWithMultipleLines() throws IOException {
		initLineReader(
				"DBLINK      BioProject: PRJNA28847\n"
			  + "            BioSample: SAMN02436234\n"
		);
		ValidationResult result = (new DblinkReader(lineReader)).read(entry);
		lineReader.readLine();
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(2, entry.getXRefs().size());
		assertEquals(
				"BioProject",
				entry.getXRefs().get(0).getDatabase());
		assertEquals(
				"PRJNA28847",
				entry.getXRefs().get(0).getPrimaryAccession());
		assertEquals(
				"BioSample",
				entry.getXRefs().get(1).getDatabase());
		assertEquals(
				"SAMN02436234",
				entry.getXRefs().get(1).getPrimaryAccession());

	}


	public void testRead_XrefNoPrimaryAccession() throws IOException {
		initLineReader(
				"DBLINK      BioProject: \n"
		);
		ValidationResult result = (new DblinkReader(lineReader)).read(entry);
		assertEquals(1, result.count("FF.1", Severity.ERROR));
		assertEquals(0, entry.getXRefs().size());
	}
	
}
