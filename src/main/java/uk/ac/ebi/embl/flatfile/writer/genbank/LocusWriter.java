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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;

/** Flat file writer for the LOCUS lines. */
public class LocusWriter extends FlatFileWriter {

  public LocusWriter(Entry entry) {
    super(entry);
  }

  static final String FORMAT_PRIMARY_ACCESSION = "%-14s";
  static final String FORMAT_SEQUENCE_LENGTH = "%14s";
  static final String FORMAT_MOLECULE_TYPE = "%-8s";
  static final String FORMAT_TOPOLOGY = "%-9s";

  public boolean write(Writer writer) throws IOException {
    writer.write(GenbankPadding.LOCUS_PADDING);
    String accession = entry.getPrimaryAccession();
    if (!isBlankString(accession)) {
      (new PrintWriter(writer)).printf(FORMAT_PRIMARY_ACCESSION, accession);
    } else {
      (new PrintWriter(writer)).printf(FORMAT_PRIMARY_ACCESSION, "XXX");
    }

    if (entry.getSequence() != null) {
      Long length = entry.getSequence().getLength();
      if (length != null) {
        (new PrintWriter(writer)).printf(FORMAT_SEQUENCE_LENGTH, length);
      } else {
        (new PrintWriter(writer)).printf(FORMAT_SEQUENCE_LENGTH, " ");
      }
    } else {
      (new PrintWriter(writer)).printf(FORMAT_SEQUENCE_LENGTH, " ");
    }
    writer.write(" bp    ");

    if (entry.getSequence() != null) {
      String moleculeType = entry.getSequence().getMoleculeType();
      if (moleculeType != null) {
        if (!moleculeType.contains(" ")) {
          (new PrintWriter(writer)).printf(FORMAT_MOLECULE_TYPE, moleculeType);
        } else if (moleculeType.contains("RNA")) {
          (new PrintWriter(writer)).printf(FORMAT_MOLECULE_TYPE, "RNA");
        } else if (moleculeType.contains("DNA")) {
          (new PrintWriter(writer)).printf(FORMAT_MOLECULE_TYPE, "DNA");
        } else {
          (new PrintWriter(writer)).printf(FORMAT_MOLECULE_TYPE, "XXX");
        }
      }
    } else {
      (new PrintWriter(writer)).printf(FORMAT_MOLECULE_TYPE, "XXX");
    }

    if (entry.getSequence() != null) {
      Sequence.Topology topology = entry.getSequence().getTopology();
      if (topology != null) {
        if (topology == Sequence.Topology.LINEAR) {
          (new PrintWriter(writer)).printf(FORMAT_TOPOLOGY, "linear");
        } else if (topology == Sequence.Topology.CIRCULAR) {
          (new PrintWriter(writer)).printf(FORMAT_TOPOLOGY, "circular");
        }
      } else {
        (new PrintWriter(writer)).printf(FORMAT_TOPOLOGY, "XXX");
      }
    } else {
      (new PrintWriter(writer)).printf(FORMAT_TOPOLOGY, "XXX");
    }

    if (entry.getDataClass() != null
        && (entry.getDataClass().equals("EST")
            || entry.getDataClass().equals("GSS")
            || entry.getDataClass().equals("HTC")
            || entry.getDataClass().equals("HTG")
            || entry.getDataClass().equals("TSA")
            || entry.getDataClass().equals("CON")
            || entry.getDataClass().equals("STS"))) {
      writer.write(entry.getDataClass());
    } else if (entry.getDivision() != null
        && (entry.getDivision().equals("ROD")
            || entry.getDivision().equals("MAM")
            || entry.getDivision().equals("VRT")
            || entry.getDivision().equals("INV")
            || entry.getDivision().equals("PLN")
            || entry.getDivision().equals("VRL")
            || entry.getDivision().equals("PHG"))) {
      writer.write(entry.getDivision());
    } else {
      writer.write("XXX");
    }

    writer.write(" ");
    if (entry.getLastUpdated() != null) {
      writer.write(FlatFileDateUtils.formatAsDay(entry.getLastUpdated()));
    } else {
      writer.write("XXX");
    }
    writer.write("\n");
    return true;
  }
}
