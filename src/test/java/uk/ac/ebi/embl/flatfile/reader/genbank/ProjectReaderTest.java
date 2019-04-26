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
import uk.ac.ebi.embl.flatfile.reader.genbank.ProjectReader;

public class ProjectReaderTest extends GenbankReaderTest {

	public void testRead_OneProject() throws IOException {
		initLineReader(
				"VERSION     GenomeProJECT:100001 "
		);
		ValidationResult result = (new ProjectReader(lineReader)).read(entry);
		assertEquals(1, entry.getProjectAccessions().size());		
		assertEquals(
				"100001",
				entry.getProjectAccessions().get(0).getText());
        FlatFileOrigin origin = (FlatFileOrigin) entry.getProjectAccessions().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(1, origin.getLastLineNumber());

        assertEquals(0, result.count(Severity.ERROR));

        entry.removeProjectAccessions();
		initLineReader(
				"VERSION     100001 "
			);
		result = (new ProjectReader(lineReader)).read(entry);
		assertEquals(1, entry.getProjectAccessions().size());
		assertEquals(
				"100001",
				entry.getProjectAccessions().get(0).getText());
		assertEquals(0, result.count(Severity.ERROR));
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(1, origin.getLastLineNumber());

        entry.removeProjectAccessions();
		initLineReader(
				"VERSION     GenomeProject:100001 "
			);
		result = (new ProjectReader(lineReader)).read(entry);
		assertEquals(1, entry.getProjectAccessions().size());
		assertEquals(
				"100001",
				entry.getProjectAccessions().get(0).getText());
		assertEquals(0, result.count(Severity.ERROR));
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(1, origin.getLastLineNumber());
	}
	
	
	public void testRead_ManyProjects() throws IOException {
		initLineReader(
			"VERSION     GenomeProject:100001  100002 100003 \n" +
			"            GenomeProject:100004 GenomeProject:100005 100006 \n" +
			"            GenomeProject:100007\n"
		);
		ValidationResult result = (new ProjectReader(lineReader)).read(entry);
		assertEquals(7, entry.getProjectAccessions().size());
		assertEquals(
				"100001",
				entry.getProjectAccessions().get(0).getText());
        FlatFileOrigin origin = (FlatFileOrigin) entry.getProjectAccessions().get(0).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

        assertEquals(
				"100002",
				entry.getProjectAccessions().get(1).getText());
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(1).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

		assertEquals(
				"100003",
				entry.getProjectAccessions().get(2).getText());
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(2).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

		assertEquals(
				"100004",
				entry.getProjectAccessions().get(3).getText());
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(3).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

		assertEquals(
				"100005",
				entry.getProjectAccessions().get(4).getText());
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(4).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

		assertEquals(
				"100006",
				entry.getProjectAccessions().get(5).getText());
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(5).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

		assertEquals(
				"100007",
				entry.getProjectAccessions().get(6).getText());
        origin = (FlatFileOrigin) entry.getProjectAccessions().get(6).getOrigin();
        assertEquals(1, origin.getFirstLineNumber());
        assertEquals(3, origin.getLastLineNumber());

		assertEquals(0, result.count(Severity.ERROR));
	}
}
