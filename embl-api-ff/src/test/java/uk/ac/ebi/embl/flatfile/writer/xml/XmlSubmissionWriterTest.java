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
package uk.ac.ebi.embl.flatfile.writer.xml;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlSubmissionWriterTest extends XmlWriterTest {

	public void testWrite() throws IOException {
		ReferenceFactory referenceFactory = new ReferenceFactory();
		Submission submission = referenceFactory.createSubmission(
				"", FlatFileUtils.getDay("10-SEP-1998"),	null);
		submission.setSubmitterAddress("Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121, Berkeley, CA 94720, USA");
		StringWriter writer = new StringWriter();
        assertTrue(new XmlReferenceLocationWriter(entry, submission).write(new SimpleXmlWriter(writer)));
        //System.out.print(writer.toString());
		assertEquals(
				"<referenceLocation>\n" +
				"Submitted (10-SEP-1998) to the INSDC.\n" +
				"Great Drosophila Genome Center, Lawrence Berkeley Laboratory, MS 64-121,\n" +
				"Berkeley, CA 94720, USA\n" + 
				"</referenceLocation>\n",
				writer.toString());
	}
}
