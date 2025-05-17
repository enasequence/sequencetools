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
package uk.ac.ebi.embl.api.validation.file;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.PolySampleValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFiles;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class PolySampleValidationCheckTest {

  private SubmissionOptions options;
  private PolySampleValidationCheck polySampleValidationCheck;
  private final String reportsPath =
      System.getProperty("user.dir")
          + "/src/test/resources/uk/ac/ebi/embl/api/validation/file/template";

  @Before
  public void init() throws Exception {

    options = new SubmissionOptions();
    options.isWebinCLI = true;
    options.reportDir = Optional.of(reportsPath);
    options.context = Optional.of(Context.sequence);
    polySampleValidationCheck =
        new PolySampleValidationCheck(options, new PolySampleValidationCheck.SharedInfo());
  }

  private void checkReport(File file, String s) throws Exception {
    boolean isFound = false;
    StringBuilder lines = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.contains(s)) {
          isFound = true;
          break;
        } else lines.append(line + "\n");
      }
    }
    if (!isFound) assertEquals(s, lines.toString());
  }

  private void checkPolySample(
      Context context,
      String tsvFile,
      String taxFile,
      String fastaFile,
      boolean isValid,
      String expectedMesage)
      throws Exception {

    SubmissionFiles submissionFiles = new SubmissionFiles();
    if (tsvFile != null) {
      submissionFiles.addFile(
          new SubmissionFile(
              SubmissionFile.FileType.SAMPLE_TSV,
              new File(reportsPath + File.separator + tsvFile),
              null));
    }
    if (taxFile != null) {
      submissionFiles.addFile(
          new SubmissionFile(
              SubmissionFile.FileType.TAX_TSV,
              new File(reportsPath + File.separator + taxFile),
              null));
    }
    if (fastaFile != null) {
      submissionFiles.addFile(
          new SubmissionFile(
              SubmissionFile.FileType.FASTA,
              new File(reportsPath + File.separator + fastaFile),
              null));
    }

    options = new SubmissionOptions();
    options.submissionFiles = Optional.of(submissionFiles);
    options.isWebinCLI = true;
    options.reportDir = Optional.of(reportsPath);
    options.context = Optional.of(context);
    polySampleValidationCheck =
        new PolySampleValidationCheck(options, new PolySampleValidationCheck.SharedInfo());

    ValidationResult result = polySampleValidationCheck.check();
    if (isValid) {
      assertTrue(result.isValid());
    } else {
      assertFalse(result.isValid());
      result.getMessages().stream()
          .forEach(
              message -> {
                assertTrue(message.getMessage().contains(expectedMesage));
              });
    }
  }

  @Test
  public void testTaxTSV() throws Exception {
    checkPolySample(Context.ploysample_tax, null, "tax_valid.tsv", null, true, "");
  }

  @Test
  public void testPolySampleTaxTSV() throws Exception {
    checkPolySample(
        Context.ploysample_full,
        "poly_sample_valid.tsv",
        "tax_valid.tsv",
        "poly_sample_fasta_valid.txt",
        true,
        "");
  }

  @Test
  public void testPolySampleTSV() throws Exception {
    checkPolySample(
        Context.ploysample_fastq_sample,
        "poly_sample_valid.tsv",
        null,
        "poly_sample_fasta_valid.txt",
        true,
        "");
  }

  @Test
  public void testPolySampleInvalidFrequency() throws Exception {
    checkPolySample(
        Context.ploysample_fastq_sample,
        "poly_sample_invalid_frequency.tsv",
        null,
        "poly_sample_fasta_valid.txt",
        false,
        "Missing message: Polysample Frequency must be a valid number");
  }

  @Test
  public void testPolySampleInvalidSequenceId() throws Exception {
    checkPolySample(
        Context.ploysample_fastq_sample,
        "poly_sample_invalid_sequence_id.tsv",
        null,
        "poly_sample_fasta_valid.txt",
        false,
        "Missing message: Sequence: ENTRY_NAME3 is not mapped in the sample TSV file.");
  }
}
