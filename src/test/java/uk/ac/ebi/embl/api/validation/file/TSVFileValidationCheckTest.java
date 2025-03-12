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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.TSVFileValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class TSVFileValidationCheckTest {
  private static final String PROJECT_ID = "PRJEB13102";
  private SubmissionOptions options;
  private FileValidationCheck fileValidationCheck;
  private SubmissionFile submissionFile;
  private final Path sequenceFixedFilePath =
      Paths.get(
          System.getProperty("user.dir")
              + "/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/sequenceFixed.txt");
  private final String reportsPath =
      System.getProperty("user.dir")
          + "/src/test/resources/uk/ac/ebi/embl/api/validation/file/template";
  private static final String[] allTemplatesA = {
    "ERT000002-rRNA.tsv.gz",
    "ERT000003-EST-1.tsv.gz",
    "ERT000006-SCM.tsv.gz",
    "ERT000009-ITS.tsv.gz",
    "ERT000020-COI.tsv.gz",
    "ERT000024-GSS-1.tsv.gz",
    "ERT000028-SVC.tsv.gz",
    "ERT000029-SCGD.tsv.gz",
    "ERT000030-MHC1.tsv.gz",
    "ERT000032-matK.tsv.gz",
    "ERT000034-Dloop.tsv.gz",
    "ERT000035-IGS.tsv.gz",
    "ERT000036-MHC2.tsv.gz",
    "ERT000037-intron.tsv.gz",
    "ERT000038-hyloMarker.tsv.gz",
    "ERT000039-Sat.tsv.gz",
    "ERT000042-ncRNA.tsv.gz",
    "ERT000047-betasat.tsv.gz",
    "ERT000050-ISR.tsv.gz",
    "ERT000051-poly.tsv.gz",
    "ERT000052-ssRNA.tsv.gz",
    "ERT000053-ETS.tsv.gz",
    "ERT000055-STS.tsv.gz",
    "ERT000056-mobele.tsv.gz",
    "ERT000057-alphasat.tsv.gz",
    "ERT000058-MLmarker.tsv.gz",
    "ERT000060-vUTR.tsv.gz",
    // Test validation with entry number column
    "with-entrynumber.tsv.gz",
    // Test validation without entry number column
    "without-entrynumber.tsv.gz",
    // Test with Checklist template id
    "ERT000002-rRNA-with-checklist-line.tsv.gz"
  };

  @Before
  public void init() throws Exception {
    try {
      if (Files.exists(sequenceFixedFilePath)) Files.delete(sequenceFixedFilePath);
      Files.createFile(sequenceFixedFilePath);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    options = new SubmissionOptions();
    options.isWebinCLI = true;
    options.setProjectId(PROJECT_ID);
    options.reportDir = Optional.of(reportsPath);
    options.context = Optional.of(Context.sequence);
    fileValidationCheck = new TSVFileValidationCheck(options, new FileValidationCheck.SharedInfo());
  }

  @After
  public void clearGeneratedFiles() throws IOException {
    // Delete files created while running the test
    for (File file : new File(reportsPath).listFiles()) {
      if (file.getAbsoluteFile().getName().startsWith("TEMPLATE_ERT")) {
        Files.delete(file.toPath());
      }
    }
    Files.delete(sequenceFixedFilePath);
  }

  @Test
  public void allTemplates() throws ValidationEngineException {
    boolean valid = true;
    String templateDirStr =
        System.getProperty("user.dir")
            + "/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/";
    for (String tsvFile : allTemplatesA) {
      try {
        if (Files.exists(sequenceFixedFilePath)) Files.delete(sequenceFixedFilePath);
        Files.createFile(sequenceFixedFilePath);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      submissionFile =
          new SubmissionFile(
              SubmissionFile.FileType.TSV,
              new File(templateDirStr + tsvFile),
              sequenceFixedFilePath.toFile());
      ValidationResult result = fileValidationCheck.check(submissionFile);
      if (!result.isValid()) {
        valid = false;
        System.out.println("Failed: " + tsvFile);
        result.getMessages(Severity.ERROR).forEach(m -> System.out.println(m.getMessage()));
      }
    }
    assertTrue(valid);
    System.out.println("Finished.");
  }

  /*   private void assertTsvValidatorError( String expectedReportFile, String actualReportFile) {
          String tsvFileDir = "uk/ac/ebi/ena/webin/cli/template/";



          try {
              Path expectedReportFilePath = tsvFileDir + expectedReportFile);
              Path actualReportFilePath = validator.getValidationDir().toPath().resolve(actualReportFile);

              String expectedReport = new String(Files.readAllBytes(expectedReportFilePath)).replaceAll("\\s+", "");
              String actualReport = new String(Files.readAllBytes(actualReportFilePath), StandardCharsets.UTF_8).replaceAll("\\s+", "");

              assertThat(actualReport).isEqualTo(expectedReport);
          } catch (IOException ex) {
              throw new RuntimeException(ex);
          }
      }
  */

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

  private void checkTSV(String fileName, boolean isValid, String expectedMesage) throws Exception {
    submissionFile =
        new SubmissionFile(
            SubmissionFile.FileType.TSV,
            new File(reportsPath + File.separator + fileName),
            sequenceFixedFilePath.toFile());
    boolean valid = fileValidationCheck.check(submissionFile).isValid();
    if (isValid) {
      assertTrue(valid);
    } else {
      assertFalse(valid);
      checkReport(new File(reportsPath + File.separator + fileName + ".report"), expectedMesage);
    }
  }

  @Test
  public void singleCDSInvalidLocation() throws Exception {
    checkTSV(
        "cds29.tsv.gz",
        false,
        "ERROR: Invalid feature location: <yes..>yes [Sequence: 1 ,  line: 22]");
  }

  /* @Test
  public void DELineIssue() throws Exception {
      checkTSV("DELineIssue.tsv.gz", true, "");
  }*/

  @Test
  public void ppGenePassedAsMarker() throws Exception {
    checkTSV("Sequence-PP_GENE-as-MARKER.tsv.gz", true, "");
  }

  @Test
  public void invalidMarker() throws Exception {
    checkTSV(
        "Sequence-invalid-marker.tsv.gz",
        false,
        "ERROR: Value \"sausages\" is not in the required set of Markers, Permitted values are actin,alpha tubulin,beta tubulin,translation elongation factor 1 alpha,calmodulin,RNA polymerase II large subunit 1,RNA polymerase II large subunit 2,Glyceraldehyde 3-phosphate dehydrogenase,Histone H3,  [Sequence: 1 ]");
  }

  @Test
  public void invalidSediment() throws Exception {
    checkTSV(
        "Sequence-invalid-sediment.tsv.gz",
        false,
        "ERROR: Value \"666\" is not in the required set of Sedimentation coefficien, Permitted values are 5S,5.8S,12S,16S,18S,23S,26S,28S  [Sequence: 1 ]");
  }

  @Test
  public void testInvalidMandatoryFieldsPresent() throws Exception {
    checkTSV(
        "Sequence-mandatory-field-missing.tsv.gz",
        false,
        "ERROR: The following mandatory field(s) are missing SEDIMENT - All headers are capitalised. [Sequence: 1 ]");
  }

  @Test
  public void testPolySampleTSV() throws Exception {
    checkTSV("poly_sample_valid.tsv", true, "");
  }
}
