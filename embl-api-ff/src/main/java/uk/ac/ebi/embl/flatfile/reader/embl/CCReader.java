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
package uk.ac.ebi.embl.flatfile.reader.embl;

import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file CC lines.
 */
public class CCReader extends MultiLineBlockReader {
	
	public CCReader(LineReader lineReader) {
		super(lineReader, ConcatenateType.CONCATENATE_BREAK);
	}

	public CCReader(LineReader lineReader, FileType fileType) {
		super(lineReader, ConcatenateType.CONCATENATE_BREAK, fileType);
	}

	@Override
	public String getTag() {
		return EmblTag.CC_TAG;
	}
	
	@Override
	protected void read(String block) {
		String comment = block;
		if (entry.getComment().getText() != null) {
			// Concatenate multiple comment blocks together and
			// separate them using an empty line.
			StringBuilder commentBuilder = new StringBuilder();
			commentBuilder.append(entry.getComment().getText());
			commentBuilder.append("\n");
			commentBuilder.append("\n");
			commentBuilder.append(block);
			comment = commentBuilder.toString();
		}
		entry.setComment(new Text(comment, getOrigin()));		
	}
}
