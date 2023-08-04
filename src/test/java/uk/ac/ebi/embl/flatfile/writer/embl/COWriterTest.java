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
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

public class COWriterTest extends EmblWriterTest {

  public void testWrite_Contigs() throws IOException {
    LocationFactory locationFactory = new LocationFactory();
    entry.setSequence(new SequenceFactory().createSequence());
    entry.getSequence().addContig(locationFactory.createRemoteRange("A00001", 1, 1L, 210L));
    entry.getSequence().addContig(locationFactory.createUnknownGap(100L));
    entry.getSequence().addContig(locationFactory.createGap(100L));
    entry.getSequence().addContig(locationFactory.createRemoteRange("A00001", 1, 1L, 210L));
    entry.getSequence().addContig(locationFactory.createRemoteRange("A00001", 1, 1L, 210L));
    entry.getSequence().addContig(locationFactory.createRemoteRange("A00001", 1, 1L, 210L));
    entry.getSequence().addContig(locationFactory.createRemoteRange("A00001", 1, 1L, 210L));
    entry.getSequence().addContig(locationFactory.createRemoteRange("A00001", 1, 1L, 210L));
    StringWriter writer = new StringWriter();
    assertTrue(new COWriter(entry, wrapType).write(writer));
    // System.out.println(writer.toString());
    assertEquals(
        "CO   join(A00001.1:1..210,gap(unk100),gap(100),A00001.1:1..210,A00001.1:1..210,\n"
            + "CO   A00001.1:1..210,A00001.1:1..210,A00001.1:1..210)\n",
        writer.toString());
  }

  public void testWrite_NoContigs() throws IOException {
    StringWriter writer = new StringWriter();
    entry.setSequence(new SequenceFactory().createSequence());
    assertFalse(new COWriter(entry, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("", writer.toString());
  }
}
