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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;

/**
 * Manual memory probe for qualifier materialization.
 *
 * <p>Executes in a dedicated JVM (with caller-controlled -Xmx) and parses one synthetic EMBL
 * assembly entry containing a configurable number of /inference qualifiers.
 */
public final class QualifierMaterializationMemoryProbeMain {

  static final int OOM_EXIT_CODE = 100;

  private QualifierMaterializationMemoryProbeMain() {}

  public static void main(String[] args) {
    int qualifierCount = args.length > 0 ? Integer.parseInt(args[0]) : 100_000;
    Path probeFile = null;
    try {
      long heapBefore = usedHeapBytes();
      probeFile = createSyntheticEntryFile(qualifierCount);
      Entry entry = parseEntry(probeFile);
      int materialized = countInferenceQualifiers(entry);
      long heapAfter = usedHeapBytes();

      System.out.println("probe.qualifier.lines=" + qualifierCount);
      System.out.println("probe.qualifier.materialized=" + materialized);
      System.out.println("probe.heap.used.before=" + heapBefore);
      System.out.println("probe.heap.used.after=" + heapAfter);
      System.out.println("probe.heap.used.delta=" + Math.max(0, heapAfter - heapBefore));

      if (materialized != qualifierCount) {
        System.err.println(
            "probe.error=Materialized qualifier count does not match source line count");
        System.exit(3);
      }
      System.exit(0);
    } catch (OutOfMemoryError oom) {
      System.err.println("probe.oom=true");
      System.exit(OOM_EXIT_CODE);
    } catch (Throwable throwable) {
      System.err.println("probe.error=" + throwable.getMessage());
      throwable.printStackTrace(System.err);
      System.exit(2);
    } finally {
      if (probeFile != null) {
        try {
          Files.deleteIfExists(probeFile);
        } catch (IOException ignored) {
          // ignore cleanup failure in a probe utility
        }
      }
    }
  }

  private static Entry parseEntry(Path entryFile) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(entryFile, StandardCharsets.UTF_8)) {
      EntryReader entryReader =
          new EmblEntryReader(
              reader, EmblEntryReader.Format.ASSEMBLY_FILE_FORMAT, entryFile.toString());
      ValidationResult parseResult = entryReader.read();
      if (!entryReader.isEntry()) {
        throw new IllegalStateException("Probe entry did not parse as an EMBL entry");
      }
      if (!parseResult.isValid()) {
        throw new IllegalStateException(
            "Probe entry parsed with validation errors: " + parseResult.count());
      }
      return entryReader.getEntry();
    }
  }

  private static int countInferenceQualifiers(Entry entry) {
    int count = 0;
    for (Feature feature : entry.getFeatures()) {
      count += feature.getQualifiers("inference").size();
    }
    return count;
  }

  private static long usedHeapBytes() {
    Runtime runtime = Runtime.getRuntime();
    runtime.gc();
    return runtime.totalMemory() - runtime.freeMemory();
  }

  private static Path createSyntheticEntryFile(int inferenceQualifierCount) throws IOException {
    Path tempFile = Files.createTempFile("qualifier-memory-probe-", ".embl");
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
      writer.write("ID   XXX; XXX; linear; genomic DNA; XXX; XXX; 60 BP.\n");
      writer.write("XX\n");
      writer.write("AC * _SYNTHETIC00001\n");
      writer.write("XX\n");
      writer.write("FH   Key             Location/Qualifiers\n");
      writer.write("FH\n");
      writer.write("FT   source          1..60\n");
      writer.write("FT                   /organism=\"Syntheticus testii\"\n");
      writer.write("FT                   /mol_type=\"genomic DNA\"\n");
      writer.write("FT   gene            1..60\n");
      writer.write("FT                   /locus_tag=\"SYN_LOCUS_1\"\n");
      for (int i = 1; i <= inferenceQualifierCount; i++) {
        writer.write("FT                   /inference=\"pipeline:evidence-");
        writer.write(String.valueOf(i));
        writer.write("\"\n");
      }
      writer.write("SQ   Sequence 60 BP; 15 A; 15 C; 15 G; 15 T; 0 other;\n");
      writer.write(
          "     acgtacgtac gtacgtacgt acgtacgtac gtacgtacgt acgtacgtac gtacgtacgt        60\n");
      writer.write("//\n");
    }
    return tempFile;
  }
}
