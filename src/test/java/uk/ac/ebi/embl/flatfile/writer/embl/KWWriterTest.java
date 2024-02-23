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
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.Text;

public class KWWriterTest extends EmblWriterTest {

  public void testWrite_Keywords() throws IOException {
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    entry.addKeyword(new Text("keyword"));
    StringWriter writer = new StringWriter();
    assertTrue(new KWWriter(entry, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "KW   keyword; keyword; keyword; keyword; keyword; keyword; keyword; keyword;\n"
            + "KW   keyword; keyword; keyword; keyword; keyword; keyword; keyword; keyword.\n",
        writer.toString());
  }

  public void testWrite_NoKeywords() throws IOException {
    StringWriter writer = new StringWriter();
    assertTrue(new KWWriter(entry, wrapType).write(writer));
    assertEquals("KW   .\n", writer.toString());
  }
}
