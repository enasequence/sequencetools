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

import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Location;

public class ContigLocationMatcher extends FlatFileMatcher {

	public ContigLocationMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);
	}

	private static final Pattern PATTERN = Pattern.compile(
"(?:(?:\\s*(complement\\s*\\()?([\\w-\\.]+)\\s*\\.(\\d+)\\s*\\:\\s*(\\d+)\\s*\\.\\.\\s*(\\d+)\\)?)|(?:\\s*(gap)\\s*\\(?((?:\\d+)|(?:unk(\\d+)))\\s*\\)?))\\s*\\)?");
			private static final int GROUP_COMPLEMENT = 1;
	private static final int GROUP_ACCESSION = 2;
	private static final int GROUP_VERSION = 3;
	private static final int GROUP_BEGIN_POSITION = 4;
	private static final int GROUP_END_POSITION = 5;
	private static final int GROUP_GAP = 6;
	private static final int GROUP_GAP_LENGTH = 7;
	private static final int GROUP_UNKNOWN_GAP_LENGTH = 8;	

	public Location getLocation() {
		Location location = null;
		LocationFactory locationFactory = new  LocationFactory();
		boolean isGap = isValue(GROUP_GAP);
		if (isGap) {
			if (getString(GROUP_GAP_LENGTH).startsWith("unk")) {
				location = locationFactory.createUnknownGap(getLong(GROUP_UNKNOWN_GAP_LENGTH));
			}
			else {
				location = locationFactory.createGap(
					getLong(GROUP_GAP_LENGTH));
			}
		}
		else {
			String accession = getString(GROUP_ACCESSION);
			Integer version = getInteger(GROUP_VERSION);
			location = locationFactory.createRemoteRange(
					accession, version,	getLong(GROUP_BEGIN_POSITION), 
					getLong(GROUP_END_POSITION));
			location.setComplement(isValue(GROUP_COMPLEMENT));
		}
		return location;
	}		
}
