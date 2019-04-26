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

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.XRef;

public class XRefQualifierMatcher extends FlatFileMatcher {

	public XRefQualifierMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}

	private static final Pattern PATTERN = Pattern.compile(			
			"([^:]+):(.+)");
	
	private static final int GROUP_DATABASE = 1;
	private static final int GROUP_ACCESSION = 2;

	public XRef getXref() {	
		String database = getString(GROUP_DATABASE);
		String accession = getString(GROUP_ACCESSION);
		return (new EntryFactory()).createXRef(database, accession);
	}	
}
