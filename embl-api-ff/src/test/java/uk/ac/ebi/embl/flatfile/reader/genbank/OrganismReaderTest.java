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

public class OrganismReaderTest extends GenbankReaderTest {

	public void testReadWithoutCommonName() throws IOException {
		initLineReader(
				"  ORGANISM  Saccharomyces cerevisiae\n" + 
				"            Eukaryota; Fungi; Ascomycota; Saccharomycotina; Saccharomycetes;\n" + 
				"            Saccharomycetales; Saccharomycetaceae; Saccharomyces.\n" 
		);
		OrganismReader reader = new OrganismReader(lineReader);
		ValidationResult result = (reader.read(entry));
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				"Eukaryota; Fungi; Ascomycota; Saccharomycotina; Saccharomycetes; " + 
				"Saccharomycetales; Saccharomycetaceae; Saccharomyces",  
				reader.getCache().getLineage("Saccharomyces cerevisiae"));
    }
	
	public void testReadWithCommonName() throws IOException {
		initLineReader(
				"  ORGANISM  Influenza A virus (A/equine/Ibadan/6/91(H3N8))\n" + 
				"            Eukaryota; Fungi; Ascomycota; Saccharomycotina; Saccharomycetes;\n" + 
				"            Saccharomycetales; Saccharomycetaceae; Saccharomyces.\n" 
		);
		OrganismReader reader = new OrganismReader(lineReader);
		ValidationResult result = (reader.read(entry));
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(
				"A/equine/Ibadan/6/91(H3N8)",
				reader.getCache().getCommonName("Influenza A virus"));
		assertEquals(
				"Eukaryota; Fungi; Ascomycota; Saccharomycotina; Saccharomycetes; " + 
				"Saccharomycetales; Saccharomycetaceae; Saccharomyces",  
				reader.getCache().getLineage("Influenza A virus"));
    }	
}
