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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

public class EmblSequenceWriterStreamingTest extends EmblWriterTest {

  private static Map<Character, Long> countBases(String seq) {
    Map<Character, Long> counts = new HashMap<>();
    for (char ch : seq.toCharArray()) {
      switch (ch) {
        case 'a':
        case 'c':
        case 'g':
        case 't':
          counts.merge(ch, 1L, Long::sum);
          break;
        default:
          break;
      }
    }
    return counts;
  }

  private String runBytePath(String seq) throws IOException {
    entry.setSequence(new SequenceFactory().createSequenceByte(seq.getBytes()));
    StringWriter w = new StringWriter();
    assertTrue(new EmblSequenceWriter(entry, entry.getSequence()).write(w));
    return w.toString();
  }

  private String runStreamingPath(String seq) throws IOException {
    return runStreamingPath(seq, 0);
  }

  private String runStreamingPath(String seq, long crc) throws IOException {
    StringWriter w = new StringWriter();
    assertTrue(
        new EmblSequenceStreamWriter(
                entry, (long) seq.length(), countBases(seq), new StringReader(seq), crc)
            .write(w));
    return w.toString();
  }

  private String runBytePathWithCrc(String seq, long crc) throws IOException {
    entry.setSequence(new SequenceFactory().createSequenceByte(seq.getBytes()));
    StringWriter w = new StringWriter();
    assertTrue(new EmblSequenceWriter(entry, entry.getSequence(), crc).write(w));
    return w.toString();
  }

  /** 130 bases: 2 full 60-base lines + 10-base partial line. */
  public void testEquivalence_MultiLine() throws IOException {
    String seq = "acgt".repeat(32) + "ac";
    assertEquals(runBytePath(seq), runStreamingPath(seq));
  }

  /** 75 bases: 1 full 60-base line + 15-base partial line. */
  public void testEquivalence_PartialLine() throws IOException {
    String seq = "acgt".repeat(18) + "acg";
    assertEquals(runBytePath(seq), runStreamingPath(seq));
  }

  /** CRC32 field is emitted identically on both paths when crc != 0. */
  public void testEquivalence_WithCrc() throws IOException {
    String seq = "acgt".repeat(20);
    long crc = 0xDEADBEEFL;
    String byteOut = runBytePathWithCrc(seq, crc);
    String streamOut = runStreamingPath(seq, crc);
    assertEquals(byteOut, streamOut);
    assertTrue("CRC32 field missing from output", byteOut.contains(crc + " CRC32;"));
  }

  /**
   * 8400 bases (> STREAM_CHUNK=8192, not aligned to 60): exercises LineFormatter state carried
   * across buffer-read boundaries.
   */
  public void testEquivalence_CrossChunkBoundary() throws IOException {
    String seq = "acgt".repeat(2100); // 8400 chars, straddles the 8192-char buffer
    assertEquals(runBytePath(seq), runStreamingPath(seq));
  }

  /**
   * Sequence containing non-acgt bases: verifies the streaming path derives 'other' correctly via
   * subtraction (totalBases - a - c - g - t) rather than by counting from the reader.
   */
  public void testEquivalence_WithOtherBases() throws IOException {
    String seq = "acgtn".repeat(20); // 100 chars, 20 'n' (other)
    Map<Character, Long> counts = countBases(seq); // only counts a/c/g/t
    String byteOut = runBytePath(seq);
    StringWriter w = new StringWriter();
    assertTrue(
        new EmblSequenceStreamWriter(entry, (long) seq.length(), counts, new StringReader(seq), 0)
            .write(w));
    assertEquals(byteOut, w.toString());
  }

  /** writeStreamingSequence on EmblEntryWriter delegates to EmblSequenceStreamWriter. */
  public void testWriteStreamingSequence_MatchesDirectWriter() throws IOException {
    String seq = "acgt".repeat(20);
    Map<Character, Long> counts = countBases(seq);
    long crc = 0xDEADBEEFL;

    StringWriter direct = new StringWriter();
    new EmblSequenceStreamWriter(entry, (long) seq.length(), counts, new StringReader(seq), crc)
        .write(direct);

    StringWriter via = new StringWriter();
    new EmblEntryWriter(entry)
        .writeStreamingSequence(via, (long) seq.length(), counts, new StringReader(seq), crc);

    assertEquals(direct.toString(), via.toString());
  }

  /** Both paths return false and write nothing for a zero-length sequence. */
  public void testEquivalence_ZeroLength() throws IOException {
    entry.setSequence(null);
    StringWriter byteWriter = new StringWriter();
    assertFalse(new EmblSequenceWriter(entry, entry.getSequence()).write(byteWriter));
    assertEquals("", byteWriter.toString());

    StringWriter streamWriter = new StringWriter();
    assertFalse(
        new EmblSequenceStreamWriter(entry, 0L, new HashMap<>(), new StringReader(""))
            .write(streamWriter));
    assertEquals("", streamWriter.toString());
  }
}
