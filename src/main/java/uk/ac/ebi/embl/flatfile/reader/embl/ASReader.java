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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the flat file AS lines. */
public class ASReader extends SingleLineBlockReader {

  public ASReader(LineReader lineReader) {
    super(lineReader);
  }

  private static final Pattern PATTERN =
      Pattern.compile(
          "^\\s*"
              + "(\\d+)"
              + // secondary range begin position
              "\\s*-\\s*"
              + "(\\d+)"
              + // secondary range end position
              "\\s*"
              + "(\\w+)"
              + // primary range accession
              "(\\s*\\.(\\d+)?\\s*)?"
              + // primary range version
              "\\s*"
              + "(\\d+)"
              + // primary range begin position
              "\\s*-\\s*"
              + "(\\d+)"
              + // primary range end position
              "\\s*"
              + "([cC])?"
              + // complement
              "\\s*$");

  private static final int GROUP_SECONDARY_BEGIN_POSITION = 1;
  private static final int GROUP_SECONDARY_END_POSITION = 2;
  private static final int GROUP_ACCESSION = 3;
  private static final int GROUP_VERSION = 5;
  private static final int GROUP_PRIMARY_BEGIN_POSITION = 6;
  private static final int GROUP_PRIMARY_END_POSITION = 7;
  private static final int GROUP_COMPLEMENT = 8;

  @Override
  public String getTag() {
    return EmblTag.AS_TAG;
  }

  @Override
  protected void read(String block) {
    FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
    if (!matcher.match(block)) {
      error("FF.1", getTag());
      return;
    }
    EntryFactory entryFactory = new EntryFactory();
    Long secondaryBeginPosition = matcher.getLong(GROUP_SECONDARY_BEGIN_POSITION);
    Long secondaryEndPosition = matcher.getLong(GROUP_SECONDARY_END_POSITION);
    String accession = matcher.getUpperString(GROUP_ACCESSION);
    Integer version = matcher.getInteger(GROUP_VERSION);
    Long primaryBeginPosition = matcher.getLong(GROUP_PRIMARY_BEGIN_POSITION);
    Long primaryEndPosition = matcher.getLong(GROUP_PRIMARY_END_POSITION);
    boolean complement = (matcher.getString(GROUP_COMPLEMENT) != null);
    Assembly assembly =
        entryFactory.createAssembly(
            accession,
            version,
            primaryBeginPosition,
            primaryEndPosition,
            complement,
            secondaryBeginPosition,
            secondaryEndPosition);
    entry.addAssembly(assembly);
    assembly.setOrigin(getOrigin());
  }
}
