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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/** Flat file writer for the COMMENT lines.
 */
public class CommentWriter extends FlatFileWriter {

	public CommentWriter(Entry entry) {
		super(entry);
	}

	public boolean write(Writer writer) throws IOException {
		if (entry.getComment() == null ||
			isBlankString(entry.getComment().getText())) {
			return false;
		}
		List<String> comments = Arrays.asList(entry.getComment().getText().split("\n"));

		boolean isFirstLine = true;
		for (String line : comments) {
			if (isFirstLine) {
				writer.write(GenbankPadding.COMMENT_PADDING);
				isFirstLine = false;
			}
			else {
				writer.write(GenbankPadding.BLANK_PADDING);				
			}
			writer.write(line);
			writer.write("\n");
		}
		return true;
	}
}
