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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;

public class GenbankArticleWriterTest extends GenbankWriterTest {

  public void testWrite_Journal() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Article article =
        referenceFactory.createArticle(
            "Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor",
            "J. Biol. Chem.");
    article.setVolume("273");
    article.setIssue("48");
    article.setFirstPage("32281");
    article.setLastPage("32287");
    article.setYear(FlatFileDateUtils.getDay("10-SEP-1998"));
    StringWriter writer = new StringWriter();
    assertTrue(new JournalWriter(entry, article, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("  JOURNAL   J. Biol. Chem. 273(48),32281-32287(1998)\n", writer.toString());
  }

  public void testWrite_EmptyJournal() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Article article = referenceFactory.createArticle(null, null);
    StringWriter writer = new StringWriter();
    assertTrue(new JournalWriter(entry, article, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("  JOURNAL    In press\n", writer.toString());
  }
}
