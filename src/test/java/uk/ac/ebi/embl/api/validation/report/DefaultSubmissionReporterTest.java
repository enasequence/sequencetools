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
package uk.ac.ebi.embl.api.validation.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.DefaultOrigin;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class DefaultSubmissionReporterTest {

  public static Path createTempFile() throws IOException {
    return Files.createTempFile("test", ".tmp");
  }

  public static DefaultSubmissionReporter createReporter() {
    return new DefaultSubmissionReporter(
        new HashSet<>(Arrays.asList(Severity.INFO, Severity.ERROR)));
  }

  @Test
  public void testWriteToReport_String_WithAndWithoutOrigin_File() throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    reporter.writeToFile(reportFile.toFile(), Severity.ERROR, "MESSAGE1");
    reporter.writeToFile(reportFile.toFile(), Severity.INFO, "MESSAGE2");

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    reporter.writeToFile(reportFile.toFile(), Severity.ERROR, "MESSAGE1", origin);
    reporter.writeToFile(reportFile.toFile(), Severity.INFO, "MESSAGE2", origin);

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }

  @Test
  public void testWriteToReport_ValidationMessage_WithAndWithoutOrigin_File() throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    reporter.writeToFile(
        reportFile.toFile(),
        DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    reporter.writeToFile(
        reportFile.toFile(),
        DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    reporter.writeToFile(
        reportFile.toFile(),
        DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1", origin));
    reporter.writeToFile(
        reportFile.toFile(),
        DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2", origin));

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }

  @Test
  public void testWriteToReport_ValidationResult_WithAndWithoutTargetOrigin_File()
      throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    ValidationResult result = new ValidationResult();
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));

    reporter.writeToFile(reportFile.toFile(), result);

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With target origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    result = new ValidationResult();
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));

    reporter.writeToFile(reportFile.toFile(), result, "ORIGIN2");

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }

  @Test
  public void testWriteToReport_ValidationPlanResult_WithAndWithoutTargetOrigin_File()
      throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    ValidationResult planResult = new ValidationResult();
    ValidationResult result1 = new ValidationResult();
    ValidationResult result2 = new ValidationResult();
    result1.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result2.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));
    planResult.append(result1);
    planResult.append(result2);

    reporter.writeToFile(reportFile.toFile(), planResult);

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With target origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    planResult = new ValidationResult();
    result1 = new ValidationResult();
    result2 = new ValidationResult();
    result1.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result2.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));
    planResult.append(result1);
    planResult.append(result2);

    reporter.writeToFile(reportFile.toFile(), planResult, "ORIGIN2");

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }

  /////////

  @Test
  public void testWriteToReport_String_WithAndWithoutOrigin_Path() throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    reporter.writeToFile(reportFile, Severity.ERROR, "MESSAGE1");
    reporter.writeToFile(reportFile, Severity.INFO, "MESSAGE2");

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    reporter.writeToFile(reportFile, Severity.ERROR, "MESSAGE1", origin);
    reporter.writeToFile(reportFile, Severity.INFO, "MESSAGE2", origin);

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }

  @Test
  public void testWriteToReport_ValidationMessage_WithAndWithoutOrigin_Path() throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    reporter.writeToFile(
        reportFile, DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    reporter.writeToFile(
        reportFile, DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    reporter.writeToFile(
        reportFile,
        DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1", origin));
    reporter.writeToFile(
        reportFile,
        DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2", origin));

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }

  @Test
  public void testWriteToReport_ValidationResult_WithAndWithoutTargetOrigin_Path()
      throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    ValidationResult result = new ValidationResult();
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));

    reporter.writeToFile(reportFile, result);

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With target origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    result = new ValidationResult();
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));

    reporter.writeToFile(reportFile, result, "ORIGIN2");

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }

  @Test
  public void testWriteToReport_ValidationPlanResult_WithAndWithoutTargetOrigin_Path()
      throws Exception {
    DefaultSubmissionReporter reporter = createReporter();

    Path reportFile = createTempFile();

    ValidationResult planResult = new ValidationResult();
    ValidationResult result1 = new ValidationResult();
    ValidationResult result2 = new ValidationResult();
    result1.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result2.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));
    planResult.append(result1);
    planResult.append(result2);

    reporter.writeToFile(reportFile, planResult);

    List<String> lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2"));

    // With target origin.

    Origin origin = new DefaultOrigin("ORIGIN2");

    reportFile = createTempFile();

    planResult = new ValidationResult();
    result1 = new ValidationResult();
    result2 = new ValidationResult();
    result1.append(DefaultSubmissionReporter.createValidationMessage(Severity.ERROR, "MESSAGE1"));
    result2.append(DefaultSubmissionReporter.createValidationMessage(Severity.INFO, "MESSAGE2"));
    planResult.append(result1);
    planResult.append(result2);

    reporter.writeToFile(reportFile, planResult, "ORIGIN2");

    lines = Files.readAllLines(reportFile);
    Assert.assertEquals(2, lines.size());
    Assert.assertTrue(lines.get(0).endsWith("ERROR: MESSAGE1 [ORIGIN2]"));
    Assert.assertTrue(lines.get(1).endsWith("INFO: MESSAGE2 [ORIGIN2]"));
  }
}
