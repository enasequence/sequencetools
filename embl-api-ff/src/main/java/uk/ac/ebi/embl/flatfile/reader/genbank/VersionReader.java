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

import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the flat file VERSION lines.
 */
public class VersionReader extends SingleLineBlockReader {
	
	public VersionReader(LineReader lineReader) {
		super(lineReader);
	}

	private static final Pattern PATTERN = Pattern.compile(
				"^\\s*" +
				"([^\\s]+)" + // primary accession
				"\\s*\\.\\s*" +
				"(\\d+)" + // sequence version
		        "(\\s+GI\\:\\d+)?.*$");

	private static int GROUP_PRIMARY_ACCESSION = 1;
	private static int GROUP_SEQUENCE_VERSION = 2;
	private static int GROUP_GI_ACCESSION = 3;

	@Override
	public String getTag() {
		return GenbankTag.VERSION_TAG;
	}
	
	@Override
	protected void read(String block) {
		entry.setOrigin(getOrigin());
		FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
		if(!matcher.match(block)) {
			error("FF.1", getTag());
			return;
		}
		if (matcher.isValueXXX(GROUP_PRIMARY_ACCESSION)) {
			entry.setPrimaryAccession(matcher.getUpperString(GROUP_PRIMARY_ACCESSION));
		}
		if (matcher.isValueXXX(GROUP_SEQUENCE_VERSION)) {
			entry.getSequence().setVersion(matcher.getInteger(GROUP_SEQUENCE_VERSION));
		}
		if (matcher.isValue(GROUP_GI_ACCESSION)) {
			entry.getSequence().setGIAccession(matcher.getString(GROUP_GI_ACCESSION));
		}
	}	
}
