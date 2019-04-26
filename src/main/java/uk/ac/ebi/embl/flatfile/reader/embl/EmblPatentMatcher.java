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

import uk.ac.ebi.embl.api.entry.reference.Patent;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

public class EmblPatentMatcher extends FlatFileMatcher {

	public EmblPatentMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}
		
	private static final Pattern PATTERN = Pattern.compile(
			"^\\s*(?:(?:Patent\\s*number)|(?:Patent)|(?:Pre-Grant\\s*Patent))\\s*\\:?\\s*([a-zA-Z]{2})?\\s*([a-zA-Z0-9]+)?\\s*-([a-zA-Z0-9]+)?\\s*(?:/|\\s*)(\\d+)?\\s*(?:,|\\s)\\s*([\\w-]+)?\\s*(?:\\.|;)\\s*(.+)?$"
		);

	private static int GROUP_PATENT_OFFICE = 1;
	private static int GROUP_PATENT_NUMBER = 2;
	private static int GROUP_PATENT_TYPE = 3;
	private static int GROUP_SEQUENCE_NUMBER = 4;
	private static int GROUP_DAY = 5;
	private static int GROUP_APPLICANTS = 6;
	
	public Patent getPatent(Publication publication) {
		Patent patent = null;
		if (publication != null) {
			patent = (new ReferenceFactory()).createPatent(publication);
			patent.setOrigin(publication.getOrigin());
		}
		else {
			patent = (new ReferenceFactory()).createPatent();
		}
		patent.setPatentOffice(getUpperString(GROUP_PATENT_OFFICE));
		if (patent.getPatentOffice() == null) {
			error("RL.5");
		}
		patent.setPatentNumber(getUpperString(GROUP_PATENT_NUMBER));
		if (patent.getPatentNumber() == null) {
			error("RL.6");
		}
		patent.setPatentType(getUpperString(GROUP_PATENT_TYPE));
		if (patent.getPatentType() == null) {
			error("RL.7");
		}
		patent.setSequenceNumber(getInteger(GROUP_SEQUENCE_NUMBER));
		if (patent.getSequenceNumber() == null) {
			error("RL.8");
		}
		patent.setDay(getDay(GROUP_DAY));
		if (patent.getDay() == null) {
			error("RL.9");
		}
		String applicants = getString(GROUP_APPLICANTS);
		if(null != applicants) {
			for (String applicant : FlatFileUtils.split(applicants, ";")) {
				patent.addApplicant(FlatFileUtils.trimRight(applicant, '.'));
			}
		}
		return patent;
	}
}
