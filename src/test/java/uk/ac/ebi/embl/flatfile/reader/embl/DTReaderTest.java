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

import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;

public class DTReaderTest extends EmblReaderTest {

	public void testRead_FullFormat() throws IOException {
		initLineReader(
				"DT   28-JAN-1993 (Rel. 34, Created)\n" +
				"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)"
		);
		ValidationResult result = (new DTReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(FlatFileDateUtils.getDay("28-JAN-1993"), entry.getFirstPublic());
		assertEquals(new Integer(34), entry.getFirstPublicRelease());
		assertEquals(FlatFileDateUtils.getDay("11-MAY-2001"), entry.getLastUpdated());
		assertEquals(new Integer(67), entry.getLastUpdatedRelease());
		assertEquals(new Integer(2), entry.getVersion());
	}

	public void testRead_MinimalFormat() throws IOException {
		initLineReader(
				"DT   28-JAN-1993  34 11-MAY-2001 67 2"
		);
		ValidationResult result = (new DTReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(FlatFileDateUtils.getDay("28-JAN-1993"), entry.getFirstPublic());
		assertEquals(new Integer(34), entry.getFirstPublicRelease());
		assertEquals(FlatFileDateUtils.getDay("11-MAY-2001"), entry.getLastUpdated());
		assertEquals(new Integer(67), entry.getLastUpdatedRelease());
		assertEquals(new Integer(2), entry.getVersion());
	}

	public void testRead_FullFormat_withoutReleaseAndVersion() throws IOException {
		initLineReader(
				"DT   28-JAN-1993 (Created)\n" +
						"DT   11-MAY-2001 (Last updated)"
		);
		ValidationResult result = (new DTReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(FlatFileDateUtils.getDay("28-JAN-1993"), entry.getFirstPublic());
		assertNull(entry.getFirstPublicRelease());
		assertEquals(FlatFileDateUtils.getDay("11-MAY-2001"), entry.getLastUpdated());
		assertNull(entry.getLastUpdatedRelease());
		assertNull(entry.getVersion());
	}
}
