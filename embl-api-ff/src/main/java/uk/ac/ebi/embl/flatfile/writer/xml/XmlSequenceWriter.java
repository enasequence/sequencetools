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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;
import java.util.Arrays;

public class XmlSequenceWriter {

	private Entry entry;
	
	public XmlSequenceWriter(Entry entry) {
		this.entry = entry;
	}
	
	public boolean write(SimpleXmlWriter writer) throws IOException {	
		if (entry.getSequence() == null) {
			return false;
		}
		// Sequence is not escaped.
		byte[] sequence = entry.getSequence().getSequenceByte();
		if (sequence == null || sequence.equals("")) {
			return false;
		}
    	writer.beginElement("sequence");
    	writer.openElement("sequence");
    	boolean escapeXml = writer.isEscapeXml();
    	writer.setEscapeXml(false);
    	try {
		    int length = sequence.length;
		    int i = 0;
			for (i = 0; i < length - 60; i += 60) {
				writer.writeElementText(new String(Arrays.copyOfRange(sequence,i, i + 60)));
				writer.writeElementText("\n");
			}
			if (i < length) {
				writer.writeElementText(new String(Arrays.copyOfRange(sequence,i,sequence.length)));
				writer.writeElementText("\n");
			}
			writer.closeElement("sequence");
    	}
    	catch (IOException e) {
    		throw e;
    	}
    	finally {
    		writer.setEscapeXml(escapeXml);    		
    	}
		return true;
	}
}
