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

import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.entry.SubmitterAccessionFix;
import uk.ac.ebi.embl.flatfile.reader.FlatFileEntryReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

public class AGPFileReader extends FlatFileEntryReader {

  public AGPFileReader(LineReader lineReader) {
    super(lineReader);
    ValidationMessageManager.addBundle(FlatFileValidations.AGP_FLAT_FILE_BUNDLE);
  }

  private boolean isEntry;
  private Entry entry;
  private static final String SCREGEX = "\\s+";
  protected int currentEntryLine = 1;
  private static final int NUMBER_OF_COLUMNS = 9;
  private static final int NO_LINKAGE_GAP_NUMBER_OF_COLUMNS = 8;
  private final String[] linkageArray = {"YES", "NO"};
  private static final int OBJECT = 0;
  private static final int OBJECT_BEG = 1;
  private static final int OBJECT_END = 2;
  private static final int PART_NUMBER = 3;
  private static final int COMPONENT_TYPE_ID = 4;
  private static final int COMPONENT_ID = 5;
  private static final int GAP_LENGTH = 5;
  private static final int COMPONENT_BEG = 6;
  private static final int GAP_TYPE = 6;
  private static final int COMPONENT_END = 7;
  private static final int LINKAGE = 7;
  private static final int ORIENTATION = 8;
  private static final int LINKAGEEVIDENCE = 8;
  private static final Long UNKNOWN_GAP_LENGTH = 100L;
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
    while (lineReader.getCurrentRawLine().charAt(0) == '#') {
      lineReader.readLine();
      continue;
    }
    currentEntryLine = nextEntryLine;
    entry = (new EntryFactory()).createEntry();
    entry.setSequence((new SequenceFactory()).createSequence());
    entry.getSequence().setTopology(Topology.LINEAR);
    String line = null;
    while (true) {
      isEntry = true;
      AgpRow agpRow = new AgpRow();
      line = lineReader.getCurrentRawLine();

      String[] fields = line.split(SCREGEX);

      if (fields.length != NUMBER_OF_COLUMNS) {
        if (fields.length >= COMPONENT_TYPE_ID + 1
            && fields[COMPONENT_TYPE_ID] != null
            && ("N".equals(fields[COMPONENT_TYPE_ID]) || "U".equals(fields[COMPONENT_TYPE_ID]))) {
          if (fields.length >= LINKAGE + 1 && "NO".equalsIgnoreCase(fields[LINKAGE])) {
            if (fields.length != NO_LINKAGE_GAP_NUMBER_OF_COLUMNS) {
              error("NumberOfColumnsCheck");
              return;
            }
          } else {
            error("NumberOfColumnsCheck");
            return;
          }
        } else {
          error("NumberOfColumnsCheck");
          return;
        }
      }

      if (fields[OBJECT] == null || fields[OBJECT].isEmpty()) {
        error("MissingObjectCheck");
      } else {
        String submitterAccession = SubmitterAccessionFix.fix(fields[OBJECT]);
        entry.setSubmitterAccession(submitterAccession);
        agpRow.setObject(submitterAccession);
      }

      // OBJECT_BEG

      if (fields[OBJECT_BEG] == null || fields[OBJECT_BEG].isEmpty()) {
        error("MissingObjectBegCheck");
      } else if (!StringUtils.isNumeric(fields[OBJECT_BEG])) {
        error("InvalidObjectBegCheck");
      } else {
        agpRow.setObject_beg(Integer.valueOf(fields[OBJECT_BEG]));
      }

      // OBJECT_END

      if (fields[OBJECT_END] == null || fields[OBJECT_END].isEmpty()) {
        error("MissingObjectEndCheck");
      } else if (!StringUtils.isNumeric(fields[OBJECT_END])) {
        error("InvalidObjectEndCheck");
      } else {
        agpRow.setObject_end(Long.valueOf(fields[OBJECT_END]));
      }

      // PART_NUMBER

      if (fields[PART_NUMBER] == null || fields[PART_NUMBER].isEmpty()) {
        error("MissingPartNumberCheck");
      } else if (!StringUtils.isNumeric(fields[PART_NUMBER])) {
        error("InvalidPartNumberCheck");
      } else {
        agpRow.setPart_number(Integer.valueOf(fields[PART_NUMBER]));
      }

      // COMPONENT_TYPE

      if (fields[COMPONENT_TYPE_ID] == null || fields[COMPONENT_TYPE_ID].isEmpty()) {
        error("MissingComponentTypeCheck");
      } else {
        agpRow.setComponent_type_id(fields[COMPONENT_TYPE_ID].toUpperCase());
      }

      if (fields[COMPONENT_TYPE_ID] != null
          && ("N".equals(fields[COMPONENT_TYPE_ID]) || "U".equals(fields[COMPONENT_TYPE_ID]))) {

        if ("U".equals(fields[COMPONENT_TYPE_ID])) {
          agpRow.setGap_length(UNKNOWN_GAP_LENGTH);
        } else {
          // GAP_LENGTH

          if (fields[GAP_LENGTH] == null || fields[GAP_LENGTH].isEmpty()) {
            error("MissingGapLengthCheck");

          } else if (!StringUtils.isNumeric(fields[GAP_LENGTH])) {
            error("InvalidGapLengthCheck");
          } else {
            agpRow.setGap_length(Long.valueOf(fields[GAP_LENGTH]));
          }
        }

        // GAP_TYPE

        if (fields[GAP_TYPE] == null || fields[GAP_TYPE].isEmpty()) {
          error("MissingGapTypeCheck");
        } else {
          agpRow.setGap_type(fields[GAP_TYPE].toLowerCase());
        }

        // LINKAGE

        if (fields[LINKAGE] == null || fields[LINKAGE].isEmpty()) {
          error("MissingLinkageCheck");
        } else if (!ArrayUtils.contains(linkageArray, fields[LINKAGE].toUpperCase())) {
          error("InvalidLinkageCheck");
        } else {
          agpRow.setLinkage(fields[LINKAGE]);
        }

        // LINKAGE_EVIDENCE
        if (fields.length == NUMBER_OF_COLUMNS) {
          if ((fields[LINKAGEEVIDENCE] == null || fields[LINKAGEEVIDENCE].isEmpty())) {
            if (!"NO".equalsIgnoreCase(agpRow.getLinkage())) error("MissingLinkageEvidenceCheck");
          } else {
            String linkageEvidence = fields[LINKAGEEVIDENCE];
            agpRow.setLinkageevidence(Arrays.asList(linkageEvidence.split(";")));
          }
        }
      } else if (fields[COMPONENT_TYPE_ID] != null
          && !fields[COMPONENT_TYPE_ID].equals("A")
          && !fields[COMPONENT_TYPE_ID].equals("D")
          && !fields[COMPONENT_TYPE_ID].equals("F")
          && !fields[COMPONENT_TYPE_ID].equals("G")
          && !fields[COMPONENT_TYPE_ID].equals("O")
          && !fields[COMPONENT_TYPE_ID].equals("P")
          && !fields[COMPONENT_TYPE_ID].equals("W")) {
        error("InvalidComponentTypeCheck");
      } else {
        if (fields[COMPONENT_ID] == null || fields[COMPONENT_ID].isEmpty()) {
          error("MissingComponentIDCheck");
        } else {
          String submitterAccession = SubmitterAccessionFix.fix(fields[COMPONENT_ID]);
          agpRow.setComponent_id(submitterAccession);
        }

        // COMPONENT_BEG

        if (fields[COMPONENT_BEG] == null || fields[COMPONENT_BEG].isEmpty()) {
          error("MissingComponentBegCheck");
        } else if (!StringUtils.isNumeric(fields[COMPONENT_BEG])) {
          error("InvalidComponentBegCheck");
        } else {
          agpRow.setComponent_beg(Long.valueOf(fields[COMPONENT_BEG]));
        }

        // COMPONENT_END

        if (fields[COMPONENT_END] == null || fields[COMPONENT_END].isEmpty()) {
          error("MissingComponentEndCheck");
        } else if (!StringUtils.isNumeric(fields[COMPONENT_END])) {
          error("InvalidComponentEndCheck");
        } else {
          agpRow.setComponent_end(Long.valueOf(fields[COMPONENT_END]));
        }

        // ORIENTATION

        if (fields[ORIENTATION] == null || fields[ORIENTATION].isEmpty()) {
          error("MissingOrientationCheck");
        } else {
          String orientation = fields[ORIENTATION].toLowerCase();
          if (orientation.equals("minus") || orientation.equals("-")) {
            agpRow.setOrientation("-");
          } else if (orientation.equals("plus") || orientation.equals("+")) {
            agpRow.setOrientation("+");

          } else {
            agpRow.setOrientation(orientation);
          }
        }
      }
      agpRow.setOrigin(
          new FlatFileOrigin(getLineReader().getFileId(), lineReader.getCurrentLineNumber()));
      entry.getSequence().addAgpRow(agpRow);

      if (!lineReader.isNextLine()) {
        break;
      } else if (!lineReader.joinLine()) {
        break;
      }

      lineReader.readLine();
    }
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
