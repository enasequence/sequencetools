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
package uk.ac.ebi.embl.flatfile.writer.embl;

import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.writer.embl.CCWriter;

import java.io.IOException;
import java.io.StringWriter;

public class CCWriterTest extends EmblWriterTest {

	public void testWrite_Comment() throws IOException {
		entry.setComment(new Text(
				"-------------- Genome Center\n" +
				"     Center: NIH Intramural Sequencing Center\n" +
				"     Center code: NISC\n" +
				"     Web site: http://www.nisc.nih.gov\n" +
				"     Contact: nisc_zoo@nhgri.nih.gov\n" +
				"All clones contained in this assembly were sequenced by the\nNIH Intramural Sequencing Center (NISC).  This multi-clone\nDNA sequence was assembled by NISC staff."));				
		StringWriter writer = new StringWriter();
		assertTrue(new CCWriter(entry).write(writer));
		assertEquals(
				"CC   -------------- Genome Center\n" +
				"CC        Center: NIH Intramural Sequencing Center\n" +
				"CC        Center code: NISC\n" +
				"CC        Web site: http://www.nisc.nih.gov\n" +
				"CC        Contact: nisc_zoo@nhgri.nih.gov\n" +
				"CC   All clones contained in this assembly were sequenced by the\n" +
				"CC   NIH Intramural Sequencing Center (NISC).  This multi-clone\n" +
				"CC   DNA sequence was assembled by NISC staff.\n",
				writer.toString());
	}

	public void testWrite_NoComment() throws IOException {
		entry.setComment(null);
		StringWriter writer = new StringWriter();
		assertFalse(new CCWriter(entry).write(writer));
		assertEquals("", writer.toString());
	}
}
