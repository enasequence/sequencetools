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
		super(reader, ISSUE_FIRST_LAST_PAGE);
	}

    private static final  Pattern YEAR = Pattern.compile("^(.+)\\s*\\(\\s*(\\d{4})\\s*\\)\\s*\\.?\\s*$");

    private static final Pattern ISSUE_FIRST_LAST_PAGE = Pattern.compile("^(.+)\\s*\\(\\s*(\\S+)\\s*\\)\\,?\\s*([^\\s\\(\\)-\\,]+)\\s*\\-\\s*([^\\s\\(\\)\\-\\,]+)\\s*$");

	private static final Pattern ISSUE_FIRST_PAGE = Pattern.compile("^(.+)\\s*\\(\\s*([^\\(\\)-\\.,]+)\\s*\\)\\,?\\s*([^\\s\\(\\)\\-\\,]+)\\s*\\-?\\s*$");

	private static final  Pattern ISSUE = Pattern.compile("^(.+)\\s*\\(\\s*([^\\(\\)\\-\\.,]+)\\s*\\)\\s*$");

    private static final  Pattern VOL_FIRST_LAST_PAGE = Pattern.compile("^(.+\\s*\\,)\\s*([^\\s\\(\\)-\\,]+)\\s*\\-\\s*([^\\s\\(\\)\\-\\,]+)\\s*$");

	public Article getArticle(Publication publication, String block) {

	    int index = block.indexOf("In press");
	    if( index  > -1) {
	        block = block.substring(0,index).trim();
        }

		Article article = createArticle(publication);

		Matcher m = YEAR.matcher(block);

		if (m.matches()) {
			article.setYear(getYear(2, m));
			block = m.group(1).trim();
		}

		m = ISSUE_FIRST_LAST_PAGE.matcher(block);
		if (m.matches()) {
			article.setIssue(getString(2,m));
			article.setFirstPage(getString(3, m));
			article.setLastPage(getString(4, m));
			block = m.group(1).trim();
		} else {
			m = ISSUE_FIRST_PAGE.matcher(block);
			if(m.matches()) {
				article.setIssue(getString(2,m));
				article.setFirstPage(getString(3, m));
				block = m.group(1).trim();
			} else {
				m = ISSUE.matcher(block);
				if(m.matches()) {
					article.setIssue(getString(2,m));
					block = m.group(1).trim();
				} else {
                    m = VOL_FIRST_LAST_PAGE.matcher(block);
                    if(m.matches()) {
                        article.setFirstPage(getString(2, m));
                        article.setLastPage(getString(3, m));
                        block = m.group(1).trim();
                    }
                }
			}
		}

		if(hasVolume(block)) {
			int lIndex = block.lastIndexOf(" ");
			if (lIndex > -1) {
			    String vol = block.substring(lIndex).trim();
			    if(vol.length() > 0 && vol.endsWith(",")) {
			        vol = vol.substring(0,vol.length()-1);
                }
				article.setVolume(vol);
				block = block.substring(0, lIndex).trim();
			}
		}

		article.setJournal(getStr(block));

		return article;
	}

	private boolean hasVolume(String input) {
		return  (input.lastIndexOf(",") == input.length()-1) || (input.lastIndexOf(".") != input.length()-1);
	}

	private String getStr(String input) {
		input = input.trim();
		if((input.lastIndexOf(",") == input.length()-1) ){
			input = input.substring(0, input.length()-1);
		}
		return  input;
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
