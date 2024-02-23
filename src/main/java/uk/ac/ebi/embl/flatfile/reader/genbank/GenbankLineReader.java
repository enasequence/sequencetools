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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.BufferedReader;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.LineReader;

public class GenbankLineReader extends LineReader {

  public GenbankLineReader() {
    super();
  }

  public GenbankLineReader(BufferedReader reader) {
    super(reader);
  }

  public GenbankLineReader(BufferedReader reader, String fileId) {
    super(reader, fileId);
  }

  private static final int TAG_WIDTH = 12;

  @Override
  protected boolean isSkipLine(String line) {
    return line.trim().startsWith(GenbankTag.ERROR_MSG_TAG);
  }

  @Override
  protected int getTagWidth(String line) {
    if (line.startsWith("            ")) {
      return TAG_WIDTH;
    }
    if (!isTag(line)) {
      return 0;
    }
    return Math.min(TAG_WIDTH, line.length());
  }

  private static final Pattern TAG = Pattern.compile("^\\s{0,4}[A-Z_]{3,11}((\\s*)|(\\s+.*))$");

  @Override
  protected boolean isTag(String line) {
    if (line.startsWith(GenbankTag.TERMINATOR_TAG)) {
      return true;
    }
    return TAG.matcher(line).matches();
  }
}
