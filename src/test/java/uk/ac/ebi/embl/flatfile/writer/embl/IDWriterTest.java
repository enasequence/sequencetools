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
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

public class IDWriterTest extends EmblWriterTest {

  public void testWrite_All() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
    entry.setIdLineSequenceLength(2);
    entry.setPrimaryAccession("DP000153");
    entry.getSequence().setVersion(1);
    entry.getSequence().setTopology(Topology.LINEAR);
    entry.getSequence().setMoleculeType("genomic RNA");
    entry.setDataClass("CON");
    entry.setDivision("MAM");
    StringWriter writer = new StringWriter();
    assertTrue(new IDWriter(entry).write(writer));
    // System.out.print(writer.toString());
    assertEquals("ID   DP000153; SV 1; linear; genomic RNA; CON; MAM; 2 BP.\n", writer.toString());
  }

  public void testWrite_XXX() throws IOException {
    entry.setPrimaryAccession(null);
    entry.setVersion(null);
    entry.setDataClass(null);
    entry.setDivision(null);
    StringWriter writer = new StringWriter();
    assertTrue(new IDWriter(entry).write(writer));
    // System.out.print(writer.toString());
    assertEquals("ID   XXX; SV XXX; XXX; XXX; XXX; XXX; .\n", writer.toString());
  }
}
