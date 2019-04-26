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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.Text;

public class CommentWriterTest extends GenbankWriterTest {

	public void testWrite_Comment() throws IOException {
		entry.setComment(new Text(
				"-------------- Genome Center\n" +
				"     Center: NIH Intramural Sequencing Center\n" +
				"     Center code: NISC\n" +
				"     Web site: http://www.nisc.nih.gov\n" +
				"     Contact: nisc_zoo@nhgri.nih.gov\n" +
				"All clones contained in this assembly were sequenced by the\nNIH Intramural Sequencing Center (NISC).  This multi-clone\nDNA sequence was assembled by NISC staff."));				
		StringWriter writer = new StringWriter();
		assertTrue(new CommentWriter(entry).write(writer));
		assertEquals(
				"COMMENT     -------------- Genome Center\n" +
				"                 Center: NIH Intramural Sequencing Center\n" +
				"                 Center code: NISC\n" +
				"                 Web site: http://www.nisc.nih.gov\n" +
				"                 Contact: nisc_zoo@nhgri.nih.gov\n" +
				"            All clones contained in this assembly were sequenced by the\n" +
				"            NIH Intramural Sequencing Center (NISC).  This multi-clone\n" +
				"            DNA sequence was assembled by NISC staff.\n",
				writer.toString());
	}

	public void testWrite_NoComment() throws IOException {
		entry.setComment(null);
		StringWriter writer = new StringWriter();
		assertFalse(new CommentWriter(entry).write(writer));
		assertEquals("", writer.toString());
	}
}
