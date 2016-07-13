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
import java.util.List;

import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlAssemblyWriter {

	private Entry entry;
	
	public XmlAssemblyWriter(Entry entry) {
		this.entry = entry;
	}
	
    public boolean write(SimpleXmlWriter writer) throws IOException {
		List<Assembly> assemblies = entry.getAssemblies();
		if (assemblies == null ||
			assemblies.size() == 0) {
			return false;
		}

		writer.beginElement("assembly");
		writer.openElement("assembly");
		
		for (Assembly assembly : assemblies) {
			writer.beginElement("range");
			writer.writeAttribute("begin", assembly.getSecondarySpan().getBeginPosition());
			writer.writeAttribute("end", assembly.getSecondarySpan().getEndPosition());
			writer.writeAttribute("primaryBegin", assembly.getPrimarySpan().getBeginPosition());
			writer.writeAttribute("primaryEnd", assembly.getPrimarySpan().getEndPosition());
			writer.writeAttribute("accession", assembly.getPrimarySpan().getAccession());
			writer.writeAttribute("version", assembly.getPrimarySpan().getVersion());
			if (assembly.getPrimarySpan().isComplement()) {
				writer.writeAttribute("complement", true);	
			}
			writer.openCloseElement("range");
		}		
		writer.closeElement("assembly");	
		
		return true;
	}	
}
