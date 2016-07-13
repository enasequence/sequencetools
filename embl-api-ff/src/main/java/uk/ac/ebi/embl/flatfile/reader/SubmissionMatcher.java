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

import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;

public class SubmissionMatcher extends FlatFileMatcher {

	public SubmissionMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}
		
	private static final Pattern PATTERN = Pattern.compile(
            "^\\s*Submitted\\s*\\(\\s*([\\w-]+)?\\s*\\)(?:\\s*to\\s*the\\s*(?:(?:\\w+/\\w+/\\w+\\s*databases)|(?:INSDC)){0,1}\\.)?(.*)?$"			
			);

	private static int GROUP_SUBMISSION_DATE = 1;
	private static int GROUP_SUBMITTER_ADDRESS = 2;

	public Submission getSubmission(Publication publication) {
		Submission submission = null;
		if (publication != null) {
			submission = (new ReferenceFactory()).createSubmission(publication);
			submission.setOrigin(publication.getOrigin());
		}
		else {
			submission = (new ReferenceFactory()).createSubmission();
		}
		submission.setDay(getDay(GROUP_SUBMISSION_DATE));
		submission.setSubmitterAddress(getString(GROUP_SUBMITTER_ADDRESS));
		return submission;
	}
}
