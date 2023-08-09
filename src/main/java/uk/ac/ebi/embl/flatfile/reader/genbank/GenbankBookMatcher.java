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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.util.Date;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.entry.reference.Book;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.FlatFileLineReader;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;

public class GenbankBookMatcher extends FlatFileMatcher {

  public GenbankBookMatcher(FlatFileLineReader reader) {
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
              + "([^\\;]+)?"
              + // book title
              "\\s*\\;\\s*"
              + "([^\\(\\)\\.]+)?"
              + // publisher
              "\\s*\\(\\s*"
              + "(\\d+)?"
              + // year
              "\\s*\\)\\s*"
              + ".*$");

  private static final int GROUP_EDITORS = 1;
  private static final int GROUP_BOOK_TITLE_PAGE = 2;
  private static final int GROUP_PUBLISHER = 3;
  private static final int GROUP_YEAR = 4;

  public Book getBook(Publication publication) {
    Book book = null;
    if (publication != null) {
      book = (new ReferenceFactory()).createBook(publication);
      book.setOrigin(publication.getOrigin());
    } else {
      book = (new ReferenceFactory()).createBook();
    }
    String editors = getString(GROUP_EDITORS);
    for (String editor : FlatFileUtils.split(editors, ", ")) {
      GenbankPersonMatcher personMatcher = new GenbankPersonMatcher(getReader());
      if (!personMatcher.match(editor)) {
        error("RL.15", editor);
      } else {
        book.addEditor(personMatcher.getPerson());
      }
    }
    String bookTitle = getString(GROUP_BOOK_TITLE_PAGE);
    if (bookTitle == null) {
      error("RL.16");
    } else {
      String firstPage = "0";
      String lastPage = "0";
      String[] arr = bookTitle.split(":");
      if (arr.length == 2) {
        if (arr[1].contains("-")) {
          String[] pageRange = arr[1].split("-");
          String fp = pageRange[0].trim();
          if (pageRange.length == 2) {
            if (StringUtils.isNumeric(fp) && StringUtils.isNumeric(pageRange[1].trim())) {
              firstPage = fp;
              lastPage = pageRange[1].trim();
              bookTitle = arr[0].trim();
            }
          } else if (StringUtils.isNumeric(fp)) {
            firstPage = fp;
            bookTitle = arr[0].trim();
          }
        } else {
          if (StringUtils.isNumeric(arr[1].trim())) {
            firstPage = arr[1].trim();
            bookTitle = arr[0].trim();
          }
        }
      }
      book.setFirstPage(firstPage);
      book.setLastPage(lastPage);
      if (StringUtils.isBlank(bookTitle)) {
        error("RL.16");
      } else {
        book.setBookTitle(bookTitle);
      }
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
