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
import uk.ac.ebi.embl.api.entry.reference.Patent;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;

public class EmblPatentWriterTest extends EmblWriterTest {

  public void testWrite_Patent() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Patent patent =
        referenceFactory.createPatent(
            "Isolation, description and sequencing of a novel species (of an established genus): Bacillus samanii sp nov. from snow",
            "EPO",
            "238993",
            "A",
            3,
            FlatFileDateUtils.getDay("10-SEP-1998"));
    patent.addApplicant("BAYER AG");
    patent.addApplicant("GOOGLE AG");
    patent.addApplicant(
        "BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH AG");
    StringWriter writer = new StringWriter();
    assertTrue(new RLWriter(entry, patent, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "RL   Patent number EPO238993-A/3, 10-SEP-1998.\n"
            + "RL   BAYER AG;\n"
            + "RL   GOOGLE AG;\n"
            + "RL   BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH BLAH\n"
            + "RL   BLAH BLAH BLAH BLAH BLAH AG.\n",
        writer.toString());
  }

  public void testWrite_EmptyPatent() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Patent patent = referenceFactory.createPatent();
    StringWriter writer = new StringWriter();
    assertTrue(new RLWriter(entry, patent, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("RL   Patent number -/, .\n", writer.toString());
  }
}
