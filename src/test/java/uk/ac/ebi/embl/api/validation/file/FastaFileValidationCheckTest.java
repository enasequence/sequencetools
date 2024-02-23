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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFiles;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class FastaFileValidationCheckTest extends SubmissionValidationTest {
  @Before
  public void init() throws SQLException {
    options = new SubmissionOptions();
    options.source = Optional.of(getSource());
    options.assemblyInfoEntry = Optional.of(getAssemblyinfoEntry());
    options.isWebinCLI = true;
    options.isDevMode = true;
  }

  @Test
  public void testInvalidFastaFile() throws ValidationEngineException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    SubmissionFile file =
        initSubmissionTestFile("invalid_fasta_sequence.txt", SubmissionFile.FileType.FASTA);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.genome);
    FastaFileValidationCheck check = new FastaFileValidationCheck(options, sharedInfo);
    assertFalse(check.check(file).isValid());
    assertNotNull(check.getMessageStats().get("SQ.1"));
  }

  @Test
  public void testTranscriptomFixedvalidFastaFile()
      throws ValidationEngineException, FlatFileComparatorException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.transcriptome);
    SubmissionFile file =
        initSubmissionFixedTestFile("valid_transcriptom_fasta.txt", SubmissionFile.FileType.FASTA);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.processDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.transcriptome);
    options.init();
    FastaFileValidationCheck check = new FastaFileValidationCheck(options, sharedInfo);
    assertTrue(check.check(file).isValid());
  }

  @Test
  public void testgenomeFixedvalidFastaFile()
      throws ValidationEngineException, FlatFileComparatorException, IOException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    SubmissionFile file =
        initSubmissionFixedTestFile("valid_genome_fasta.txt", SubmissionFile.FileType.FASTA);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.processDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.genome);
    options.init();
    FastaFileValidationCheck check = new FastaFileValidationCheck(options, sharedInfo);
    //
    assertTrue(check.check(file).isValid());
    /*   ConcurrentMap map = check.getSequenceDB().hashMap("map").createOrOpen();
    assertEquals("caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac",map.get("IWGSC_CSS_6DL_contig_209591".toUpperCase()));
    assertEquals("gttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggtttttttttttttttttttttttttttttttttg",map.get("IWGSC_CSS_6DL_contig_209592".toUpperCase()));
    assertEquals("aggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggga",map.get("IWGSC_CSS_6DL_contig_209593".toUpperCase()));
    check.getSequenceDB().close();*/
  }

  @Test
  public void testValidateUnlocalisedEntryNames() throws ValidationEngineException {

    sharedInfo = new FileValidationCheck.SharedInfo();
    sharedInfo.entryNames.addAll(Arrays.asList("1", "2", "3", "4", "5"));
    sharedInfo.unlocalisedEntryNames.addAll(Arrays.asList("1", "2", "3"));
    FileValidationCheck check = new FastaFileValidationCheck(options, sharedInfo);
    FileValidationCheck.validateUnlocalisedEntryNames(sharedInfo);

    sharedInfo = new FileValidationCheck.SharedInfo();
    sharedInfo.entryNames.addAll(Arrays.asList("1", "2", "3", "4", "5"));
    sharedInfo.unlocalisedEntryNames.addAll(
        Arrays.asList("Not-found-1", "1", "Not-found-3", "2", "3", "Not-found-2"));
    FileValidationCheck check1 = new FastaFileValidationCheck(options, sharedInfo);
    Exception exception =
        assertThrows(
            ValidationEngineException.class,
            () -> FileValidationCheck.validateUnlocalisedEntryNames(sharedInfo));

    String expectedMessage =
        "No sequences found for the following unlocalised sequence object names: "
            + "Not-found-1,Not-found-2,Not-found-3";
    assertEquals(exception.getMessage(), expectedMessage);
  }
}
