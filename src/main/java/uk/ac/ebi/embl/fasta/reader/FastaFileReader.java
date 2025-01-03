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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.entry.SubmitterAccessionFix;
import uk.ac.ebi.embl.flatfile.reader.FlatFileEntryReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SequenceReader;

public class FastaFileReader extends FlatFileEntryReader {
  public FastaFileReader(LineReader lineReader) {
    super(lineReader);
  }

  private final String OBJECT_NAME_REGEX = "^>\\s*([^\\s*\\|]*)(.*)$";
  private final Pattern objectNamePattern = Pattern.compile(OBJECT_NAME_REGEX);

  private boolean isEntry;

  private Entry entry;

  protected boolean isCheckBlockCounts = true;

  protected boolean isIgnoreLocationParseError = false;
  public static boolean isOrigin = true;

  protected int currentEntryLine = 1;
  protected int nextEntryLine = currentEntryLine;

  @Override
  protected void afterReadLines(ValidationResult validationResult) {
    SubmitterAccessionFix.fix(getEntry());
  }

  @Override
  public void readLines() throws IOException {
    isEntry = false;
    lineReader.readLine();
    if (!lineReader.isCurrentLine()) {
      return;
    }
    currentEntryLine = nextEntryLine;
    entry = (new EntryFactory()).createEntry();
    entry.setSequence((new SequenceFactory()).createSequence());
    entry.getSequence().setTopology(Topology.LINEAR);
    String object_name = readObjectName(lineReader);
    if (object_name != null) {
      entry.setSubmitterAccession(SubmitterAccessionFix.fix(object_name));
      Text header = new Text(lineReader.getCurrentLine());
      entry.setComment(header);
      isEntry = true;
    }
    if (!lineReader.isNextTag()) {
      lineReader.readLine();
      try {
        append((new SequenceReader(lineReader)).read(entry));
      } catch (Exception e) {
        String entryname =
            entry.getPrimaryAccession() == null
                ? entry.getSubmitterAccession()
                : entry.getPrimaryAccession();
        if (entryname == null)
          throw new IOException(
              "Invalid Sequence:Failed to read the Sequence at line :"
                  + lineReader.getCurrentLine(),
              e);
        else
          throw new IOException(
              "Invalid Sequence:Failed to read the Sequence of : " + entryname, e);
      }
    }
    nextEntryLine = lineReader.getCurrentLineNumber();
  }

  private String readObjectName(LineReader reader) {
    Matcher matcher = objectNamePattern.matcher(reader.getCurrentRawLine());
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
  }

  @Override
  public Entry getEntry() {
    return entry;
  }

  @Override
  public boolean isEntry() {
    return isEntry;
  }

  @Override
  protected void skipLines() throws IOException {
    // TODO Auto-generated method stub
  }
}
