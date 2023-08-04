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
package uk.ac.ebi.embl.fasta.writer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

public class FastaFileWriterTest {
  @Test
  public void testwrite_entry() throws IOException {
    String output =
        ">EM_XXX:ad0897987 ad0897987.1 STD:adfddfgghhjkll\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n";
    StringWriter writer = new StringWriter();
    Entry entry = new EntryFactory().createEntry();
    entry.setPrimaryAccession("ad0897987");
    entry.setDivision("XXX");
    entry.setDescription(new Text("adfddfgghhjkll"));
    entry.setDataClass(Entry.STD_DATACLASS);
    Sequence sequence = new SequenceFactory().createSequence();
    sequence.setSequence(
        ByteBuffer.wrap(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                .getBytes()));
    sequence.setAccession("ad0897987");
    sequence.setVersion(1);
    entry.setSequence(sequence);
    FastaFileWriter fileWriter = new FastaFileWriter(entry, writer);
    fileWriter.write();
    assertEquals(output, writer.toString());
  }

  @Test
  public void testwrite_analysisEntry() throws IOException {
    String output =
        ">EM_XXX:ad0897987 ad0897987.1 STD:adfddfgghhjkll\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n";
    StringWriter writer = new StringWriter();
    Entry entry = new EntryFactory().createEntry();
    entry.setPrimaryAccession("ad0897987");
    entry.setDivision("XXX");
    entry.setDescription(new Text("adfddfgghhjkll"));
    entry.setDataClass(Entry.STD_DATACLASS);
    Sequence sequence = new SequenceFactory().createSequence();
    sequence.setSequence(
        ByteBuffer.wrap(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                .getBytes()));
    sequence.setAccession("ad0897987");
    sequence.setVersion(1);
    entry.setSequence(sequence);
    FastaFileWriter fileWriter = new FastaFileWriter(entry, writer);
    fileWriter.write();
    assertEquals(output, writer.toString());
  }
}
