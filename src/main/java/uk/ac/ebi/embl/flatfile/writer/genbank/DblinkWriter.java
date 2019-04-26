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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the PROJECT lines.
 */
public class DblinkWriter extends FlatFileWriter {

	public DblinkWriter(Entry entry) {
		super(entry);
		}

	public boolean write(Writer writer) throws IOException {
		if (entry.getXRefs() == null || entry.getXRefs().isEmpty() ) {
			return false;
		}

		boolean contentWritten = false;

		if(entry.getProjectAccessions() != null && !entry.getProjectAccessions().isEmpty()) {
			//move text BioProject to a constant file
			contentWritten = writeDBLink(writer, false, "BioProject", entry.getProjectAccessions().get(0).getText());
		}

        for(XRef xref : entry.getXRefs()) {
			contentWritten = writeDBLink(writer, contentWritten, xref.getDatabase(), xref.getPrimaryAccession());
		}

		return contentWritten;
	}

	private boolean writeDBLink(Writer writer, boolean contentWritten, String key , String value) throws  IOException{

		if (contentWritten) {
			writer.write(GenbankPadding.BLANK_PADDING);
		} else {
			writer.write(GenbankPadding.DBLINK_PADDING);
		}

		if (!isBlankString(key)) {
			writer.write(key);
		}
		writer.write(": ");
		if (!isBlankString(value)) {
			writer.write(value);
		}
		writer.write("\n");

		return true;
	}
}
