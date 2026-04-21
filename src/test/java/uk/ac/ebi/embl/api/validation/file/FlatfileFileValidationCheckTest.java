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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFiles;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class FlatfileFileValidationCheckTest extends SubmissionValidationTest {
  @Before
  public void init() throws SQLException {
    options = new SubmissionOptions();
    options.source = Optional.of(getSource());
    options.assemblyInfoEntry = Optional.of(getAssemblyinfoEntry());
    options.isWebinCLI = true;
    options.isDevMode = true;
  }

  @Test
  public void testInvalidFlatFile() throws ValidationEngineException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    SubmissionFile file =
        initSubmissionTestFile("invalid_flatfile.txt", SubmissionFile.FileType.FLATFILE);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.genome);
    options.init();
    FastaFileValidationCheck check = new FastaFileValidationCheck(options, sharedInfo);
    assertFalse(check.check(file).isValid());
  }

  @Test
  public void testTranscriptomFixedvalidFlatFile()
      throws ValidationEngineException, FlatFileComparatorException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.transcriptome);
    SubmissionFile file =
        initSubmissionFixedTestFile(
            "valid_transcriptom_flatfile.txt", SubmissionFile.FileType.FLATFILE);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.processDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.transcriptome);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.init();
    FlatfileFileValidationCheck check = new FlatfileFileValidationCheck(options, sharedInfo);
    assertTrue(check.check(file).isValid());
  }

  @Test
  public void testgenomeFixedvalidFlatFile()
      throws ValidationEngineException, FlatFileComparatorException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    // FT                   /circular_RNA
    SubmissionFile file =
        initSubmissionFixedTestFile("valid_genome_flatfile.txt", SubmissionFile.FileType.FLATFILE);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.processDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.genome);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.init();
    FlatfileFileValidationCheck check = new FlatfileFileValidationCheck(options, sharedInfo);
    assertTrue(check.check(file).isValid());
  }

  @Test
  public void testGenBankFixedValidFlatFile()
      throws ValidationEngineException, FlatFileComparatorException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    SubmissionFile file =
        initSubmissionFixedTestFile("valid_genbank_flatfile.txt", SubmissionFile.FileType.FLATFILE);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.processDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.sequence);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.init();
    FlatfileFileValidationCheck check = new FlatfileFileValidationCheck(options, sharedInfo);
    assertTrue(check.check(file).isValid());
  }

  @Test
  public void testGenomeFlatFilePseudogeneQualWithSingleQuote()
      throws ValidationEngineException, FlatFileComparatorException {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    SubmissionFile file =
        initSubmissionFixedTestFile(
            "valid_genome_flatfile_pseudogene.txt", SubmissionFile.FileType.FLATFILE);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(file.getFile().getParent());
    options.processDir = Optional.of(file.getFile().getParent());
    options.context = Optional.of(Context.sequence);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.init();
    FlatfileFileValidationCheck check = new FlatfileFileValidationCheck(options, sharedInfo);
    assertTrue(check.check(file).isValid());
  }

  @Test
  public void testAppendHeaderReplacesSubmittedIsolateWithMasterSource() throws Exception {
    sharedInfo = new FileValidationCheck.SharedInfo();

    validateMaster(Context.genome);
    options.context = Optional.of(Context.genome);
    options.getEntryValidationPlanProperty().fileType.set(FileType.EMBL);
    options
        .getEntryValidationPlanProperty()
        .validationScope
        .set(ValidationScope.ASSEMBLY_CONTIG);

    Entry entry = new EntryFactory().createEntry();
    entry.setSubmitterAccession("ENTRY_NAME1");
    entry.setSequence(new SequenceFactory().createSequenceofLength(20, 'a'));
    FeatureFactory featureFactory = new FeatureFactory();
    entry.addFeature(featureFactory.createSourceFeature());
    entry
        .getPrimarySourceFeature()
        .addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, "Submitted organism");
    entry
        .getPrimarySourceFeature()
        .addQualifier(Qualifier.ISOLATE_QUALIFIER_NAME, "submitted-isolate");
    entry
        .getPrimarySourceFeature()
        .addQualifier(Qualifier.SUBMITTER_SEQID_QUALIFIER_NAME, "AAAA02:E1");

    TestFlatfileFileValidationCheck check = new TestFlatfileFileValidationCheck(options, sharedInfo);
    assertTrue(
        entry.getPrimarySourceFeature()
            .getSingleQualifierValue(Qualifier.ISOLATE_QUALIFIER_NAME)
            .equals("submitted-isolate"));

    check.appendHeaderForTest(entry);

    assertFalse(
        entry.getPrimarySourceFeature().getQualifiers(Qualifier.ISOLATE_QUALIFIER_NAME).size() > 0);
    assertTrue(
        entry.getPrimarySourceFeature()
            .getSingleQualifierValue(Qualifier.ORGANISM_QUALIFIER_NAME)
            .equals("Micrococcus sp. 5"));
    assertTrue(
        entry.getPrimarySourceFeature()
            .getSingleQualifierValue(Qualifier.STRAIN_QUALIFIER_NAME)
            .equals("PR1"));
  }

  @Test
  public void testTemplateFixedvalidFlatFile()
      throws ValidationEngineException, FlatFileComparatorException {
    /*SubmissionFile file=initSubmissionFixedTestFile(".txt",SubmissionFile.FileType.FLATFILE);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(file);
          options.submissionFiles =Optional.of(submissionFiles);
          options.reportDir = Optional.of(file.getFile().getParent());
          options.context = Optional.of(Context.genome);
          options.init();
    FlatfileFileValidationCheck check = new FlatfileFileValidationCheck(options);
    assertTrue(check.check(file));
          assertTrue(compareOutputFiles(file.getFile()));*/
  }

  private static class TestFlatfileFileValidationCheck extends FlatfileFileValidationCheck {
    TestFlatfileFileValidationCheck(
        SubmissionOptions options, FileValidationCheck.SharedInfo sharedInfo) {
      super(options, sharedInfo);
    }

    void appendHeaderForTest(Entry entry) throws ValidationEngineException {
      appendHeader(entry);
    }
  }
}
