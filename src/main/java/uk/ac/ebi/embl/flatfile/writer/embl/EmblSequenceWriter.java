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
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the sequence lines. */
public class EmblSequenceWriter extends FlatFileWriter {

  private final Sequence sequence;

  private long crc;

  public EmblSequenceWriter(Entry entry, Sequence sequence) {
    super(entry);
    this.sequence = sequence;
  }

  public EmblSequenceWriter(Entry entry, Sequence sequence, long crc) {
    this(entry, sequence);
    this.crc = crc;
  }

  public boolean write(Writer writer) throws IOException {
    if (sequence == null || sequence.getLength() == 0 || sequence.getSequenceByte() == null) {
      return false;
    }

    int GCount = 0;
    int CCount = 0;
    int ACount = 0;
    int TCount = 0;
    int OtherCount = 0;
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

    writer.write(EmblTag.SQ_TAG);
    writer.write("   ");
    writer.write("Sequence ");
    writer.write(String.valueOf(sequence.getLength()));
    String dataclass = entry.getDataClass();
    if (dataclass != null && dataclass.equals(Entry.PRT_DATACLASS)) {
      writer.write(" AA;\n");
    } else {
      writer.write(" BP; ");
      writer.write(Integer.toString(ACount));
      writer.write(" A; ");
      writer.write(Integer.toString(CCount));
      writer.write(" C; ");
      writer.write(Integer.toString(GCount));
      writer.write(" G; ");
      writer.write(Integer.toString(TCount));
      writer.write(" T; ");
      writer.write(Integer.toString(OtherCount));
      writer.write(" other;");

      if (crc != 0) {
        writer.write(" " + crc + " CRC32;");
      }
      writer.write("\n");
    }

    int blockNumber = 0;
    int charNumber = 0;
    int lineNumber = 1;

    StringBuffer line = new StringBuffer();
    for (byte base : sequenceByte) {
      if (charNumber == 10) {
        line.append(" ");
        blockNumber++;
        charNumber = 0;
      }
      if (blockNumber == 6) {
        writer.write(EmblPadding.SEQUENCE_PADDING);
        if (dataclass != null && dataclass.equals(Entry.PRT_DATACLASS)) {
          writer.write(line.toString().toUpperCase());
        } else {
          writer.write(line.toString());
        }
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

      line.append((char) base);
      charNumber++;
    }

    writer.write(EmblPadding.SEQUENCE_PADDING);
    if (dataclass != null && dataclass.equals(Entry.PRT_DATACLASS)) {
      writer.write(line.toString().toUpperCase());
    } else {
      writer.write(line.toString());
    }

    String baseCount = Integer.toString((60 * (lineNumber - 1) + (10 * blockNumber) + charNumber));
    int baseCountPadding = 76 - line.length() - baseCount.length();
    for (int j = 1; j < baseCountPadding; j++) {
      writer.write(" ");
    }
    writer.write(baseCount + "\n");
    return true;
  }
}
