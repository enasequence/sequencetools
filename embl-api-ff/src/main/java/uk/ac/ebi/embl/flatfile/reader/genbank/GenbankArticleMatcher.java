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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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
		super(reader, DEFAULT_PATTERN);
	}
	// journal volume (issue), first page-last page (year)
	private static final Pattern DEFAULT_PATTERN = Pattern.compile("(?:^(.+)\\,\\s*([^\\(\\-\\)]+)?\\s*(?:-\\s*([^\\(\\)\\-\\.]+))?\\s*(?:\\(\\s*(\\d+)\\s*\\)\\s*)?.*$)" );
	// journal volume (issue) (year) In press
	private static final Pattern JOURNAL_VOl_ISSUE_YEAR = Pattern.compile("(?:^(.+)\\s*(?:\\(\\s*(\\d+)\\s*\\)\\s*)\\s*In\\s*press.*$)");
	//Journal (year), In press
	private static final Pattern JOURNAL_YEAR_IN_PRESS = Pattern.compile("^(.+)\\((\\d{4})\\).+In\\s+press$");
	//journal volume (year)
	private static final Pattern JOURNAL_VOL_YEAR = Pattern.compile("((?:^(.+)\\s*(?:\\((\\d{4})\\))\\s*$))");
	// journal volume (issue) In Press
	private static final Pattern JOURNAL_VOl_ISSUE_IN_PRESS = Pattern.compile("(?:^(.+)\\s*In\\s*press.*$)");

	private static final  Pattern PAGE = Pattern.compile(".*\\d+.*");

	public Article getArticle(Publication publication, String block) {

		Matcher m = DEFAULT_PATTERN.matcher(block);

		if (m.matches()) {
			String firstPage = FlatFileUtils.shrink(getString(2, m));
			if(null == firstPage || PAGE.matcher(firstPage).matches() ) {
				Article article = createArticle(publication);
				String journal = FlatFileUtils.shrink(getString(1, m));
				article.setJournal(journal);
				String lastPage = FlatFileUtils.shrink(getString(3, m));
				article.setFirstPage(firstPage);
				article.setLastPage(lastPage);
				article.setYear(getYear(4, m));
				return article;
			}
		}

		m = JOURNAL_VOl_ISSUE_YEAR.matcher(block);
	 	if (m.matches()) {
			return  parseArticle(publication, m, 1, 2);
		}

		m = JOURNAL_YEAR_IN_PRESS.matcher(block);
		if(m.matches()){
			return  parseArticle(publication, m, 1, 2);
		}

		m = JOURNAL_VOL_YEAR.matcher(block);
		if(m.matches()){
			return  parseArticle(publication, m, 2, 3);
		}

		m = JOURNAL_VOl_ISSUE_IN_PRESS.matcher(block);
		if(m.matches()){
			Article article = createArticle(publication);
			String journal = FlatFileUtils.shrink(getString(1, m));
			article.setJournal(journal);
			return article;
		}

		return null;
	}

	private Article parseArticle(Publication publication, Matcher m , int first, int second) {
		Article article = createArticle(publication);
		String journal = FlatFileUtils.shrink(getString(first, m));
		article.setJournal(journal);
		article.setYear(getYear(second, m));
		return  article;
	}

	private Article createArticle(Publication publication) {
		Article article;
		if (publication != null) {
			article = (new ReferenceFactory()).createArticle(publication);
			article.setOrigin(publication.getOrigin());
		}
		else {
			article = (new ReferenceFactory()).createArticle();
		}
		return  article;
	}

}
