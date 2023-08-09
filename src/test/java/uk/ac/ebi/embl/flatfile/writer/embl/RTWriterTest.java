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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class RTWriterTest extends EmblWriterTest {

  public void testWrite_ShortTitle() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Publication publication = referenceFactory.createPublication();
    publication.setTitle("NISC Comparative Sequencing Initiative");
    StringWriter writer = new StringWriter();
    assertTrue(new RTWriter(entry, publication, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("RT   \"NISC Comparative Sequencing Initiative\";\n", writer.toString());
  }

  public void testWrite_EmptyTitle() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Publication publication = referenceFactory.createPublication();
    publication.setTitle("");
    StringWriter writer = new StringWriter();
    assertTrue(new RTWriter(entry, publication, wrapType).write(writer));
    assertEquals("RT   ;\n", writer.toString());
  }

  public void testWrite_NullTitle() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Publication publication = referenceFactory.createPublication();
    StringWriter writer = new StringWriter();
    assertTrue(new RTWriter(entry, publication, wrapType).write(writer));
    assertEquals("RT   ;\n", writer.toString());
  }

  public void testWrite_LongTitle() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Publication publication = referenceFactory.createPublication();
    publication.setTitle(
        "NISC Comparative Sequencing Initiative NISC Comparative Sequencing Initiative NISC Comparative Sequencing Initiative");
    StringWriter writer = new StringWriter();
    assertTrue(new RTWriter(entry, publication, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "RT   \"NISC Comparative Sequencing Initiative NISC Comparative Sequencing\n"
            + "RT   Initiative NISC Comparative Sequencing Initiative\";\n",
        writer.toString());
  }
}
