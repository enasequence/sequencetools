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
package uk.ac.ebi.embl.flatfile.writer.embl;

import uk.ac.ebi.embl.api.AccessionMatcher;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the ID lines.
 */
public class IDWriter extends FlatFileWriter {

	public IDWriter(Entry entry) {
		super(entry);
	}

	public boolean write(Writer writer) throws IOException {
		writer.write(EmblPadding.ID_PADDING);

		String accession = entry.getPrimaryAccession();
		if (!isBlankString(accession)) {
			writer.write(accession);
		}
		else {
			writer.write("XXX");
		}
		writer.write("; ");

		writer.write("SV ");
		if(entry.isMaster()) {
			if(entry.getPrimaryAccession() != null) {
				AccessionMatcher.Accession accn = AccessionMatcher.getSplittedAccession(entry.getPrimaryAccession());
				writer.write(accn != null ? accn.version: entry.getPrimaryAccession().substring(4, 6));
			} else {
				writer.write("XX");
			}
		} else if (entry.getSequence() != null) {
			Integer version = entry.getSequence().getVersion();
			if (version != null) {
					writer.write(version.toString());
			}
			else {
				writer.write("XXX");
			}
		}
		else {
			writer.write("XXX");
		}
		writer.write("; ");

		if (entry.getSequence() != null) {
			Sequence.Topology topology = entry.getSequence().getTopology();
			if (topology != null) {
				if (topology == Sequence.Topology.LINEAR) {
					writer.write("linear");
				} else if (topology == Sequence.Topology.CIRCULAR) {
					writer.write("circular");
				}
			}
			else {
				writer.write("XXX");
			}
		}
		else {
			writer.write("XXX");
		}
		writer.write("; ");

		if (entry.getSequence() != null) {
			String moleculeType = entry.getSequence().getMoleculeType();
			if (!isBlankString(moleculeType)) {
				writer.write(moleculeType);
			}
			else {
				writer.write("XXX");			
			}
		}
		else {
			writer.write("XXX");			
		}
		writer.write("; ");

		String dataclass =entry.getDataClass();

		if (!isBlankString(dataclass)) {
			writer.write(dataclass);
		}
		else {
			writer.write("XXX");			
		}
		writer.write("; ");

		String division = entry.getDivision();
		if (!isBlankString(division)) {
			writer.write(division);
		}
		else {
			writer.write("XXX");
		}
		writer.write("; ");

		if (entry.isMaster())
		{
			writer.write(String.valueOf(entry.getSequenceCount()));
			writer.write(" SQ");
		} else if (entry.getSequence() != null)
		{
			Long length = entry.getSequence().getLength();
			if (length == 0L && entry.getIdLineSequenceLength() != 0)
			{
				if (entry.isMaster() || Entry.SET_DATACLASS.equals(entry.getDataClass())||entry.isAnnotationOnlyCON())
				{
					length = entry.getIdLineSequenceLength();
				}
			}

			if (length != null)
			{
				writer.write(length.toString());
				writer.write(" BP");
			}
		}
	
		writer.write(".\n");
		return true;
	}
}
