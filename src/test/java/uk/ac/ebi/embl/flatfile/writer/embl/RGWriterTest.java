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

public class RGWriterTest extends EmblWriterTest {

  public void testWrite_Consortium() throws IOException {
    Publication publication = (new ReferenceFactory()).createPublication();
    publication.setConsortium("consortium");
    StringWriter writer = new StringWriter();
    assertTrue(new RGWriter(entry, publication, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("RG   consortium\n", writer.toString());
  }

  public void testWrite_NoConsortium() throws IOException {
    Publication publication = (new ReferenceFactory()).createPublication();
    publication.setConsortium(null);
    StringWriter writer = new StringWriter();
    assertFalse(new RGWriter(entry, publication, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("", writer.toString());
  }
}
