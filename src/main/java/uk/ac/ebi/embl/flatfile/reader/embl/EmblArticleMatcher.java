/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

public class EmblArticleMatcher extends FlatFileMatcher {

  public EmblArticleMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  private static final Pattern PATTERN =
      Pattern.compile(
          // journal volume (issue), first page-last page (year)
          "^"
              + "\\s*"
              + "([^\\:]+)"
              + // journal + volume + issue
              "\\s*"
              + "\\:"
              + "\\s*"
              + "(?:([^\\(\\-\\)]+)?"
              + // first page
              "\\s*"
              + "(?:-\\s*([^\\(\\)\\-]+)))?"
              + // last page
              "\\s*"
              + "(?:\\(\\s*(\\d+)\\s*\\)\\s*)?"
              + // year
              ".*$");

  private static final int GROUP_JOURNAL_VOLUME_ISSUE = 1;
  private static final int GROUP_FIRST_PAGE = 2;
  private static final int GROUP_LAST_PAGE = 3;
  private static final int GROUP_YEAR = 4;

  public Article getArticle(Publication publication) {
    Article article = null;
    if (publication != null) {
      article = (new ReferenceFactory()).createArticle(publication);
      article.setOrigin(publication.getOrigin());
    } else {
      article = (new ReferenceFactory()).createArticle();
    }
    EmblArticleIssueMatcher matcher = new EmblArticleIssueMatcher(null);
    if (matcher.match(getString(GROUP_JOURNAL_VOLUME_ISSUE))) {
      article.setJournal(matcher.getJournal());
      article.setVolume(matcher.getVolume());
      article.setIssue(matcher.getIssue());
    } else {
      String journal_volume_issue = getString(GROUP_JOURNAL_VOLUME_ISSUE);
      if (journal_volume_issue.matches("^.*(\\s*\\([^\\(\\)]+\\)\\s*)$")) {
        article.setIssue(
            journal_volume_issue.substring(
                journal_volume_issue.lastIndexOf('(') + 1, journal_volume_issue.length() - 1));
        String journal_volume = StringUtils.replace(journal_volume_issue, article.getIssue(), "");
        article.setVolume(
            journal_volume.substring(
                journal_volume.lastIndexOf(' ') + 1, journal_volume.lastIndexOf('(')));
        article.setJournal(journal_volume.substring(0, journal_volume.lastIndexOf(' ')));
      } else if (journal_volume_issue.matches(".*\\d$")) {
        article.setVolume(
            journal_volume_issue.substring(journal_volume_issue.lastIndexOf(' ') + 1));
        article.setJournal(
            journal_volume_issue.substring(0, journal_volume_issue.lastIndexOf(' ')));
      } else {
        article.setJournal(journal_volume_issue);
      }
    }
    String firstPage = FlatFileUtils.shrink(getString(GROUP_FIRST_PAGE));
    String lastPage = FlatFileUtils.shrink(getString(GROUP_LAST_PAGE));
    if (firstPage != null && firstPage.endsWith(".")) {
      firstPage = StringUtils.removeEnd(firstPage, ".");
    }
    if (lastPage != null && lastPage.endsWith(".")) {
      lastPage = StringUtils.removeEnd(lastPage, ".");
    }
    article.setFirstPage(firstPage);
    article.setLastPage(lastPage);
    article.setYear(getYear(GROUP_YEAR));
    return article;
  }
}
