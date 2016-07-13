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

public class OrganismMatcher extends FlatFileMatcher {

	public OrganismMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}
	
	private static final Pattern PATTERN = Pattern.compile(
			"^\\s*([^\\(]+)(?:\\((.+)\\))?\\s*$");

	private static int GROUP_SCIENTIFIC_NAME = 1;
	private static int GROUP_COMMON_NAME = 2;
	
	public String getScientificName() {
		return getString(GROUP_SCIENTIFIC_NAME);
	}

	public String getCommonName() {
		if (!isValue(GROUP_COMMON_NAME)) {
			return null;
		}
		return getString(GROUP_COMMON_NAME);
	}
}
