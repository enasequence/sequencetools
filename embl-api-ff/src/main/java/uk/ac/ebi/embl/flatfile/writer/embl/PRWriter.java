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

import java.io.IOException;
import java.io.Writer;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the PR lines.
 */
public class PRWriter extends FlatFileWriter {

	public PRWriter(Entry entry, WrapType wrapType) {
		super(entry);
		setWrapType(wrapType);
		setWrapChar(WrapChar.WRAP_CHAR_SEMICOLON);
	}

	public boolean write(Writer writer) throws IOException {
		if (entry.getProjectAccessions() == null ||
			entry.getProjectAccessions().size() == 0 ) {
			return false;
		}

		StringBuilder block = new StringBuilder();
		boolean isFirstProjectAccession = true;
		for (Text projectAccession : entry.getProjectAccessions()) {
			if (isBlankString(projectAccession.getText())) {
				continue;
			}
			if (!isFirstProjectAccession) {
				block.append(" ");	
			}
			else {
				isFirstProjectAccession = false;
			}
			block.append("Project:");
			block.append(projectAccession.getText());
			block.append(";");
		}
		writeBlock(writer, EmblPadding.PR_PADDING, block.toString());
		return true;
	}
}
