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
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.EntryFactory;

public class PrimaryWriterTest extends GenbankWriterTest {

  public void testWrite_Assembly() throws IOException {
    EntryFactory entryFactory = new EntryFactory();
    entry.addAssembly(entryFactory.createAssembly("CD690073", 1, 43L, 486L, false, 1L, 444L));
    entry.addAssembly(entryFactory.createAssembly("AK126350", 1, 1L, 274L, false, 11L, 284L));
    StringWriter writer = new StringWriter();
    assertTrue(new PrimaryWriter(entry).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "PRIMARY     TPA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n"
            + "            1-444               CD690073.1         43-486\n"
            + "            11-284              AK126350.1         1-274\n",
        writer.toString());
  }

  public void testWrite_NoAssembly() throws IOException {
    entry.removeAssemblies();
    StringWriter writer = new StringWriter();
    assertFalse(new PrimaryWriter(entry).write(writer));
    // System.out.print(writer.toString());
    assertEquals("", writer.toString());
  }
}
