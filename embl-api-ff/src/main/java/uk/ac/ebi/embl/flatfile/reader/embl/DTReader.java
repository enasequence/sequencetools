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

import java.util.regex.Pattern;

import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file DT lines.
 */
public class DTReader extends MultiLineBlockReader {
	
	public DTReader(LineReader lineReader) {
		super(lineReader, ConcatenateType.CONCATENATE_SPACE);
	}
	
	private static final Pattern PATTERN = Pattern.compile(
				"^\\s*" +
				"([\\w-]+)?" + // first public date
				"[^\\d]+" +
				"(\\d+)?" + // first public release
				"[^\\d]+" +
				"([\\w-]+)?" + // last updated date
				"[^\\d]+" +
				"(\\d+)?" + // last updated release
				"[^\\d]+" +
				"(\\d+)?" + // entry version
		        ".*$");

	private static int GROUP_FIRST_PUBLIC_DATE = 1;
	private static int GROUP_FIRST_PUBLIC_RELEASE = 2;
	private static int GROUP_LAST_UPDATED_DATE = 3;
	private static int GROUP_LAST_UPDATED_RELEASE = 4;
	private static int GROUP_ENTRY_VERSION = 5;

	@Override
	public String getTag() {
		return EmblTag.DT_TAG;
	}	
	
	@Override
	protected void read(String block) {
		FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
		if(!matcher.match(block)) {
			error("FF.1", getTag());
			return;
		}
		entry.setFirstPublic(matcher.getDay(GROUP_FIRST_PUBLIC_DATE));
		entry.setFirstPublicRelease(matcher.getInteger(GROUP_FIRST_PUBLIC_RELEASE));
		entry.setLastUpdated(matcher.getDay(GROUP_LAST_UPDATED_DATE));
		entry.setLastUpdatedRelease(matcher.getInteger(GROUP_LAST_UPDATED_RELEASE));
		entry.setVersion(matcher.getInteger(GROUP_ENTRY_VERSION));
	}	
}
