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

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the flat file PUBMED lines.
 */
public class PubmedReader extends SingleLineBlockReader {
	
	public PubmedReader(LineReader lineReader) {
		super(lineReader);
	}

	private static final Pattern PATTERN = Pattern.compile(
			"^\\s*(\\d+)\\s*$");

	private static int GROUP_PUBMED = 1;

	@Override
	public String getTag() {
		return GenbankTag.PUBMED_TAG;
	}
	
	@Override
	protected void read(String block) {
		FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
		if(!matcher.match(block)) {
			error("FF.1", getTag());
			return;
		}
		getCache().getPublication().addXRef(
				(new EntryFactory()).createXRef("PUBMED", 
						matcher.getString(GROUP_PUBMED)));				
	}	
}
