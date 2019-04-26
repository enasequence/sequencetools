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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlContigWriter {
	
	private Entry entry;	

	public XmlContigWriter(Entry entry) {
		this.entry = entry;
	}
	
    public boolean write(SimpleXmlWriter writer) throws IOException {
    	
		List<Location> contigs = entry.getSequence().getContigs();
		if (contigs == null || contigs.size() == 0) {
			return false;
		}
		writer.beginElement("contig");
		writer.openElement("contig");
		
		Long beginPosition = 1L;
		for (Location contig : contigs) {
			if (contig instanceof Gap) {
				writer.beginElement("gap");
				Gap gap = (Gap)contig;
				writer.writeAttribute("begin", beginPosition);
				beginPosition += contig.getLength();				
				writer.writeAttribute("end", beginPosition - 1);				
				writer.writeAttribute("length", gap.getLength());				
				writer.writeAttribute("unknownLength", gap.isUnknownLength());
				writer.openCloseElement("gap");
			}
			else {
				writer.beginElement("range");
				writer.writeAttribute("primaryBegin", contig.getBeginPosition());
				writer.writeAttribute("primaryEnd", contig.getEndPosition());
				writer.writeAttribute("begin", beginPosition);
				beginPosition += contig.getLength();				
				writer.writeAttribute("end", beginPosition - 1);				
				if (contig instanceof RemoteLocation)  {
					writer.writeAttribute("accession", 
							((RemoteLocation)contig).getAccession());
					writer.writeAttribute("version", 
							((RemoteLocation)contig).getVersion());
				}
				writer.writeAttribute("complement", contig.isComplement());
				writer.openCloseElement("segment");
			}
		}
		writer.closeElement("contig");
		
		return true;
	}	
}
