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

import java.util.Vector;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

public class EmblArticleIssueMatcher extends FlatFileMatcher {

	public EmblArticleIssueMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);
	}

	private static final Pattern PATTERN = Pattern.compile(
			// journal volume (issue)
			"^" +
			"\\s*" +
			"([^\\(\\)]+)" + // journal + volume
			"(\\s*\\([^\\(\\)]+\\)\\s*)?" // issue
		);
	
	private static int GROUP_JOURNAL_VOLUME = 1;
	private static int GROUP_ISSUE = 2;
	
	public String getJournal() {
		// Assume that the last token is the volume. This is not
		// reliable but best we can do without having a list
		// of journals to compare against.
		Vector<String> v = FlatFileUtils.split(
				getString(GROUP_JOURNAL_VOLUME), "\\s+");
		if (v.size() == 0) {
			return null;
		}
		if (v.size() == 1) {
			return v.get(0);
		}
		StringBuilder str = new StringBuilder(); 
		for (int i = 0 ; i < v.size() - 1 ; ++i) {
			if (i > 0) {
				str.append(' ');
			}
			str.append(v.get(i));
		}
		return str.toString(); 
	}

	public String getVolume() {
		// Assume that the last token is the volume. This is not
		// reliable but best we can do without having a list
		// of journals to compare against.
		Vector<String> v = FlatFileUtils.split(
				getString(GROUP_JOURNAL_VOLUME), "\\s+");
		if (v.size() > 1) {
			return v.get(v.size() - 1);
		}
		return null;
	}

	public String getIssue() {
		String issue = getString(GROUP_ISSUE);
		if (issue == null) {
			return null;
		}
		issue = FlatFileUtils.trimLeft(issue, '(');
		issue = FlatFileUtils.trimRight(issue, ')');
		return FlatFileUtils.shrink(issue);
	}

}
