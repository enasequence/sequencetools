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

import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file AUTHOR lines.
 */
public class AuthorsReader extends MultiLineBlockReader {
	
	public AuthorsReader(LineReader lineReader) {
		super(lineReader, ConcatenateType.CONCATENATE_SPACE);
	}
	
	@Override
	public String getTag() {
		return GenbankTag.AUTHORS_TAG;
	}

	private static final Pattern REPLACE = Pattern.compile(",");

	@Override
	protected void read(String block) {
		getCache().getReference().setAuthorExists(true);
		for (String author : FlatFileUtils.split(block, "(,\\s+)|(\\s+and\\s+)")) {
			GenbankPersonMatcher personMatcher = new GenbankPersonMatcher(this);
			if (!personMatcher.match(author)) {
				// Replace any remaining commas with spaces.
				author = REPLACE.matcher(author).replaceAll(" ");
				author = FlatFileUtils.shrink(author);
				getCache().getPublication().addAuthor(
					(new ReferenceFactory()).createPerson(author));
			}
			else {
				getCache().getPublication().addAuthor(personMatcher.getPerson());
			}
		}
	}
}
