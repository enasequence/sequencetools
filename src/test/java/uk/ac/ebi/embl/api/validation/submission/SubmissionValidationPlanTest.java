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
package uk.ac.ebi.embl.api.validation.submission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.*;
import org.junit.rules.ExpectedException;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;
import uk.ac.ebi.embl.api.service.WebinSampleRetrievalService;
import uk.ac.ebi.embl.api.validation.GenomeUtils;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.file.SubmissionValidationTest;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.template.TemplateEntryProcessorTest;

public class SubmissionValidationPlanTest extends SubmissionValidationTest {
  SubmissionOptions options = null;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  public static void beforeClass() {
    // to clear out changes made by other tests that might interfere with tests in this class.
    GlobalDataSets.resetTestDataSets();
  }

  @Before
  public void init() {
    options = new SubmissionOptions();
    options.isWebinCLI = true;
    options.assemblyInfoEntry = Optional.of(getAssemblyinfoEntry());
    options.source = Optional.of(getSource());
    options.ignoreErrors = true;
    options.isDevMode = true;
    options.forceReducedFlatfileCreation = true;
  }

  // 1
  @Test
  public void testGenomeWithFastaAGPMultiLevel()
      throws FlatFileComparatorException, ValidationEngineException {
    String rootPath =
        "genome" + RESOURCE_FILE_SEPARATOR + "multilevel_scaffold" + RESOURCE_FILE_SEPARATOR;
    String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
    String agpFileName = "agp_scafoold_levels.txt"; // IWGSC_CSS_6DL_scaff_3330716
    String chrListFileName = "chromosome_list_scaffold_levels.txt";
    options.context = Optional.of(Context.genome);

    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.AGP));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, chrListFileName, FileType.CHROMOSOME_LIST));
    options.submissionFiles = Optional.of(submissionFiles);

    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();
    assertEquals(1, plan.getUnplacedEntryNames().size());
    assertTrue(plan.getUnplacedEntryNames().contains("IWGSC_CSS_6DL_scaff_3330718".toUpperCase()));

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "valid_fastaforAGP_contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "valid_fastaforAGP_scaffolds.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.scaffoldFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "valid_fastaforAGP_chromosome.flatfile.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.chromosomeFileName)));
  }

  @Test
  public void testGenomeWithFastaFlatfile()
      throws ValidationEngineException, FlatFileComparatorException {
    String rootPath =
        "genome" + RESOURCE_FILE_SEPARATOR + "fasta_flatfile" + RESOURCE_FILE_SEPARATOR;
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_genome_flatfile.txt", FileType.FLATFILE));
    options.submissionFiles = Optional.of(submissionFiles);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA)
                .getFile()
                .getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
  }

  @Test
  public void testGenomeWithFastaChromosomeListErrorSequenceless()
      throws ValidationEngineException, IOException {
    String rootPath =
        "genome" + RESOURCE_FILE_SEPARATOR + "sequenceless_chr" + RESOURCE_FILE_SEPARATOR;
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(
            rootPath, "chromosome_list_sequenceless.txt", FileType.CHROMOSOME_LIST));
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA)
                .getFile()
                .getParent());

    Files.deleteIfExists(
        Paths.get(options.reportDir.get() + RESOURCE_FILE_SEPARATOR + "fasta.info"));
    Files.deleteIfExists(Paths.get(options.reportDir.get() + RESOURCE_FILE_SEPARATOR + "agp.info"));
    Files.deleteIfExists(
        Paths.get(options.reportDir.get() + RESOURCE_FILE_SEPARATOR + "flatfile.info"));
    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    thrown.expect(ValidationEngineException.class);
    thrown.expectMessage(
        "Sequenceless chromosomes are not allowed in assembly : IWGSC_CSS_6DL_SCAFF_3330719,IWGSC_CSS_6DL_SCAFF_3330717,IWGSC_CSS_6DL_SCAFF_3330716");
    plan.execute();
  }

  @Test
  @Ignore
  public void testGenomeSubmissionWithFastawithValidChromosomeList()
      throws ValidationEngineException, FlatFileComparatorException {
    // String rootPath = "genome"+ RESOURCE_FILE_SEPARATOR+ "sequenceless_chr" +
    // RESOURCE_FILE_SEPARATOR;
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionFixedTestFile("chromosome_list.txt", FileType.CHROMOSOME_LIST));
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir =
        Optional.of(
            initSubmissionTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA)
                .getFile()
                .getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    thrown.expect(ValidationEngineException.class);
    // thrown.expectMessage(getmessage("fasta",initSubmissionFixedTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA).getFile().getName(), options.reportDir.get()));
    plan.execute();
    // assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA).getFile()));
  }

  @Test
  public void testGenomeWithFlatfileAGP()
      throws FlatFileComparatorException, ValidationEngineException, IOException,
          InterruptedException {
    String rootPath = "genome" + RESOURCE_FILE_SEPARATOR + "agp_flatfile" + RESOURCE_FILE_SEPARATOR;
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_flatfileforAgp.txt", FileType.FLATFILE));
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, "valid_agp.txt", FileType.AGP));
    options.submissionFiles = Optional.of(submissionFiles);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_flatfileforAgp.txt", FileType.FLATFILE)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_flatfileforAgp.txt", FileType.FLATFILE)
                .getFile()
                .getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "scaffolds.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.scaffoldFileName)));
  }

  @Test
  public void testGenomeWithFastaAGP()
      throws FlatFileComparatorException, ValidationEngineException {
    String rootPath = "genome" + RESOURCE_FILE_SEPARATOR + "agp_fasta" + RESOURCE_FILE_SEPARATOR;
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_fastaforAgp.txt", FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_fastaagp.txt", FileType.AGP));
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_fastaforAgp.txt", FileType.FASTA)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_fastaforAgp.txt", FileType.FASTA)
                .getFile()
                .getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "scaffolds.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.scaffoldFileName)));
  }

  @Test
  public void testGenomeWithFastaAGPUnlocalisedList()
      throws FlatFileComparatorException, ValidationEngineException, IOException {
    String rootPath =
        "genome" + RESOURCE_FILE_SEPARATOR + "agp_unlocalised" + RESOURCE_FILE_SEPARATOR;

    String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
    String agpFileName = "agp_scaffold_levels.txt";
    String chrListFileName = "chromosome_list_scaffold_levels.txt";
    String unlocalisedListFile = "unlocalised_list_agp.txt";

    options.context = Optional.of(Context.genome);

    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.AGP));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, chrListFileName, FileType.CHROMOSOME_LIST));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, unlocalisedListFile, FileType.UNLOCALISED_LIST));
    options.submissionFiles = Optional.of(submissionFiles);

    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();
    assertTrue(plan.getUnplacedEntryNames().isEmpty());

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "valid_fastaforAGP_contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "valid_fastaforAGP_scaffolds.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.scaffoldFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "valid_fastaforAGP_chromosome.flatfile.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.chromosomeFileName)));
  }

  @Test
  public void testGenomeWithFastaAGPChromosomeList()
      throws FlatFileComparatorException, ValidationEngineException {
    String rootPath =
        "genome" + RESOURCE_FILE_SEPARATOR + "agp_fasta_chr_list" + RESOURCE_FILE_SEPARATOR;

    String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
    String agpFileName = "agp_scafoold_levels_1.txt";
    String chrListFileName = "chromosome_list_scaffold_levels_1.txt";

    options.context = Optional.of(Context.genome);

    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.AGP));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, chrListFileName, FileType.CHROMOSOME_LIST));
    options.submissionFiles = Optional.of(submissionFiles);

    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();
    assertTrue(plan.getUnplacedEntryNames().isEmpty());

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "chromosome.flatfile.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.chromosomeFileName)));
    assertTrue(plan.getUnplacedEntryNames().isEmpty());
  }

  @Test
  public void testGenomeWithFasta() throws ValidationEngineException, FlatFileComparatorException {
    String rootPath = "genome" + RESOURCE_FILE_SEPARATOR + "fasta" + RESOURCE_FILE_SEPARATOR;
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA));
    options.submissionFiles = Optional.of(submissionFiles);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA)
                .getFile()
                .getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
  }

  @Test
  public void testGenomeSubmissionFastaAnnotationOnly()
      throws FlatFileComparatorException, ValidationEngineException {
    String rootPath =
        "genome" + RESOURCE_FILE_SEPARATOR + "annotation_only" + RESOURCE_FILE_SEPARATOR;
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, "valid_fastaforAnnotationOnly.txt", FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(
            rootPath, "valid_AnnotationOnly_flatfile.txt", FileType.FLATFILE));
    options.submissionFiles = Optional.of(submissionFiles);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_fastaforAnnotationOnly.txt", FileType.FASTA)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, "valid_fastaforAnnotationOnly.txt", FileType.FASTA)
                .getFile()
                .getParent());
    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
  }

  @Test
  public void testGenomeWithFastaAGPAnnotationOnly()
      throws FlatFileComparatorException, ValidationEngineException {
    // anootation file has annotations for 1 contig, 1 scaffold and 1 chromosome ,
    // after execution this will be verified by comparing contigs and scaffolds reduced file and
    // chromosome flatfile.
    // also verified by comparing existing enapro loading flatfiles.
    String rootPath =
        "genome" + RESOURCE_FILE_SEPARATOR + "agp_annotation_only" + RESOURCE_FILE_SEPARATOR;
    String fastaFileName = "valid_fasta.txt";
    String agpFileName = "valid_agp.txt";
    String chrListFileName = "valid_chromosome_list.txt";
    String annotationFileName = "valid_annotation_only_flatfile.txt";

    options.context = Optional.of(Context.genome);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));

    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.AGP));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, chrListFileName, FileType.CHROMOSOME_LIST));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, annotationFileName, FileType.FLATFILE));
    options.submissionFiles = Optional.of(submissionFiles);

    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute();

    assertEquals(plan.getUnplacedEntryNames().size(), 1);
    assertEquals(
        plan.getUnplacedEntryNames().toArray()[0].toString(), "IWGSC_CSS_6DL_SCAFF_3330717");

    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "contigs.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.contigFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "scaffolds.reduced.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.scaffoldFileName)));
    assertTrue(
        compareOutputFixedFiles(
            getReducedFilePath(rootPath, "chromosome.flatfile.expected"),
            getReducedFilePath(rootPath, FileValidationCheck.chromosomeFileName)));
  }

  @Test
  public void testValidTranscriptomeFastaSubmission()
      throws ValidationEngineException, FlatFileComparatorException {
    options.context = Optional.of(Context.transcriptome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    SubmissionFile subFile =
        initSubmissionFixedTestFile("valid_transcriptom_fasta.txt", FileType.FASTA);
    submissionFiles.addFile(subFile);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(subFile.getFile().getParent());
    options.processDir = Optional.of(subFile.getFile().getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    ValidationResult validationResult = plan.execute();
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testValidTranscriptomeFlatFileSubmission()
      throws ValidationEngineException, FlatFileComparatorException {
    options.context = Optional.of(Context.transcriptome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionFixedTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE));
    options.submissionFiles = Optional.of(submissionFiles);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.reportDir =
        Optional.of(
            initSubmissionTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE)
                .getFile()
                .getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE)
                .getFile()
                .getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    ValidationResult validationResult = plan.execute();
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testTranscriptomeSuplicateEntryNameFlatFile() throws ValidationEngineException {
    options.context = Optional.of(Context.transcriptome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    SubmissionFile subFile =
        initSubmissionFixedTestFile(
            "transcriptom_flatfile_duplicate_entryname.txt", FileType.FLATFILE);
    submissionFiles.addFile(subFile);
    options.submissionFiles = Optional.of(submissionFiles);
    options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
    options.reportDir = Optional.of(subFile.getFile().getParent());
    options.processDir = Optional.of(subFile.getFile().getParent());

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    thrown.expect(ValidationEngineException.class);
    thrown.expectMessage("Entry names are duplicated in assembly : ENTRY_NAME1");
    plan.execute();
  }

  @Test
  public void testGenomeSubmissionwitFastawithValidChromosomeListMultipleTimesInParallel() {
    List<CompletableFuture<Void>> futs = new ArrayList<>();
    for (int i = 0; i < 32; i++) {
      int taskId = i + 1;

      futs.add(
          CompletableFuture.runAsync(
              () -> {
                SubmissionFiles submissionFiles = new SubmissionFiles();
                submissionFiles.addFile(
                    initSubmissionFixedTestFile("valid_genome_fasta_2.txt", FileType.FASTA));
                submissionFiles.addFile(
                    initSubmissionFixedTestFile("chromosome_list_2.txt", FileType.CHROMOSOME_LIST));

                File reportProcessDir =
                    submissionFiles
                        .getFiles()
                        .get(0)
                        .getFile()
                        .getParentFile()
                        .toPath()
                        .resolve("" + taskId)
                        .toFile();
                reportProcessDir.mkdir();

                SubmissionOptions opts = new SubmissionOptions();
                opts.isWebinCLI = true;
                opts.assemblyInfoEntry = Optional.of(getAssemblyinfoEntry());
                opts.source = Optional.of(getSource());
                opts.ignoreErrors = true;
                opts.isDevMode = true;
                opts.context = Optional.of(Context.genome);
                opts.submissionFiles = Optional.of(submissionFiles);
                opts.reportDir = Optional.of(reportProcessDir.getAbsolutePath());
                opts.processDir = Optional.of(reportProcessDir.getAbsolutePath());

                try {
                  SubmissionValidationPlan plan = new SubmissionValidationPlan(opts);

                  ValidationResult res = plan.execute();

                  assertTrue(res.isValid());
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              }));
    }

    CompletableFuture.allOf(futs.toArray(new CompletableFuture[futs.size()])).join();
  }

  @Test
  public void testCovid19GenomeSizeContigsScaffoldsSuccess()
      throws ValidationEngineException, IOException {
    String rootPath = "genome/size_check/";
    String fastaFile = "covid19_contig.fasta"; // 29,518bp
    String agpFile = "covid19_scaffold.agp";
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFile, FileType.FASTA));
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFile, FileType.AGP));
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.assemblyInfoEntry.get().setAssemblyType(AssemblyType.COVID_19_OUTBREAK.getValue());

    clearInfoFiles(options.processDir.get());
    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute(); // should throw no exception
  }

  @Test
  public void testCovid19GenomeSizeContigsScaffoldsFailure()
      throws ValidationEngineException, IOException {
    String rootPath = "genome/size_check/";
    String fastaFile = "covid19_contig2.fasta"; // 31,711bp
    String agpFile = "covid19_scaffold2.agp";
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFile, FileType.FASTA));
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFile, FileType.AGP));
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.assemblyInfoEntry.get().setAssemblyType(AssemblyType.COVID_19_OUTBREAK.getValue());

    clearInfoFiles(options.processDir.get());
    String expectedMsg =
        String.format(
            "%s maximum genome size is %d bp.",
            AssemblyType.COVID_19_OUTBREAK.getValue(),
            GenomeUtils.COVID_19_OUTBREAK_GENOME_MAX_SIZE);
    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    thrown.expect(ValidationEngineException.class);
    thrown.expectMessage(expectedMsg);
    plan.execute();
  }

  @Test
  public void testCovid19GenomeSizeOneChromosomeSuccess()
      throws ValidationEngineException, IOException {
    String rootPath = "genome/size_check/";
    String fastaFile = "covid19_one_chromosome.fasta";
    String chromoListFile = "covid19_one_chromosome_list.txt";
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFile, FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, chromoListFile, FileType.CHROMOSOME_LIST));
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.assemblyInfoEntry.get().setAssemblyType(AssemblyType.COVID_19_OUTBREAK.getValue());

    clearInfoFiles(options.processDir.get());
    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    plan.execute(); // should throw no exception
  }

  @Test
  public void testCovid19GenomeSizeOneChromosomeFailure()
      throws ValidationEngineException, IOException {
    String rootPath = "genome/size_check/";
    String fastaFile = "covid19_one_chromosome2.fasta"; // 31,216bp
    String chromoListFile = "covid19_one_chromosome_list.txt";
    options.context = Optional.of(Context.genome);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFile, FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionFixedTestFile(rootPath, chromoListFile, FileType.CHROMOSOME_LIST));
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.processDir =
        Optional.of(
            initSubmissionTestFile(rootPath, fastaFile, FileType.FASTA).getFile().getParent());
    options.assemblyInfoEntry.get().setAssemblyType(AssemblyType.COVID_19_OUTBREAK.getValue());

    clearInfoFiles(options.processDir.get());
    String expectedMsg =
        String.format(
            "%s maximum genome size is %d bp.",
            AssemblyType.COVID_19_OUTBREAK.getValue(),
            GenomeUtils.COVID_19_OUTBREAK_GENOME_MAX_SIZE);
    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    thrown.expect(ValidationEngineException.class);
    thrown.expectMessage(expectedMsg);
    plan.execute();
  }

  @Test
  public void testTSVSubmission()
      throws ValidationEngineException, FlatFileComparatorException, IOException {
    SequenceToolsServices.init(
        new WebinSampleRetrievalService(
            TemplateEntryProcessorTest.WEBIN_REST_URI,
            TemplateEntryProcessorTest.BIOSAMPLES_URI,
            TemplateEntryProcessorTest.getAuthTokenForTest(
                TemplateEntryProcessorTest.WEBIN_AUTH_JSON),
            TemplateEntryProcessorTest.getAuthTokenForTest(
                TemplateEntryProcessorTest.BIOSAMPLES_WEBIN_AUTH_JSON)));

    // Test submission with taxId 9606 in ORGANISM_NAME
    executeTSVSubmission("ERT000002-rRNA-with-taxid.tsv.gz");

    // Test submission with sample organism name in ORGANISM_NAME
    executeTSVSubmission("ERT000002-rRNA-with-organism-name.tsv.gz");

    // Test submission with sample SAMEA9403245 in ORGANISM_NAME
    executeTSVSubmission("ERT000002-rRNA-with-sample.tsv.gz");
  }

  public void executeTSVSubmission(String tsvZipFile)
      throws IOException, ValidationEngineException, FlatFileComparatorException {
    String rootPath =
        System.getProperty("user.dir")
            + "/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/";
    File inputFile = new File(rootPath + tsvZipFile);
    File outPutFile = new File(rootPath + tsvZipFile + ".fixed");
    File updatedOutPutFile = new File(rootPath + tsvZipFile + ".expected.updated.fixed");
    options.context = Optional.of(Context.sequence);
    SubmissionFiles submissionFiles = new SubmissionFiles();
    SubmissionFile subFile = new SubmissionFile(FileType.TSV, inputFile, outPutFile);
    submissionFiles.addFile(subFile);
    options.submissionFiles = Optional.of(submissionFiles);
    options.reportDir = Optional.of(rootPath);
    options.processDir = Optional.of(rootPath);

    SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
    Files.deleteIfExists(
        Paths.get(options.processDir.get() + "/" + AssemblySequenceInfo.sequencefileName));
    plan.execute();

    // Compare input and output files are correct
    assertTrue(compareOutputFixedFiles(inputFile));
    Files.deleteIfExists(outPutFile.toPath());
    Files.deleteIfExists(updatedOutPutFile.toPath());
  }
}
