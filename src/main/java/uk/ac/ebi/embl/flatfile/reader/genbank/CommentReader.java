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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.embl.CCReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;

/** Reader for the flat file COMMENT line.
 */
public class CommentReader extends CCReader {

	public CommentReader(LineReader lineReader, FileType fileType) {
		super(lineReader, fileType);
	}

	public CommentReader(LineReader lineReader) {
		super(lineReader);
	}

	@Override
	public String getTag() {
		return GenbankTag.COMMENT_TAG;
	}
}
