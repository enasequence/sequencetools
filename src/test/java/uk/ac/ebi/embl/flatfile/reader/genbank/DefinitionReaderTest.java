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

public class DefinitionReaderTest extends GenbankReaderTest {

	public void testRead_ShortDescription() throws IOException {
		initLineReader(
			"DEFINITION  description   description description "
		);
		ValidationResult result = (new DefinitionReader(lineReader)).read(entry);
		assertEquals(
				"description description description",
				entry.getDescription().getText());
		assertEquals(0, result.count(Severity.ERROR));

        FlatFileOrigin origin = (FlatFileOrigin) entry.getDescription().getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(1, origin.getLastLineNumber());

    }

	public void testRead_LongDescription() throws IOException {
		initLineReader(
			"DEFINITION  description description description \n" +
			"            description    description   \n" +
			"               description description\n" +
			"            description description description description description description description description description description description description \n"
		);
		ValidationResult result = (new DefinitionReader(lineReader)).read(entry);
		assertEquals(
				"description description description description description " +
				"description description description description description " +
				"description description description description description " +
				"description description description description",
				entry.getDescription().getText());
		assertEquals(0, result.count(Severity.ERROR));

        FlatFileOrigin origin = (FlatFileOrigin) entry.getDescription().getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(4, origin.getLastLineNumber());
	}
		
	public void testRead_EmptyLine() throws IOException {
		initLineReader(
			"DEFINITION"
		);
		ValidationResult result = (new DefinitionReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getDescription().getText());
		initLineReader(
			"DEFINITION    "
		);
		result = (new DefinitionReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getDescription().getText());
	}	
}
