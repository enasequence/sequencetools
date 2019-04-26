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
import java.io.Writer;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

/** XML writer for the sequence entry.
 */
public class XmlEntryWriter {

	private Entry entry;
		
	public XmlEntryWriter(Entry entry) {		
		this.entry = entry;
	}

	public boolean write(Writer writer) throws IOException {
		if (entry == null) {
			return false;
		}
		boolean value = write(new SimpleXmlWriter(writer));
        writer.flush();
        return value;
	}

	private boolean write(SimpleXmlWriter writer) throws IOException {
	
    	writer.beginElement("entry");		
		
		// Write entry attributes.
		(new XmlEntryAttributeWriter(entry)).write(writer);

		writer.openElement("entry");

		// Write secondary accessions.
        for (Text secondaryAccession : entry.getSecondaryAccessions()) {
        	writer.writeSingleLineTextElement("secondaryAccession", secondaryAccession.getText());
        }
		// Write project accessions.		
		for (Text projectAccession : entry.getProjectAccessions()) {
			writer.writeSingleLineTextElement("projectAccession", projectAccession.getText());
		}
		// Write description.
		writer.writeSingleLineTextElement("description", entry.getDescription().getText());
    	
		// Write comment.
    	writer.writeMultiLineTextElement("comment", entry.getComment().getText());

		// Write keywords.		
		for (Text keyword : entry.getKeywords()) {
			writer.writeSingleLineTextElement("keyword", keyword.getText());
		}

		// Write references.
		for (Reference reference : entry.getReferences()) {
			new XmlReferenceWriter(entry, reference).write(writer);
		}
		
    	// Write cross-references.
		(new XmlXrefWriter(entry.getXRefs())).write(writer);
		
		// Write master cross-references.
		(new XmlMasterXrefWriter(entry)).write(writer);		
		
		// Write features.
		(new XmlFeatureWriter(entry)).write(writer);

		// Write assembly.
		(new XmlAssemblyWriter(entry)).write(writer);

		// Write contig.
		(new XmlContigWriter(entry)).write(writer);
				
		// Write sequence.		
    	(new XmlSequenceWriter(entry)).write(writer);
		
    	writer.closeElement("entry");
		
        return true;
	}	
}
