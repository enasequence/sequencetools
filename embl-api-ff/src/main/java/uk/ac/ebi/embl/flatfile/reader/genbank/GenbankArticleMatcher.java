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

import java.util.regex.Pattern;

import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

/** It is not possible to reliably separate the journal name from 
 * volume and issues without comparing against known journal names.
 * These are stored in the journal name.
 */
public class GenbankArticleMatcher extends FlatFileMatcher {

	public GenbankArticleMatcher(FlatFileLineReader reader) {
		super(reader, PATTERN);		
	}
			
	private static final Pattern PATTERN = Pattern.compile(
			// journal volume (issue), first page-last page (year)
			"(?:^" +
			"(.+)" + // journal + volume + issue
			"\\," +
			"\\s*" +
			"([^\\(\\-\\)\\.]+)?" + // first page
			"\\s*" +
			"(?:-\\s*([^\\(\\)\\-\\.]+))?" + // last page
			"\\s*" +
			"(?:\\(\\s*(\\d+)\\s*\\)\\s*)?" + // year
			".*$)|" +
		    // journal volume (issue) (year) In press
			"(?:^" +
			"(.+)" + // journal + volume + issue
			"\\s*" +
			"(?:\\(\\s*(\\d+)\\s*\\)\\s*)" + // year
			"\\s*" +
			"In\\s*press.*$)|" +
			// journal volume (issue) In Press
			"(?:^" +
			"(.+)" + // journal + volume + issue
			"\\s*" +			
			"In\\s*press.*$)|"+
			//journal volume (year)
			"((?:^(.+)\\s*(?:\\((\\d{4})\\))\\s*$))"
		);

	private static int GROUP_1_JOURNAL_VOLUME_ISSUE = 1;
	private static int GROUP_1_FIRST_PAGE = 2;
	private static int GROUP_1_LAST_PAGE = 3;
	private static int GROUP_1_YEAR = 4;
	private static int GROUP_2_JOURNAL_VOLUME_ISSUE = 5;
	private static int GROUP_2_YEAR = 6;
	private static int GROUP_3_JOURNAL_VOLUME_ISSUE = 7;
	private static int GROUP_4_JOURNAL_VOLUME_YEAR = 8;
	private static int GROUP_4_JOURNAL = 9;
	private static int GROUP_4_YEAR = 10;//year from last group
	
	public Article getArticle(Publication publication) {
		Article article = null;
		if (publication != null) {
			article = (new ReferenceFactory()).createArticle(publication);
			article.setOrigin(publication.getOrigin());
		}
		else {
			article = (new ReferenceFactory()).createArticle();
		}
		if (isValue(GROUP_1_JOURNAL_VOLUME_ISSUE)) {
			String journal = FlatFileUtils.shrink(getString(GROUP_1_JOURNAL_VOLUME_ISSUE));
			article.setJournal(journal);
			String firstPage = FlatFileUtils.shrink(getString(GROUP_1_FIRST_PAGE));
			String lastPage = FlatFileUtils.shrink(getString(GROUP_1_LAST_PAGE));
			article.setFirstPage(firstPage);
			article.setLastPage(lastPage);
			article.setYear(getYear(GROUP_1_YEAR));
		}
		else if (isValue(GROUP_2_JOURNAL_VOLUME_ISSUE)) {
			String journal = FlatFileUtils.shrink(getString(GROUP_2_JOURNAL_VOLUME_ISSUE));
			article.setJournal(journal);
			article.setYear(getYear(GROUP_2_YEAR));
		}
		else if(isValue(GROUP_3_JOURNAL_VOLUME_ISSUE)){
			String journal = FlatFileUtils.shrink(getString(GROUP_3_JOURNAL_VOLUME_ISSUE));
			article.setJournal(journal);
		} else if(isValue(GROUP_4_JOURNAL_VOLUME_YEAR)){
			String journal = FlatFileUtils.shrink(getString(GROUP_4_JOURNAL));
			article.setJournal(journal);
			article.setYear(getYear(GROUP_4_YEAR));
		}
		return article;
	}
}
