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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class SourceReaderTest extends GenbankReaderTest {

	Pattern htmlEntityRegexPattern = Pattern.compile("&(?:\\#(?:([0-9]+)|[Xx]([0-9A-Fa-f]+))|([A-Za-z0-9]+));?");
	public void testReadWithoutCommonName() throws IOException {

		String blockString = "Feline panleukopenia virus gene for &quot; structural protein 1, complete cds, isolate: 483.";
		//Matcher m = htmlEntityRegexPattern.matcher(blockStringwithNohtmlEntity);


		String blockStringwithNohtmlEntity= StringEscapeUtils.unescapeHtml4(blockString);
		System.out.println(blockStringwithNohtmlEntity);
		/*initLineReader(
			"SOURCE      dsfdsfjsdlkfslkdfjdsl\n"
		);
		ValidationResult result = (new SourceReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));*/
    }

	
	public void testRead_EmptyLine() throws IOException {
		initLineReader(
				"SOURCE\n"
			);
			ValidationResult result = (new SourceReader(lineReader)).read(entry);
			assertEquals(0, result.count(Severity.ERROR));
	}	
}
