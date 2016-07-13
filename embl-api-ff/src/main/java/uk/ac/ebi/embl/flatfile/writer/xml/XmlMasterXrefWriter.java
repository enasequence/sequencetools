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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlMasterXrefWriter {

	private Entry entry;
	
	public XmlMasterXrefWriter(Entry entry) {		
		this.entry = entry;
	}
	
    public boolean write(SimpleXmlWriter writer) throws IOException {
    	if (entry == null) {
    		return false;
    	}
    	for (Text accession : entry.getMasterWgsAccessions()) {
			writer.beginElement("xref");	
			writer.writeAttribute("db", "ENA-WGS");
			writer.writeAttribute("id", accession.getText());
    		writer.openCloseElement("xref");
		}
    	for (Text accession : entry.getMasterConAccessions()) {
			writer.beginElement("xref");	
			writer.writeAttribute("db", "ENA-CON");
			writer.writeAttribute("id", accession.getText());
    		writer.openCloseElement("xref");
		}
    	for (Text accession : entry.getMasterTpaAccessions()) {
			writer.beginElement("xref");	
			writer.writeAttribute("db", "ENA-TPA");
			writer.writeAttribute("id", accession.getText());
    		writer.openCloseElement("xref");
		}
    	for (Text accession : entry.getMasterTsaAccessions()) {
			writer.beginElement("xref");	
			writer.writeAttribute("db", "ENA-TSA");
			writer.writeAttribute("id", accession.getText());
    		writer.openCloseElement("xref");
		}
    	return true;
     }
}
