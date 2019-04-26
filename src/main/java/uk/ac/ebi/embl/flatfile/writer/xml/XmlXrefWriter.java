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

import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;
import java.util.List;

public class XmlXrefWriter {

    private List<XRef> xrefs;

	public XmlXrefWriter(List<XRef> xrefs) {
		this.xrefs = xrefs;
	}

    public boolean write(SimpleXmlWriter writer) throws IOException {
    	if (xrefs == null || xrefs.size() == 0) {
    		return false;
    	}
		for (XRef xref : xrefs) {
			writer.beginElement("xref");	
			writer.writeAttribute("db", xref.getDatabase());
			writer.writeAttribute("id", xref.getPrimaryAccession());
			writer.writeAttribute("secondaryId", xref.getSecondaryAccession());
    		writer.openCloseElement("xref");
		}
    	return true;
     }
}
