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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the CC lines.
 */
public class CCWriter extends FlatFileWriter {

	private static final int CC_OPTIMAL_LINE_LENGTH = 200;

	public CCWriter(Entry entry) {
		super(entry);
	}

	public boolean write(Writer writer) throws IOException {
		if (entry.getComment() == null ||
			isBlankString(entry.getComment().getText())) {
			return false;
		}
		// Preserve CC line wrapping provided by the submitter while
		// keeping the lines shorter than 200 characters as requested
		// by NCBI. This is done by using optimal line length of 200
		// and force break. The optimal line length is longer then
		// the default optimal line length to preserve the submitter
		// provided CC line wrapping as much as possible.
		setForceLineBreak(true);
		setOptimalLineLength(CC_OPTIMAL_LINE_LENGTH);
		for (String line : entry.getComment().getText().split("\n")) {
			if (line.trim().isEmpty()) {
				writeLine(writer, EmblPadding.CC_PADDING, "");
			}
			else {
			writeBlock(writer, EmblPadding.CC_PADDING, line);
		}
		}
		return true;
	}
}
