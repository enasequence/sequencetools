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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the journal article lines. */
public class EmblArticleWriter extends FlatFileWriter {

  private final Article article;
  private String header = EmblPadding.RL_PADDING;

  public EmblArticleWriter(Entry entry, Article article, WrapType wrapType) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
    this.article = article;
  }

  public EmblArticleWriter(Entry entry, Article article, WrapType wrapType, String header) {
    super(entry, wrapType);
    setWrapChar(WrapChar.WRAP_CHAR_SPACE);
    this.article = article;
    this.header = header;
  }

  public boolean write(Writer writer) throws IOException {
    StringBuilder block = new StringBuilder();
    if (!isBlankString(article.getJournal())) {
      block.append(article.getJournal());
    }
    if (!isBlankString(article.getVolume())) {
      block.append(" ");
      block.append(article.getVolume());
    }
    if (!isBlankString(article.getIssue())) {
      block.append("(");
      block.append(article.getIssue());
      block.append(")");
    }
    block.append(":");
    if (!isBlankString(article.getFirstPage())) {
      block.append(article.getFirstPage());
    }
    if (!isBlankString(article.getLastPage())) {
      block.append("-");
      block.append(article.getLastPage());
    }
    if (article.getYear() != null) {
      block.append("(");
      block.append(FlatFileDateUtils.formatAsYear(article.getYear()));
      block.append(")");
    }
    block.append(".");
    writeBlock(writer, header, block.toString());
    return true;
  }
}
