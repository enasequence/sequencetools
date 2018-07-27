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

import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class CommentReaderTest extends GenbankReaderTest {

	public void testRead_ShortComment() throws IOException {
		initLineReader(
				"COMMENT     Sequence is homologous to positions 14855 to 15136 of the human\n"
		);
		ValidationResult result = (new CommentReader(lineReader)).read(entry);
		assertEquals(
				"Sequence is homologous to positions 14855 to 15136 of the human",
				entry.getComment().getText());
		assertEquals(0, result.count(Severity.ERROR));

        FlatFileOrigin origin = (FlatFileOrigin) entry.getComment().getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(1, origin.getLastLineNumber());
    }

	public void testRead_LongComment() throws IOException {
		initLineReader(
				"COMMENT     Sequence is homologous to positions 14855 to 15136 of the human\n" +
	            "            mitochondrial DNA sequence (J01415, V00662).\n" +
	            "            See also X54885-7, X54891-X54914."
		);
		ValidationResult result = (new CommentReader(lineReader)).read(entry);
		assertEquals(
				"Sequence is homologous to positions 14855 to 15136 of the human\n" +
	            "mitochondrial DNA sequence (J01415, V00662).\n" +
	            "See also X54885-7, X54891-X54914.",
				entry.getComment().getText());
		assertEquals(0, result.count(Severity.ERROR));

        FlatFileOrigin origin = (FlatFileOrigin) entry.getComment().getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());
    }

	public void testRead_TwoComments() throws IOException {
		initLineReader(
				"COMMENT     Sequence is homologous to positions 14855 to 15136 of the human\n"
		);
		ValidationResult result = (new CommentReader(lineReader)).read(entry);
		initLineReader(
				"COMMENT     mitochondrial DNA sequence (J01415, V00662).\n" +
	            "            See also X54885-7, X54891-X54914."
		);
		ValidationResult result2 = (new CommentReader(lineReader)).read(entry);		
		assertEquals(
				"Sequence is homologous to positions 14855 to 15136 of the human\n" +
				"\n" +
	            "mitochondrial DNA sequence (J01415, V00662).\n" +
	            "See also X54885-7, X54891-X54914.",
				entry.getComment().getText());
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(0, result2.count(Severity.ERROR));
    }
	
	public void testRead_EmptyLine() throws IOException {
		initLineReader(
			"COMMENT     "
		);
		ValidationResult result = (new CommentReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getComment().getText());
	}
}
