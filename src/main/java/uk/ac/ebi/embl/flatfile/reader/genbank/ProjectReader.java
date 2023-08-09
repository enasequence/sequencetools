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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file PR lines. */
public class ProjectReader extends MultiLineBlockReader {

  public ProjectReader(LineReader lineReader) {
    super(lineReader, ConcatenateType.CONCATENATE_SPACE);
  }

  @Override
  public String getTag() {
    return GenbankTag.PROJECT_TAG;
  }

  private static final Pattern PATTERN =
      Pattern.compile(
          "^\\s*"
              + "(?:[^\\d]+\\s*\\:\\s*)?"
              + "\\s*"
              + "(\\d+)"
              + // project id
              ".*$");

  private static final int GROUP_PROJCT_ID = 1;

  @Override
  protected void read(String block) {
    block = FlatFileUtils.trimRight(block, '.');
    for (String projectAccession : FlatFileUtils.split(block, " ")) {
      FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
      if (!matcher.match(projectAccession)) {
        error("FF.1", getTag());
        return;
      }
      projectAccession = matcher.getString(GROUP_PROJCT_ID);
      entry.addProjectAccession(new Text(projectAccession, getOrigin()));
    }
  }
}
