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
import java.io.Writer;
import java.util.Map;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the sequence lines. */
public class EmblSequenceWriter extends FlatFileWriter {

  private static final int STREAM_CHUNK = 8192;

  private final Sequence sequence;

  private long crc;

  private final Reader reader;
  private final long totalBases;
  private final Map<Character, Long> baseCounts;

  public EmblSequenceWriter(Entry entry, Sequence sequence) {
    super(entry);
    this.sequence = sequence;
    this.reader = null;
    this.totalBases = 0;
    this.baseCounts = null;
  }

  public EmblSequenceWriter(Entry entry, Sequence sequence, long crc) {
    this(entry, sequence);
    this.crc = crc;
  }

  public EmblSequenceWriter(
      Entry entry, long totalBases, Map<Character, Long> baseCounts, Reader reader) {
    this(entry, totalBases, baseCounts, reader, 0);
  }

  public EmblSequenceWriter(
      Entry entry, long totalBases, Map<Character, Long> baseCounts, Reader reader, long crc) {
    super(entry);
    this.sequence = null;
    this.totalBases = totalBases;
    this.baseCounts = baseCounts;
    this.reader = reader;
    this.crc = crc;
  }

  public boolean write(Writer writer) throws IOException {
    if (reader != null) {
      return writeStreaming(writer);
    }

    if (sequence == null || sequence.getLength() == 0 || sequence.getSequenceByte() == null) {
      return false;
    }

    long GCount = 0;
    long CCount = 0;
    long ACount = 0;
    long TCount = 0;
    long OtherCount = 0;
    byte[] sequenceByte = sequence.getSequenceByte();

    for (int i = 0; i < sequenceByte.length; i++) {
      char base = (char) (sequenceByte[i]); // & 0xFF);

      switch (base) {
        case 'g':
          GCount++;
          break;
        case 'c':
          CCount++;
          break;
        case 'a':
          ACount++;
          break;
        case 't':
          TCount++;
          break;
        default:
          OtherCount++;
          break;
      }
    }

    boolean protein =
        writeHeader(writer, sequence.getLength(), ACount, CCount, GCount, TCount, OtherCount);

    LineFormatter formatter = new LineFormatter(writer, protein);
    for (byte base : sequenceByte) {
      formatter.accept((char) base);
    }
    formatter.finish();
    return true;
  }

  private boolean writeStreaming(Writer writer) throws IOException {
    if (totalBases == 0) {
      return false;
    }

    long aCount = countFor('a');
    long cCount = countFor('c');
    long gCount = countFor('g');
    long tCount = countFor('t');
    long otherCount = totalBases - aCount - cCount - gCount - tCount;

    boolean protein = writeHeader(writer, totalBases, aCount, cCount, gCount, tCount, otherCount);

    LineFormatter formatter = new LineFormatter(writer, protein);
    char[] buffer = new char[STREAM_CHUNK];
    int n;
    while ((n = reader.read(buffer)) != -1) {
      for (int i = 0; i < n; i++) {
        formatter.accept(buffer[i]);
      }
    }
    formatter.finish();
    return true;
  }

  private long countFor(char base) {
    Long value = baseCounts.get(base);
    return value == null ? 0L : value;
  }

  /** Writes the {@code SQ} header line. Returns whether the entry is a protein dataclass. */
  private boolean writeHeader(
      Writer writer,
      long length,
      long aCount,
      long cCount,
      long gCount,
      long tCount,
      long otherCount)
      throws IOException {
    writer.write(EmblTag.SQ_TAG);
    writer.write("   ");
    writer.write("Sequence ");
    writer.write(String.valueOf(length));
    String dataclass = entry.getDataClass();
    boolean protein = dataclass != null && dataclass.equals(Entry.PRT_DATACLASS);
    if (protein) {
      writer.write(" AA;\n");
    } else {
      writer.write(" BP; ");
      writer.write(Long.toString(aCount));
      writer.write(" A; ");
      writer.write(Long.toString(cCount));
      writer.write(" C; ");
      writer.write(Long.toString(gCount));
      writer.write(" G; ");
      writer.write(Long.toString(tCount));
      writer.write(" T; ");
      writer.write(Long.toString(otherCount));
      writer.write(" other;");

      if (crc != 0) {
        writer.write(" " + crc + " CRC32;");
      }
      writer.write("\n");
    }
    return protein;
  }

  /**
   * Emits the sequence body: groups of 10 bases, 6 blocks per 60-base line, each line padded by
   * {@link EmblPadding#SEQUENCE_PADDING} and terminated with a right-aligned position counter.
   * Shared by the {@code byte[]} and streaming write paths so their output cannot drift.
   */
  private static final class LineFormatter {
    private final Writer writer;
    private final boolean protein;
    private final StringBuffer line = new StringBuffer();
    private int blockNumber = 0;
    private int charNumber = 0;
    private int lineNumber = 1;

    LineFormatter(Writer writer, boolean protein) {
      this.writer = writer;
      this.protein = protein;
    }

    void accept(char base) throws IOException {
      if (charNumber == 10) {
        line.append(" ");
        blockNumber++;
        charNumber = 0;
      }
      if (blockNumber == 6) {
        writer.write(EmblPadding.SEQUENCE_PADDING);
        writer.write(protein ? line.toString().toUpperCase() : line.toString());
        String baseCount = Integer.toString(60 * lineNumber);
        int baseCountPadding = 10 - baseCount.length();
        for (int j = 1; j < baseCountPadding; j++) {
          writer.write(" ");
        }
        writer.write(baseCount);
        writer.write("\n");
        line.delete(0, line.length());
        lineNumber++;
        blockNumber = 0;
      }
      line.append(base);
      charNumber++;
    }

    void finish() throws IOException {
      writer.write(EmblPadding.SEQUENCE_PADDING);
      writer.write(protein ? line.toString().toUpperCase() : line.toString());
      String baseCount = Integer.toString(60 * (lineNumber - 1) + (10 * blockNumber) + charNumber);
      int baseCountPadding = 76 - line.length() - baseCount.length();
      for (int j = 1; j < baseCountPadding; j++) {
        writer.write(" ");
      }
      writer.write(baseCount + "\n");
    }
  }
}
