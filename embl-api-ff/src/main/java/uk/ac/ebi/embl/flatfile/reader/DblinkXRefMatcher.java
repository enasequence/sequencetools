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

import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.XRef;

public class DblinkXRefMatcher extends FlatFileMatcher {

	public DblinkXRefMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}

	private static final Pattern PATTERN = Pattern.compile
			("^(\\s*([^:]+)" //DATABASE
			+ "(\\s*:\\s*)"
			+ "([^:]+)" //ACCESSION
			+ "\\s*)$");

    private static int GROUP_DATABASE = 2;
	private static int GROUP_ACCESSION = 4;


	public boolean match(String string) {	
		boolean matches = super.match(string);
		if (matches) {
			if (getString(GROUP_DATABASE) == null) {
				matches = false;
			}
			else if (getString(GROUP_ACCESSION) == null) {
				matches = false;
			}
		}
		return matches;
	}
	
	public XRef getXRef() {	
		XRef xref = (new EntryFactory()).createXRef();
		xref.setDatabase(FlatFileUtils.trimRight(
				getString(GROUP_DATABASE), ' '));
		xref.setPrimaryAccession(FlatFileUtils.trimRight(
				getString(GROUP_ACCESSION), ' '));
		return xref;
	}	
}
