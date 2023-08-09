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
package uk.ac.ebi.embl.flatfile.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;

/** Reader for the flat file sequence. */
public class SequenceReader extends FlatFileLineReader {

  private static final int MAX_SEQUENCE_LENGTH = Integer.MAX_VALUE - 100;
  private static final byte SUBSTITUTION = 26;
  private ByteBuffer sequence = ByteBuffer.allocate(4096);

  public SequenceReader(LineReader lineReader) {
    super(lineReader);
  }

  @Override
  protected void readLines() throws IOException {

    if (lineReader.getReaderOptions() != null && lineReader.getReaderOptions().isIgnoreSequence()) {
      while (!lineReader.isNextTag() && lineReader.isNextLine()) {
        lineReader.readLine();
      }
      return;
    }

    int firstLineNumber = lineReader.getCurrentLineNumber();
    int lastLineNumber;

    String molType = getCache().getMolType();

    final Consumer<byte[]> compactor =
        Entry.PROTEIN.equals(molType) ? this::compactProteinSequence : this::compactDNASequence;

    sequence.rewind();
    while (true) {
      lastLineNumber = lineReader.getCurrentLineNumber();
      String line = lineReader.getCurrentLine();

      if (sequence.remaining() < line.length()) {
        if (sequence.capacity() >= MAX_SEQUENCE_LENGTH) {
          throw new IOException(
              "Maximum supported sequence size " + MAX_SEQUENCE_LENGTH + " exceeded");
        }

        byte[] biggerArray =
            Arrays.copyOf(
                sequence.array(),
                Math.min(
                    MAX_SEQUENCE_LENGTH,
                    Math.max(
                        (int) (sequence.capacity() * 1.7), sequence.capacity() + line.length())));

        int position = sequence.position();
        sequence = ByteBuffer.wrap(biggerArray);
        sequence.position(position);
      }

      compactor.accept(line.getBytes(StandardCharsets.UTF_8));

      if (lineReader.isNextTag() || !lineReader.isNextLine()) {
        break;
      }
      lineReader.readLine();
    }

    entry
        .getSequence()
        .setSequence(ByteBuffer.wrap(Arrays.copyOf(sequence.array(), sequence.position())));
    entry
        .getSequence()
        .setOrigin(new FlatFileOrigin(lineReader.getFileId(), firstLineNumber, lastLineNumber));
  }

  private static final byte[] dna_tr = new byte[256];
  private static final byte[] prn_tr = new byte[256];

  static {
    // DNA
    dna_tr['A'] = dna_tr['a'] = 'a';
    dna_tr['B'] = dna_tr['b'] = 'b';
    dna_tr['C'] = dna_tr['c'] = 'c';
    dna_tr['D'] = dna_tr['d'] = 'd';
    dna_tr['G'] = dna_tr['g'] = 'g';
    dna_tr['H'] = dna_tr['h'] = 'h';
    dna_tr['K'] = dna_tr['k'] = 'k';
    dna_tr['M'] = dna_tr['m'] = 'm';
    dna_tr['N'] = dna_tr['n'] = 'n';
    dna_tr['R'] = dna_tr['r'] = 'r';
    dna_tr['S'] = dna_tr['s'] = 's';
    dna_tr['T'] = dna_tr['t'] = 't';
    dna_tr['V'] = dna_tr['v'] = 'v';
    dna_tr['W'] = dna_tr['w'] = 'w';
    dna_tr['Y'] = dna_tr['y'] = 'y';
    dna_tr['U'] = dna_tr['u'] = 't';

    dna_tr[' '] =
        dna_tr['0'] =
            dna_tr['1'] =
                dna_tr['2'] =
                    dna_tr['3'] =
                        dna_tr['4'] =
                            dna_tr['5'] =
                                dna_tr['6'] =
                                    dna_tr['7'] = dna_tr['8'] = dna_tr['9'] = SUBSTITUTION;

    // PROTEIN
    for (int i = 0; i < prn_tr.length; ++i) prn_tr[i] = (byte) Character.toLowerCase((char) i);

    prn_tr[' '] =
        prn_tr['0'] =
            prn_tr['1'] =
                prn_tr['2'] =
                    prn_tr['3'] =
                        prn_tr['4'] =
                            prn_tr['5'] =
                                prn_tr['6'] =
                                    prn_tr['7'] = prn_tr['8'] = prn_tr['9'] = SUBSTITUTION;
  }

  private void compactDNASequence(byte[] seq) {
    if (seq == null || seq.length == 0) {
      return;
    }
    for (byte base : seq) {
      byte c = dna_tr[base];
      if (0 == c) {
        this.error("SQ.1", (char) base);
      } else if (SUBSTITUTION != c) {
        sequence.put(c);
      }
    }
  }

  private void compactProteinSequence(byte[] seq) {
    if (seq == null || seq.length == 0) {
      return;
    }
    for (byte base : seq) {
      byte c = prn_tr[base];
      if (SUBSTITUTION != c) sequence.put(c);
    }
  }
}
