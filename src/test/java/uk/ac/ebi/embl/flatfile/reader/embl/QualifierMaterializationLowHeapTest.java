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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * Runs a parser probe in a dedicated low-heap JVM to confirm OOM behavior for qualifier-heavy
 * entries.
 */
public class QualifierMaterializationLowHeapTest {

  @Test
  public void qualifierHeavyEntryTriggersOOMUnderLowHeap() throws Exception {
    int qualifierCount = intProperty("qualifier.probe.qualifierCount", 750_000);
    int lowHeapMb = intProperty("qualifier.probe.lowHeapMb", 96);

    ProbeRun run = runProbe(lowHeapMb, qualifierCount);

    assertEquals(
        "Expected low-heap probe to exit with OOM code. Command="
            + run.command
            + " Output="
            + run.output,
        QualifierMaterializationMemoryProbeMain.OOM_EXIT_CODE,
        run.exitCode);
    assertTrue(
        "Expected OOM marker in probe output: " + run.output,
        run.output.contains("probe.oom=true"));
  }

  private ProbeRun runProbe(int maxHeapMb, int qualifierCount) throws Exception {
    List<String> command = new ArrayList<>();
    command.add(javaBinary());
    command.add("-Xms16m");
    command.add("-Xmx" + maxHeapMb + "m");
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add(QualifierMaterializationMemoryProbeMain.class.getName());
    command.add(String.valueOf(qualifierCount));

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    boolean finished = process.waitFor(240, TimeUnit.SECONDS);
    if (!finished) {
      process.destroyForcibly();
      throw new AssertionError("Probe process timed out. Command=" + String.join(" ", command));
    }

    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new ProbeRun(process.exitValue(), output, String.join(" ", command));
  }

  private int intProperty(String key, int defaultValue) {
    String value = System.getProperty(key);
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    return Integer.parseInt(value);
  }

  private String javaBinary() {
    String executable =
        System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")
            ? "java.exe"
            : "java";
    return Path.of(System.getProperty("java.home"), "bin", executable).toString();
  }

  private static final class ProbeRun {
    private final int exitCode;
    private final String output;
    private final String command;

    private ProbeRun(int exitCode, String output, String command) {
      this.exitCode = exitCode;
      this.output = output;
      this.command = command;
    }
  }
}
