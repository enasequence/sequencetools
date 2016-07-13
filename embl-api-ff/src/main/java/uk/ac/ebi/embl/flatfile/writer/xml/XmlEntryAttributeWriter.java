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
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;
import java.util.Date;

public class XmlEntryAttributeWriter  {

	private Entry entry;
	
	public XmlEntryAttributeWriter(Entry entry) {
		this.entry = entry;
	}
	
	public boolean write(SimpleXmlWriter writer) throws IOException {
		String accession = entry.getPrimaryAccession();
		writer.writeAttribute("accession", accession); 
		if (entry.getSequence() != null) {
			Integer version = entry.getSequence().getVersion();
			writer.writeAttribute("version", version);
		}
		Integer entryVersion = entry.getVersion();
		writer.writeAttribute("entryVersion", entryVersion);
		String dataclass = entry.getDataClass();
		writer.writeAttribute("dataClass", dataclass);
		String division = entry.getDivision();
		writer.writeAttribute("taxonomicDivision", division);
		if (entry.getSequence() != null) {
			String moleculeType = entry.getSequence().getMoleculeType();
			writer.writeAttribute("moleculeType", moleculeType);
			//Long length =entry.isMaster()?entry.getIdLineSequenceLength():entry.getSequence().getLength();
            Long length =entry.getIdLineSequenceLength();

			writer.writeAttribute("sequenceLength", length);
			Sequence.Topology topology = entry.getSequence().getTopology();
			if (topology != null) {
				if (topology == Sequence.Topology.LINEAR) {
					writer.writeAttribute("topology", "linear");
				} else if (topology == Sequence.Topology.CIRCULAR) {
					writer.writeAttribute("topology", "circular");
				}
			}
		}
		Date firstPublic = entry.getFirstPublic();
		writer.writeAttribute("firstPublic", firstPublic);
		Integer firstPublicRelease = entry.getFirstPublicRelease();
		writer.writeAttribute("firstPublicRelease", firstPublicRelease);
		Date lastUpdated = entry.getLastUpdated();
		writer.writeAttribute("lastUpdated", lastUpdated);
		Integer lastUpdatedRelease = entry.getLastUpdatedRelease();
		writer.writeAttribute("lastUpdatedRelease", lastUpdatedRelease);
		return true;
	}
}
