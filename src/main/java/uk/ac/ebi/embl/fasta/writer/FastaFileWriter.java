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
package uk.ac.ebi.embl.fasta.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.ac.ebi.embl.api.entry.Entry;

public class FastaFileWriter {
  private final Entry entry;
  private final Writer writer;
  private final FastaHeaderFormat headerFormat;

  public enum FastaHeaderFormat {
    DEFAULT_HEADER_FORMAT,
    ANALYSIS_HEADER_FORMAT,
    ENA_HEADER_FORMAT,
    POLYSAMPLE_HEADER_FORMAT,
    TRANSLATION_HEADER_FORMAT,
    JSON_FASTA_HEADER,
  }

  public FastaFileWriter(Entry entry, Writer writer) {
    this.entry = entry;
    this.writer = writer;
    this.headerFormat = FastaHeaderFormat.DEFAULT_HEADER_FORMAT;
  }

  public FastaFileWriter(Entry entry, Writer writer, FastaHeaderFormat headerFormat) {
    this.entry = entry;
    this.writer = writer;
    this.headerFormat = headerFormat;
  }

  public void write() throws IOException {
    String header = null;
    switch (headerFormat) {
      case DEFAULT_HEADER_FORMAT:
        header =
            String.format(
                ">EM_%s:%s %s %s:%s",
                entry.getDivision(),
                entry.getPrimaryAccession(),
                entry.getSequence().getAccessionwithVersion(),
                entry.getDataClass(),
                entry.getDescription().getText());
        break;
      case ANALYSIS_HEADER_FORMAT:
        header =
            String.format(">%s %s", entry.getPrimaryAccession(), entry.getSubmitterAccession());
        break;
      case ENA_HEADER_FORMAT:
        header =
            String.format(
                ">ENA|%s|%s %s",
                entry.getPrimaryAccession(),
                entry.getSequence().getAccessionwithVersion(),
                entry.getDescription().getText());
        break;
      case POLYSAMPLE_HEADER_FORMAT:
        header =
            String.format(">%s|%s", entry.getPrimaryAccession(), entry.getSubmitterAccession());
        break;
      case TRANSLATION_HEADER_FORMAT:
        header = String.format(">%s", entry.getPrimaryAccession());
        break;
      case JSON_FASTA_HEADER:
        // make header
        String id = getFullAccession(entry);
        header = getJsonFastaHeader(id, entry);
        break;
      default:
        break;
    }

    FastaSequenceWriter sequenceWriter = new FastaSequenceWriter(writer, entry);
    writer.write(header + "\n");
    sequenceWriter.write();
    writer.write("\n");
  }

  public void writeWithId(String id) throws IOException {
    String header = null;
    switch (headerFormat) {
      case JSON_FASTA_HEADER:
        header = getJsonFastaHeader(id, entry);
        break;
      default:
        throw new UnsupportedOperationException(
            "This operation is not supported for the header format: " + headerFormat);
    }

    FastaSequenceWriter sequenceWriter = new FastaSequenceWriter(writer, entry);
    writer.write(header + "\n");
    sequenceWriter.write();
    writer.write("\n");
  }

  private String getJsonFastaHeader(String id, Entry entry) {
    Map<String, String> json = new LinkedHashMap<>();

    if (entry.getDescription() != null) {
      json.put("description", entry.getDescription().getText());
    }

    if (entry.getSequence() != null) {
      if (entry.getSequence().getMoleculeType() != null) {
        json.put("molecule_type", entry.getSequence().getMoleculeType());
      }
      if (entry.getSequence().getTopology() != null) {
        json.put("topology", entry.getSequence().getTopology().toString());
      }
    }

    try {
      ObjectMapper MAPPER = new ObjectMapper();
      String jsonPart = MAPPER.writeValueAsString(json);
      return String.format(">%s | %s", id, jsonPart);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to build JSON part", e);
    }
  }

  /**
   * Mimics the gff3tools logic for constructing an accession for Gff3Annotation out of Entry.
   * Supplying the id externally should be preffered for this case.
   *
   * @param entry the Entry to get the accession from
   * @return the effective accession, or null if neither is available
   */
  private static String getFullAccession(Entry entry) {
    String accession = getEffectiveAccession(entry);
    if (accession == null || accession.isEmpty()) {
      throw new IllegalStateException("No accession found for entry " + entry);
    }
    String[] parts = accession.split("[.]");

    // get base accession
    String baseAccession = parts[0];
    // get sequence version, aka second part of accession
    Integer sequenceVersion;
    if (parts.length == 2) {
      sequenceVersion = Integer.parseInt(parts[1]);
    } else if (entry.getSequence() != null && entry.getSequence().getVersion() != null) {
      sequenceVersion = entry.getSequence().getVersion();
    } else {
      sequenceVersion = 1;
    }

    String versionSuffix = "." + sequenceVersion;
    return baseAccession + versionSuffix;
  }

  /**
   * Gets the effective accession for an Entry, falling back to submitter accession if the sequence
   * accession is not available.
   *
   * <p>This is useful for TSV-based entries where the sequence accession is not set (assigned after
   * submission), but the submitter accession (ENTRYNUMBER) is available.
   *
   * @param entry the Entry to get the accession from
   * @return the effective accession, or null if neither is available
   */
  private static String getEffectiveAccession(Entry entry) {
    if (entry == null) {
      return null;
    }
    if (entry.getSequence() != null) {
      String accession = entry.getSequence().getAccession();
      if (accession != null && !accession.isEmpty()) {
        return accession;
      }
    }
    return entry.getSubmitterAccession();
  }
}
