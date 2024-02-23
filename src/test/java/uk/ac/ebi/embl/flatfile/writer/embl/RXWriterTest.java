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
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class RXWriterTest extends EmblWriterTest {

  public void testWrite_Xref() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Publication publication = referenceFactory.createPublication();
    EntryFactory entryFactory = new EntryFactory();
    publication.addXRef(entryFactory.createXRef("UniProtKB", "A00001"));
    publication.addXRef(entryFactory.createXRef("UniProtKB", "A00002"));
    StringWriter writer = new StringWriter();
    assertTrue(new RXWriter(entry, publication, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("RX   UniProtKB; A00001.\n" + "RX   UniProtKB; A00002.\n", writer.toString());
  }

  public void testWrite_NoXref() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Publication publication = referenceFactory.createPublication();
    StringWriter writer = new StringWriter();
    assertFalse(new RXWriter(entry, publication, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("", writer.toString());
  }
}
