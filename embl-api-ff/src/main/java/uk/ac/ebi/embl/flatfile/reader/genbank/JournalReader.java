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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.ElectronicReferenceMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;
import uk.ac.ebi.embl.flatfile.reader.SubmissionMatcher;
import uk.ac.ebi.embl.flatfile.reader.ThesisMatcher;
import uk.ac.ebi.embl.flatfile.reader.UnpublishedMatcher;

import java.util.regex.Pattern;

/** Reader for the flat file JOURNAL line.
 */
public class JournalReader extends MultiLineBlockReader {

	public JournalReader(LineReader lineReader) {
		super(lineReader,  ConcatenateType.CONCATENATE_SPACE);
	}

	public JournalReader(LineReader lineReader, FileType fileType) {
		super(lineReader,  ConcatenateType.CONCATENATE_SPACE, fileType);
	}

	private static final Pattern UNPUBLISHED_WITH_YEAR_PATTERN = Pattern.compile(
			"^(Unpublished\\s*\\([0-9]*\\))\\s*(\\.)?\\s*"
	);

	@Override
	public String getTag() {
		return GenbankTag.JOURNAL_TAG;
	}
	
	@Override
	protected void read(String block) {
		Publication publication = null;
		UnpublishedMatcher unpublishedMatcher = new UnpublishedMatcher(this);
		if (unpublishedMatcher.match(block)) {
			publication = unpublishedMatcher.getUnpublished(getCache().getPublication());
		} else if(UNPUBLISHED_WITH_YEAR_PATTERN.matcher(block).matches()) {
			Unpublished unPublished = unpublishedMatcher.getUnpublished(getCache().getPublication());
			unPublished.setJournalBlock(block);
			publication =  unPublished;
		}
		if (publication == null) {
			ThesisMatcher thesisMatcher = new ThesisMatcher(this);
			if (thesisMatcher.match(block)) {
				publication = thesisMatcher.getThesis(getCache().getPublication());
			}
		}
		if (publication == null) {
			SubmissionMatcher submissionMatcher = new SubmissionMatcher(this);
			if (submissionMatcher.match(block)) {
				publication = submissionMatcher.getSubmission(getCache().getPublication());
			}
		}
		if (publication == null) {
			GenbankPatentMatcher patentMatcher = new GenbankPatentMatcher(this);
			if (patentMatcher.match(block)) {
				publication = patentMatcher.getPatent(getCache().getPublication());
			}
		}
		if (publication == null) {
			GenbankBookMatcher bookMatcher = new GenbankBookMatcher(this);
			if (bookMatcher.match(block)) {
				publication = bookMatcher.getBook(getCache().getPublication());
			}
		}
		if (publication == null) {
			GenbankArticleMatcher articleMatcher = new GenbankArticleMatcher(this);
			if (articleMatcher.match(block)) {
				publication = articleMatcher.getArticle(getCache().getPublication());
			}
		}
		if (publication == null) {
			ElectronicReferenceMatcher electronicReferenceMatcher = new ElectronicReferenceMatcher(this);
			if (electronicReferenceMatcher.match(block)) {
				publication = electronicReferenceMatcher.getElectronicReference(getCache().getPublication());
			}
		}
		if (publication != null) {
			getCache().setPublication(publication);
			entry.addReference(getCache().getReference());
		}
		else {
			error("FF.1", getTag());
		}
	}
}
