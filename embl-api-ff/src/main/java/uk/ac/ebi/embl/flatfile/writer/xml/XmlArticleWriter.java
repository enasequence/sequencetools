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
package uk.ac.ebi.embl.flatfile.writer.xml;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblArticleWriter;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlArticleWriter {

	private Entry entry;
    private Article article;

	public XmlArticleWriter(Entry entry, Article article) {
		this.entry = entry;
		this.article = article;
	}

    public boolean write(SimpleXmlWriter writer) throws IOException {
    	StringWriter stringWriter = new StringWriter();
    	(new EmblArticleWriter(entry, article, WrapType.EMBL_WRAP, "")).write(stringWriter);
    	writer.writeElementText(stringWriter.toString());
    	return true;
	}
}
