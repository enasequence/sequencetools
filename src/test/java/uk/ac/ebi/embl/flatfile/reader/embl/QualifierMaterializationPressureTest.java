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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;

/**
 * Hypothesis test support:
 *
 * <p>For EMBL assembly entries, qualifier-heavy FT sections can create very high heap pressure
 * because each qualifier line is materialized as an in-memory Qualifier object attached to a
 * Feature.
 */
public class QualifierMaterializationPressureTest {

  @Test
  public void materializesEachInferenceQualifierLineIntoAQualifierObject() throws Exception {
    int inferenceQualifierCount = 5_000;
    String entryText = buildAssemblyEntryWithInferenceQualifiers(inferenceQualifierCount);
    Entry entry = parseAssemblyEntry(entryText);

    long rawInferenceLines = countInferenceLines(entryText);
    int materializedInferenceQualifiers = countInferenceQualifiers(entry);
    int totalMaterializedQualifiers = countAllQualifiers(entry);

    assertEquals(rawInferenceLines, materializedInferenceQualifiers);
    assertTrue(totalMaterializedQualifiers >= materializedInferenceQualifiers);
  }

  @Test
  public void materializedQualifierCountScalesWithInputQualifierVolume() throws Exception {
    Entry smallEntry = parseAssemblyEntry(buildAssemblyEntryWithInferenceQualifiers(500));
    Entry largeEntry = parseAssemblyEntry(buildAssemblyEntryWithInferenceQualifiers(5_000));

    int smallQualifierCount = countInferenceQualifiers(smallEntry);
    int largeQualifierCount = countInferenceQualifiers(largeEntry);

    assertEquals(500, smallQualifierCount);
    assertEquals(5_000, largeQualifierCount);
    assertTrue(largeQualifierCount >= smallQualifierCount * 10);
  }

  private Entry parseAssemblyEntry(String entryText) throws Exception {
    BufferedReader reader = new BufferedReader(new StringReader(entryText));
    EntryReader entryReader =
        new EmblEntryReader(reader, EmblEntryReader.Format.ASSEMBLY_FILE_FORMAT, "synthetic.embl");

    ValidationResult parseResult = entryReader.read();
    assertTrue("Synthetic test entry should parse as one entry", entryReader.isEntry());
    assertTrue(
        "Synthetic test entry should parse without validation errors", parseResult.isValid());
    return entryReader.getEntry();
  }

  private long countInferenceLines(String entryText) {
    return entryText
        .lines()
        .filter(line -> line.startsWith("FT                   /inference="))
        .count();
  }

  private int countInferenceQualifiers(Entry entry) {
    int count = 0;
    for (Feature feature : entry.getFeatures()) {
      count += feature.getQualifiers("inference").size();
    }
    return count;
  }

  private int countAllQualifiers(Entry entry) {
    int count = 0;
    for (Feature feature : entry.getFeatures()) {
      count += feature.getQualifiers().size();
    }
    return count;
  }

  private String buildAssemblyEntryWithInferenceQualifiers(int inferenceQualifierCount) {
    StringBuilder sb = new StringBuilder(8_192);
    sb.append("ID   XXX; XXX; linear; genomic DNA; XXX; XXX; 60 BP.\n");
    sb.append("XX\n");
    sb.append("AC * _SYNTHETIC00001\n");
    sb.append("XX\n");
    sb.append("FH   Key             Location/Qualifiers\n");
    sb.append("FH\n");
    sb.append("FT   source          1..60\n");
    sb.append("FT                   /organism=\"Syntheticus testii\"\n");
    sb.append("FT                   /mol_type=\"genomic DNA\"\n");
    sb.append("FT   gene            1..60\n");
    sb.append("FT                   /locus_tag=\"SYN_LOCUS_1\"\n");
    for (int i = 1; i <= inferenceQualifierCount; i++) {
      sb.append("FT                   /inference=\"pipeline:evidence-");
      sb.append(i);
      sb.append("\"\n");
    }
    sb.append("SQ   Sequence 60 BP; 15 A; 15 C; 15 G; 15 T; 0 other;\n");
    sb.append("     acgtacgtac gtacgtacgt acgtacgtac gtacgtacgt acgtacgtac gtacgtacgt        60\n");
    sb.append("//\n");
    return sb.toString();
  }
}
