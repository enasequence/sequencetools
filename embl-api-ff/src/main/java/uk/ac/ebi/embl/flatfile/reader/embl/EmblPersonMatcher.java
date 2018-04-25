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

import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

public class EmblPersonMatcher extends FlatFileMatcher {

	public EmblPersonMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}

	public static final Pattern PATTERN = Pattern.compile(
			"^([^\\.]+)" + // surname
			"(\\s+[^\\s\\.]*\\s*\\..*)?$"); // first name
	
	private static final int GROUP_SURNAME = 1;
	private static final int GROUP_FIRST_NAME = 2;

	public Person getPerson() {	
		String surname = getString(GROUP_SURNAME);
		if (surname != null) {
			surname = surname.trim();
		}
		String firstName = getString(GROUP_FIRST_NAME);
		if (firstName != null) {
			firstName = FlatFileUtils.shrink(firstName.trim(), '.');
		}
		return (new ReferenceFactory()).createPerson(
			surname, firstName);
	}	
}
