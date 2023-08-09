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
import uk.ac.ebi.embl.api.entry.reference.Person;

/** Flat file writer for the book editor lines. */
public class BookEditorWriter extends FlatFileWriter {

  private final Book book;

  public BookEditorWriter(
      Entry entry, Book book, WrapType wrapType, String firstLineHeader, String header) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_COMMA);
    this.book = book;
    this.firstLineHeader = firstLineHeader;
    this.header = header;
  }

  private final String firstLineHeader;
  private final String header;

  public boolean write(Writer writer) throws IOException {
    StringBuilder block = new StringBuilder();
    block.append("(in) ");
    if (book.getEditors() != null && book.getEditors().size() > 0) {
      boolean firstEditor = true;
      for (Person editor : book.getEditors()) {
        if (!firstEditor) {
          block.append(", ");
        } else {
          firstEditor = false;
        }
        if (!isBlankString(editor.getSurname())) {
          block.append(editor.getSurname().trim());
        }
        if (!isBlankString(editor.getFirstName())) {
          block.append(" ");
          block.append(editor.getFirstName().trim());
        }
      }
      block.append(" (Eds.);");
    }
    writeBlock(writer, firstLineHeader, header, block.toString());
    return true;
  }
}
