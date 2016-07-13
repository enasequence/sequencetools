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
package uk.ac.ebi.embl.flatfile.reader;

import java.util.Date;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.Entry;

public class StatusMatcher extends FlatFileMatcher {

	public StatusMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}
	
	private static final Pattern PATTERN = Pattern.compile(
			"\\s*(\\w+)(?:\\s+([\\w-]+))?");

	private static int GROUP_STATUS = 1;
	private static int GROUP_HOLD_DATE = 2;

	public Entry.Status getStatus() {
		String status = getString(GROUP_STATUS).toLowerCase();
		if(status.equals("public")) {
			return Entry.Status.PUBLIC;
		}
		if(status.equals("cancelled")) {
			return Entry.Status.CANCELLED;
		}
		if(status.equals("killed")) {
			return Entry.Status.KILLED;
		}
		if(status.equals("private")) {
			return Entry.Status.PRIVATE;
		}
		if(status.equals("suppressed")) {
			return Entry.Status.SUPPRESSED;
		}
		if(status.equals("draft")) {
			return Entry.Status.DRAFT;
		}
		error("ST.1", getString(GROUP_STATUS));
		return null;
	}
	
	public Date getHoldDate() {
		if (isValueXXX(GROUP_HOLD_DATE)) {
			return getDay(GROUP_HOLD_DATE);
		}
		return null;
	}
}
