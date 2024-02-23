/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.util.Date;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.reference.Book;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

public class EmblBookMatcher extends FlatFileMatcher {

  public EmblBookMatcher(FlatFileLineReader reader) {
    super(reader, PATTERN);
  }

  /*
  RL   (in) Engberg J., Klenow H., Leick V. (Eds.);
     RL   SPECIFIC EUKARYOTIC GENES:117-132;
     RL   Munksgaard, Copenhagen (1979).
  */
  private static final Pattern PATTERN =
      Pattern.compile(
          "^\\s*\\(\\s*in\\s*\\)\\s*"
              + "([^\\(\\)\\;]+)?"
              + // editors
              "\\s*\\(\\s*Eds?\\s*\\.\\s*\\)\\s*;\\s*"
              + "([^\\:]+)?"
              + // book title
              "\\s*\\:\\s*"
              + "([^-;]+)?"
              + // first page
              "\\s*\\-\\s*"
              + "([^-;]+)?"
              + // last page
              "\\s*\\;\\s*"
              + "([^\\(\\)\\.]+)?"
              + // publisher
              "\\s*\\(\\s*"
              + "(\\d+)?"
              + // year
              "\\s*\\)\\s*"
              + ".*$");

  private static final int GROUP_EDITORS = 1;
  private static final int GROUP_BOOK_TITLE = 2;
  private static final int GROUP_FIRST_PAGE = 3;
  private static final int GROUP_LAST_PAGE = 4;
  private static final int GROUP_PUBLISHER = 5;
  private static final int GROUP_YEAR = 6;

  public Book getBook(Publication publication) {
    Book book = null;
    if (publication != null) {
      book = (new ReferenceFactory()).createBook(publication);
      book.setOrigin(publication.getOrigin());
    } else {
      book = (new ReferenceFactory()).createBook();
    }
    String editors = getString(GROUP_EDITORS);
    for (String editor : FlatFileUtils.split(editors, ",")) {
      EmblPersonMatcher emblPersonMatcher = new EmblPersonMatcher(getReader());
      if (!emblPersonMatcher.match(editor)) {
        error("RL.15", editor);
      } else {
        book.addEditor(emblPersonMatcher.getPerson());
      }
    }
    String bookTitle = getString(GROUP_BOOK_TITLE);
    if (bookTitle == null) {
      error("RL.16");
    } else {
      book.setBookTitle(bookTitle);
    }
    String firstPage = getString(GROUP_FIRST_PAGE);
    if (firstPage == null) {
      error("RL.17");
    } else {
      book.setFirstPage(firstPage);
    }
    String lastPage = getString(GROUP_LAST_PAGE);
    if (lastPage == null) {
      error("RL.18");
    } else {
      book.setLastPage(lastPage);
    }
    String publisher = getString(GROUP_PUBLISHER);
    if (publisher == null) {
      error("RL.19");
    } else {
      book.setPublisher(publisher);
    }
    Date year = getYear(GROUP_YEAR);
    if (year == null) {
      error("RL.20");
    } else {
      book.setYear(year);
    }
    return book;
  }
}
