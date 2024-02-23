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
package uk.ac.ebi.embl.fasta.reader;

import java.io.BufferedReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;

public class FastaLineReader extends LineReader {

  public FastaLineReader(BufferedReader reader) {
    super(reader);
  }

  private static final int DEFAULT_TAG_WIDTH = 1;
  private static final String FASTA_TAG = ">";

  @Override
  protected int getTagWidth(String line) {
    return Math.min(DEFAULT_TAG_WIDTH, line.length());
  }

  @Override
  protected boolean isTag(String line) {
    return line.startsWith(FASTA_TAG);
  }

  @Override
  protected boolean isSkipLine(String line) {
    return line != null && line.trim().isEmpty();
  }
}
