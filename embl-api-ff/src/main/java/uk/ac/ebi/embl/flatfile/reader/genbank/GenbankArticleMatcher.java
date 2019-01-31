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
		super(reader, YEAR);
	}

    private static final  Pattern YEAR = Pattern.compile("^(.+)\\s*\\(\\s*(\\d{4})\\s*\\)\\s*\\.?,?\\s*$");

    private static final  Pattern FIRST_LAST_PAGE = Pattern.compile("^(.+)\\s*\\,\\s*([^\\(\\)]+)$");

    private static final  Pattern ISSUE = Pattern.compile("^(.+)\\s*\\(\\s*([^\\(\\)]+)\\s*\\)\\s*$");

    private static final  Pattern VOLUME = Pattern.compile("^[^\\s\\(\\)]+$");

	public Article getArticle(Publication publication, String block) {

        int inPressIndex = block.indexOf("In press");
        if (inPressIndex > -1) {
            block = block.substring(0, inPressIndex).trim();
        }

        Article article = createArticle(publication);

        Matcher m = YEAR.matcher(block);
        if (m.matches()) {
            article.setYear(getYear(2, m));
            block = m.group(1).trim();
        } else {
            return  null;
        }

        m = FIRST_LAST_PAGE.matcher(block);
        if (m.matches()) {
            String page = getString(2, m);
            if(page.length() <= 50) {
                if (page.contains("-")) {
                    String[] pageRange = page.split("-");
                    if (pageRange.length > 2) {
                        article.setFirstPage(pageRange[0]);
                        article.setLastPage(pageRange[1]);
                        for (int i = 2; i < pageRange.length; i++) {
                            article.setLastPage(article.getLastPage() + "-" + pageRange[i]);
                        }
                    } else if (pageRange.length == 2) {
                        article.setFirstPage(pageRange[0].trim());
                        article.setLastPage(pageRange[1].trim());
                    } else if (page.startsWith("-")) {
                        article.setLastPage(pageRange[0].trim());
                    } else {
                        article.setFirstPage(pageRange[0].trim());
                    }
                } else {
                    article.setFirstPage(page);
                }
                block = m.group(1).trim();
            }
        }

        m = ISSUE.matcher(block);
        if (m.matches()) {
            article = getVolumeAndJournal(m.group(1).trim(), article);
            if(StringUtils.isNotBlank(article.getVolume())) {
                article.setIssue(getString(2, m));
            } else {
                article.setJournal(block);
            }
        }

        if(StringUtils.isBlank(article.getJournal())) {
            article = getVolumeAndJournal(block, article);
        }

		return article.getJournal() == null ? null : article;
	}

	private Article getVolumeAndJournal(String input, Article article) {
	    String vol = null;
	    String journal = null;

        int lIndex = input.lastIndexOf(" ");
        //should have min 2 token
        if(lIndex > -1) {

            if (input.lastIndexOf(".") != input.length() - 1) {

                journal = input.substring(0, lIndex).trim();
                vol = input.substring(lIndex).trim();
                Matcher m = VOLUME.matcher(vol);
                if(m.matches() ) {
                    if (vol.length() > 0 && (vol.endsWith(",") || vol.endsWith("."))) {
                        vol = vol.substring(0, vol.length() - 1).trim();
                    }
                } else {
                    vol = null;
                }

            }
        }

        if(vol == null) {
            journal = input;
        }
        article.setVolume(vol);
        article.setJournal(journal);
        return article;
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
