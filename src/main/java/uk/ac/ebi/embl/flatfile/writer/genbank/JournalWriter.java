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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Book;
import uk.ac.ebi.embl.api.entry.reference.ElectronicReference;
import uk.ac.ebi.embl.api.entry.reference.Patent;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.reference.Thesis;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.BookWriter;
import uk.ac.ebi.embl.flatfile.writer.ElectronicReferenceWriter;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the JOURNAL lines. */
public class JournalWriter extends FlatFileWriter {

  private Publication publication;

  public JournalWriter(Entry entry, Publication publication, WrapType wrapType) {
    super(entry, wrapType);
    this.publication = publication;
  }

  public boolean write(Writer writer) throws IOException {
    if (publication == null) {
      return false;
    }
    boolean writeBlock = false;
    if (publication instanceof Book) {
      writeBlock |=
          new BookWriter(
                  entry,
                  (Book) publication,
                  wrapType,
                  GenbankPadding.JOURNAL_PADDING,
                  GenbankPadding.BLANK_PADDING)
              .write(writer);
    } else if (publication instanceof ElectronicReference) {
      writeBlock |=
          new ElectronicReferenceWriter(
                  entry,
                  (ElectronicReference) publication,
                  wrapType,
                  GenbankPadding.JOURNAL_PADDING,
                  GenbankPadding.BLANK_PADDING)
              .write(writer);
    } else if (publication instanceof Article) {
      writeBlock |= new GenbankArticleWriter(entry, (Article) publication, wrapType).write(writer);
    } else if (publication instanceof Patent) {
      writeBlock |= new GenbankPatentWriter(entry, (Patent) publication, wrapType).write(writer);
    } else if (publication instanceof Submission) {
      writeBlock |=
          new GenbankSubmissionWriter(entry, (Submission) publication, wrapType).write(writer);
    } else if (publication instanceof Thesis) {
      writeBlock |= new GenbankThesisWriter(entry, (Thesis) publication, wrapType).write(writer);
    } else if (publication instanceof Unpublished) {
      writeBlock |= new GenbankUnpublishedWriter(entry, wrapType).write(writer);
    }
    return writeBlock;
  }
}
