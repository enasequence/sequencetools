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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Book;

/** Flat file writer for the book lines. */
public class BookWriter extends FlatFileWriter {

  private Book book;

  public BookWriter(Entry entry, Book book, WrapType wrapType, String header) {
    this(entry, book, wrapType, header, header);
  }

  public BookWriter(
      Entry entry, Book book, WrapType wrapType, String firstLineHeader, String header) {
    super(entry, wrapType);
    this.book = book;
    this.firstLineHeader = firstLineHeader;
    this.header = header;
  }

  private String firstLineHeader;
  private String header;

  public boolean write(Writer writer) throws IOException {
    boolean writeBlock = false;
    writeBlock |=
        new BookEditorWriter(entry, book, wrapType, firstLineHeader, header).write(writer);
    writeBlock |= new BookChapterWriter(entry, book, wrapType, header, header).write(writer);
    writeBlock |= new BookPublisherWriter(entry, book, wrapType, header, header).write(writer);
    return writeBlock;
  }
}
