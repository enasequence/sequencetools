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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.Writer;

import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Article;

/** Flat file writer for the journal article lines.
 */
public class GenbankArticleWriter extends FlatFileWriter {

	private Article article;

	public GenbankArticleWriter(Entry entry, Article article, WrapType wrapType) {
		super(entry, wrapType);
		setWrapChar(WrapChar.WRAP_CHAR_SPACE);
		this.article = article;
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
		boolean inPress = false;
		if (isBlankString(article.getFirstPage()) &&
			isBlankString(article.getLastPage())) {
			inPress = true;						
		}
		else {
			block.append(",");
			if (!isBlankString(article.getFirstPage())) {
				block.append(article.getFirstPage());	
			}
			if (!isBlankString(article.getLastPage())) {
				block.append("-");
				block.append(article.getLastPage());
			}
		}
		if (article.getYear() != null) {
			block.append("(");
			block.append(YEAR_FORMAT.format(article.getYear()).toUpperCase());
			block.append(")");
		}
		if (inPress) {
			block.append(" In press");
		}
		writeBlock(writer, GenbankPadding.JOURNAL_PADDING, 
				GenbankPadding.BLANK_PADDING, block.toString());
		return true;
	}
}
