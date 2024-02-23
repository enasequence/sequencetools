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
package uk.ac.ebi.embl.agp.reader;

import java.io.BufferedReader;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.flatfile.reader.LineReader;

public class AGPLineReader extends LineReader {
  public AGPLineReader(BufferedReader reader) {
    super(reader);
  }

  private static final String SCREGEX = "\\s";

  @Override
  protected int getTagWidth(String line) {
    return getTag(line).length();
  }

  @Override
  protected boolean isTag(String line) {
    return true;
  }

  @Override
  protected String getTag(String line) {
    if (line == null) return null;
    String[] fields = line.trim().split(SCREGEX);
    if (fields.length == 0) {
      return null;
    }
    return trimObjectName(fields[0]);
  }

  @Override
  public boolean joinLine() {
    if (!isCurrentLine()) {
      return false;
    }
    if (!isNextLine()) {
      return false;
    }
    // compare current and next tag
    return getCurrentTag().equals(getNextTag());
  }

  @Override
  protected boolean isSkipLine(String line) {
    return line != null && line.trim().isEmpty();
  }

  private String trimObjectName(String object_name) {
    return StringUtils.removeEnd(object_name.replaceAll("\\s", ""), ";");
  }
}
