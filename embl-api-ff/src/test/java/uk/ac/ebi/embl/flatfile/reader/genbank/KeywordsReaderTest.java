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
import uk.ac.ebi.embl.flatfile.reader.genbank.KeywordsReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;

public class KeywordsReaderTest extends GenbankReaderTest {

	public void testRead_OneKeyword() throws IOException {

		initLineReader(
				"KEYWORDS      keyword 1 "
		);
		ValidationResult result = (new KeywordsReader(lineReader)).read(entry);
		assertEquals(1, entry.getKeywords().size());		
		assertEquals(
				"keyword 1",
				entry.getKeywords().get(0).getText());
		assertEquals(0, result.count(Severity.ERROR));
		entry.removeKeywords();

		initLineReader(
				"KEYWORDS      keyword 1; "
			);
		result = (new KeywordsReader(lineReader)).read(entry);
		assertEquals(1, entry.getKeywords().size());
		assertEquals(
				"keyword 1",
				entry.getKeywords().get(0).getText());
		assertEquals(0, result.count(Severity.ERROR));
		entry.removeKeywords();

		initLineReader(
				"KEYWORDS      keyword 1;; "
			);
		result = (new KeywordsReader(lineReader)).read(entry);
		assertEquals(1, entry.getKeywords().size());
		assertEquals(
				"keyword 1",
				entry.getKeywords().get(0).getText());
		assertEquals(0, result.count(Severity.ERROR));
		entry.removeKeywords();

		initLineReader(
				"KEYWORDS    ;;;; ; ;  keyword 1;; ; ; ; ;; "
			);
		result = (new KeywordsReader(lineReader)).read(entry);
		assertEquals(1, entry.getKeywords().size());
		assertEquals(
				"keyword 1",
				entry.getKeywords().get(0).getText());
		assertEquals(0, result.count(Severity.ERROR));

        FlatFileOrigin origin = (FlatFileOrigin)entry.getKeywords().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(1, origin.getLastLineNumber());

    }
	
	
	public void testRead_ManyKeywords() throws IOException {
		initLineReader(
			"KEYWORDS      keyword 1;  keyword 2 ; keyword 3;\n" +
			"KEYWORDS      keyword 4; keyword 5 ; keyword 6; \n" +
			"KEYWORDS      keyword 7\n"
		);
		ValidationResult result = (new KeywordsReader(lineReader)).read(entry);
		assertEquals(7, entry.getKeywords().size());

        int keyworrdNo = 0;
        assertEquals(
				"keyword 1",
				entry.getKeywords().get(keyworrdNo).getText());

        FlatFileOrigin origin = (FlatFileOrigin)entry.getKeywords().get(keyworrdNo).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

        keyworrdNo = 1;
        assertEquals(
				"keyword 2",
				entry.getKeywords().get(keyworrdNo).getText());

        origin = (FlatFileOrigin)entry.getKeywords().get(keyworrdNo).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

        keyworrdNo = 2;
        assertEquals(
				"keyword 3",
				entry.getKeywords().get(keyworrdNo).getText());

        origin = (FlatFileOrigin)entry.getKeywords().get(keyworrdNo).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

        keyworrdNo = 3;
		assertEquals(
				"keyword 4",
				entry.getKeywords().get(keyworrdNo).getText());

        origin = (FlatFileOrigin)entry.getKeywords().get(keyworrdNo).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

        keyworrdNo = 4;
		assertEquals(
				"keyword 5",
				entry.getKeywords().get(keyworrdNo).getText());

        origin = (FlatFileOrigin)entry.getKeywords().get(keyworrdNo).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

        keyworrdNo = 5;
		assertEquals(
				"keyword 6",
				entry.getKeywords().get(keyworrdNo).getText());

        origin = (FlatFileOrigin)entry.getKeywords().get(keyworrdNo).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

        keyworrdNo = 6;
		assertEquals(
				"keyword 7",
				entry.getKeywords().get(keyworrdNo).getText());

        origin = (FlatFileOrigin)entry.getKeywords().get(keyworrdNo).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

		assertEquals(0, result.count(Severity.ERROR));
	}
	
	public void testRead_EmptyKeywords() throws IOException {
		initLineReader(
			"KEYWORDS      .\n"
		);
		ValidationResult result = (new KeywordsReader(lineReader)).read(entry);
		assertEquals(0, entry.getKeywords().size());
		assertEquals(0, result.count(Severity.ERROR));
	}	

	public void testRead_EmptyKeywords2() throws IOException {
		initLineReader(
			"KEYWORDS      \n"
		);
		ValidationResult result = (new KeywordsReader(lineReader)).read(entry);
		assertEquals(0, entry.getKeywords().size());
		assertEquals(0, result.count(Severity.ERROR));
	}
}
