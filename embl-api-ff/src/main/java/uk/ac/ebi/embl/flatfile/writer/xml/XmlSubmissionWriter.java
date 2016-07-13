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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblSubmissionWriter;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlSubmissionWriter {
	
	private Entry entry;
    private Submission submission;

	public XmlSubmissionWriter(Entry entry, Submission submission) {
		this.entry = entry;
		this.submission = submission;
	}

    public boolean write(SimpleXmlWriter writer) throws IOException {
    	StringWriter stringWriter = new StringWriter();
    	(new EmblSubmissionWriter(entry, submission, WrapType.EMBL_WRAP, "")).write(stringWriter);
    	writer.writeElementText(stringWriter.toString());
    	return true;
    }	
}
