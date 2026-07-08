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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

/**
 * Verifies that a subclass can substitute a streaming sequence body for the in-memory {@code
 * byte[]} path via the {@link EmblEntryWriter#writeSequence} / {@link
 * EmblEntryWriter#isExpandedEntry} hooks, while the rest of {@link EmblEntryWriter#write} (ID / AC
 * / DE / feature table / terminator) is reused unchanged.
 */
public class EmblEntryWriterStreamingTest extends EmblWriterTest {

  private static final String SEQUENCE = "acgtacgtac".repeat(4); // 40 bases: 12 a, 12 c, 8 g, 8 t

  private static Map<Character, Long> baseCounts() {
    Map<Character, Long> counts = new HashMap<>();
    for (char base : SEQUENCE.toCharArray()) {
      counts.merge(base, 1L, Long::sum);
    }
    return counts;
  }

  /** Builds an annotated entry; the sequence byte[] is populated only when {@code withBytes}. */
  private static Entry buildEntry(boolean withBytes) {
    EntryFactory entryFactory = new EntryFactory();
    FeatureFactory featureFactory = new FeatureFactory();
    LocationFactory locationFactory = new LocationFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();

    Entry entry = entryFactory.createEntry();
    entry.setPrimaryAccession("DP000153");
    entry.setDataClass("STD");
    entry.setDivision("MAM");
    entry.setDescription(new Text("Streaming equivalence test entry."));

    Sequence sequence = sequenceFactory.createSequence();
    if (withBytes) {
      sequence.setSequence(ByteBuffer.wrap(SEQUENCE.getBytes()));
    }
    sequence.setVersion(1);
    sequence.setTopology(Topology.LINEAR);
    sequence.setMoleculeType("genomic DNA");
    // Length feeds the ID line "n BP" field. The byte[] path derives it from the array; the
    // streaming path has no array, so the caller advertises it via Sequence.setLength(), which
    // getLength() now returns as a fallback.
    sequence.setLength(SEQUENCE.length());
    entry.setSequence(sequence);

    Feature source = featureFactory.createSourceFeature();
    Join<Location> sourceLocations = new Join<>();
    sourceLocations.addLocation(locationFactory.createLocalRange(1L, (long) SEQUENCE.length()));
    source.setLocations(sourceLocations);
    entry.addFeature(source);

    Feature gene = featureFactory.createFeature("gene");
    Join<Location> geneLocations = new Join<>();
    geneLocations.addLocation(locationFactory.createLocalRange(5L, 20L));
    gene.setLocations(geneLocations);
    gene.addQualifier(qualifierFactory.createQualifier("gene", "example"));
    entry.addFeature(gene);

    return entry;
  }

  private static String writeBytePath(Entry entry) throws IOException {
    StringWriter writer = new StringWriter();
    EmblEntryWriter entryWriter = new EmblEntryWriter(entry);
    entryWriter.setShowAcStartLine(false);
    assertTrue(entryWriter.write(writer));
    return writer.toString();
  }

  private static String writeStreamingPath(Entry entry) throws IOException {
    StringWriter writer = new StringWriter();
    StreamingEmblEntryWriter entryWriter =
        new StreamingEmblEntryWriter(entry, SEQUENCE.length(), baseCounts(), SEQUENCE);
    entryWriter.setShowAcStartLine(false);
    assertTrue(entryWriter.write(writer));
    assertTrue("writeSequence hook was not invoked by write()", entryWriter.sequenceHookCalled);
    return writer.toString();
  }

  /**
   * The full flat file produced by streaming the sequence must be byte-identical to the in-memory
   * byte[] path: same ID line (including the "n BP" length, supplied via Sequence.setLength()),
   * same DE/KW, the feature table appearing before the SQ block, and the full SQ header + sequence
   * body. Exercises the writeSequence override hook, the isExpandedEntry feature-ordering fix, and
   * the getLength() fallback together.
   */
  public void testStreamingProducesIdenticalOutputToBytePath() throws IOException {
    String bytePath = writeBytePath(buildEntry(true));
    String streamingPath = writeStreamingPath(buildEntry(false));
    assertEquals(bytePath, streamingPath);
    assertTrue("ID line should report the streamed length", streamingPath.contains("; 40 BP."));
  }

  /**
   * The SQ header and sequence body produced by streaming must be byte-identical to the byte[] path
   * (same base counts, same 60-base line layout, same position counter).
   */
  public void testStreamingSequenceBlockMatchesBytePath() throws IOException {
    String byteSq = writeBytePath(buildEntry(true)).replaceAll("(?s).*\\n(SQ   )", "$1");
    String streamSq = writeStreamingPath(buildEntry(false)).replaceAll("(?s).*\\n(SQ   )", "$1");
    assertEquals(byteSq, streamSq);
  }

  /**
   * Guards the feature-ordering fix: an expanded entry writes the feature table AFTER the CO
   * position (i.e. the FT block precedes the SQ block), and streaming keeps that ordering even
   * though no byte[] is present.
   */
  public void testFeatureTablePrecedesSequenceBlock() throws IOException {
    String streamingPath = writeStreamingPath(buildEntry(false));
    int featureIndex = streamingPath.indexOf("\nFT   ");
    int sequenceIndex = streamingPath.indexOf("\nSQ   ");
    assertTrue("feature table missing", featureIndex >= 0);
    assertTrue("sequence block missing", sequenceIndex >= 0);
    assertTrue("feature table must precede the sequence block", featureIndex < sequenceIndex);
  }

  private static final class StreamingEmblEntryWriter extends EmblEntryWriter {

    private final long totalBases;
    private final Map<Character, Long> baseCounts;
    private final String sequence;
    private boolean sequenceHookCalled;

    StreamingEmblEntryWriter(
        Entry entry, long totalBases, Map<Character, Long> baseCounts, String sequence) {
      super(entry);
      this.totalBases = totalBases;
      this.baseCounts = baseCounts;
      this.sequence = sequence;
    }

    @Override
    protected boolean isExpandedEntry() {
      // Streamed entries carry real sequence content but never populate the byte[].
      return totalBases > 0 && !entry.isNonExpandedCON();
    }

    @Override
    protected void writeSequence(Writer writer) throws IOException {
      sequenceHookCalled = true;
      Reader reader = new StringReader(sequence);
      writeStreamingSequence(writer, totalBases, baseCounts, reader, 0);
    }
  }
}
