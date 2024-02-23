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

import java.util.Date;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the flat file LOCUS lines. */
public class LocusReader extends SingleLineBlockReader {

  public LocusReader(LineReader lineReader) {
    super(lineReader);
  }

  // X54885                   282 bp    DNA     linear   VRT 18-JUL-1991
  private static final Pattern PATTERN =
      Pattern.compile(
          "^\\s*"
              + "(\\w+)"
              + // locus name
              "\\s+"
              + "(?:(\\d+)\\s+\\w+)"
              + // length
              "\\s+"
              + "(\\S+)"
              + // molecule type
              "(?:\\s+(\\w+))?"
              + // topology
              "\\s+"
              + "(\\w+)"
              + // division
              "\\s+"
              + "([\\w-]+)"
              + // date
              "$");

  // private static int GROUP_LOCUS_NAME = 1;
  private static final int GROUP_SEQUENCE_LENGTH = 2;
  // private static int GROUP_MOLECULE_TYPE = 3;
  private static final int GROUP_TOPOLOGY = 4;
  private static final int GROUP_DIVISION = 5;
  private static final int GROUP_DATE = 6;

  @Override
  public String getTag() {
    return GenbankTag.LOCUS_TAG;
  }

  @Override
  protected void read(String block) {
    entry.setOrigin(getOrigin());
    FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
    if (!matcher.match(block)) {
      error("FF.1", getTag());
      return;
    }

    // Locus name is not the same as primary accession number.

    Long sequenceLength = matcher.getLong(GROUP_SEQUENCE_LENGTH);
    if (sequenceLength != null) {
      entry.setIdLineSequenceLength(matcher.getLong(GROUP_SEQUENCE_LENGTH));
    }

    // Molecule type is not compatible with the /mol_type.

    Topology topology = getTopology(matcher.getString(GROUP_TOPOLOGY));
    entry.getSequence().setTopology(topology);

    if (matcher.isValueXXX(GROUP_DIVISION)) {
      String division = matcher.getUpperString(GROUP_DIVISION);
      if (division.equals("PHG")
          || division.equals("ENV")
          || division.equals("FUN")
          || division.equals("HUM")
          || division.equals("INV")
          || division.equals("MAM")
          || division.equals("VRT")
          || division.equals("MUS")
          || division.equals("PLN")
          || division.equals("PRO")
          || division.equals("ROD")
          || division.equals("SYN")
          || division.equals("TGN")
          || division.equals("UNC")
          || division.equals("BCT")
          || division.equals("VRL")) {
        entry.setDivision(division);
        entry.setDataClass("STD");
      } else if (division.equals("PRI")) {
        entry.setDivision("MAM");
        entry.setDataClass("STD");
      } else entry.setDataClass(division);
    }
    Date ffDate = matcher.getDay(GROUP_DATE);
    entry.setFFDate(ffDate);
    entry.setLastUpdated(ffDate);
  }

  private Topology getTopology(String topology) {
    if (topology == null) {
      return Topology.LINEAR;
    }
    topology = topology.toLowerCase();
    if (topology.equals("linear")) {
      return Topology.LINEAR;
    } else if (topology.equals("circular")) {
      return Topology.CIRCULAR;
    }
    error("ID.1", topology);
    return null;
  }
}
