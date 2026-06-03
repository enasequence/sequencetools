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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.service.MasterEntryService;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.file.SubmissionValidationTest;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public class SubmissionValidationPlanUnplacedListTest extends SubmissionValidationTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private AtomicReference<MasterEntryService> masterEntryServiceReference;
  private MasterEntryService originalMasterEntryService;

  @Before
  public void replaceMasterEntryService() throws Exception {
    Field field = SequenceToolsServices.class.getDeclaredField("masterEntryService");
    field.setAccessible(true);

    masterEntryServiceReference = (AtomicReference<MasterEntryService>) field.get(null);
    originalMasterEntryService = masterEntryServiceReference.get();

    MasterEntryService masterEntryService = mock(MasterEntryService.class);
    when(masterEntryService.createMasterEntry(
            any(SubmissionOptions.class), any(ValidationResult.class)))
        .thenReturn(createMasterEntry());
    masterEntryServiceReference.set(masterEntryService);
  }

  @After
  public void restoreMasterEntryService() {
    masterEntryServiceReference.set(originalMasterEntryService);
  }

  @Test
  public void writesUnplacedListForInternalNonAgpFastaOnlyGenomeSubmission() throws Exception {
    Path processDir = temporaryFolder.newFolder("process").toPath();

    options = createInternalGenomeOptions(processDir, null);
    options.submissionFiles = Optional.of(fastaOnlySubmission());

    ValidationResult result = new SubmissionValidationPlan(options).execute();

    assertTrue(result.getMessages().toString(), result.isValid());
    Path unplacedList = processDir.resolve("unplaced.txt");
    assertTrue(Files.exists(unplacedList));
    Set<?> unplacedEntryNames = readUnplacedList(unplacedList);
    assertTrue(
        "Expected FASTA-only object to be unplaced: " + unplacedEntryNames,
        unplacedEntryNames.contains("1"));
  }

  @Test
  public void writesOnlyUnassignedNonAgpFastaObjectsToUnplacedList() throws Exception {
    Path processDir = temporaryFolder.newFolder("partial-assignment-process").toPath();

    options = createInternalGenomeOptions(processDir, null);
    options.submissionFiles = Optional.of(fastaWithPartialChromosomeAssignment(processDir));

    ValidationResult result = new SubmissionValidationPlan(options).execute();

    assertTrue(result.getMessages().toString(), result.isValid());
    Set<?> unplacedEntryNames = readUnplacedList(processDir.resolve("unplaced.txt"));
    assertTrue(
        "Expected unassigned FASTA object to be unplaced: " + unplacedEntryNames,
        unplacedEntryNames.contains("IWGSC_CSS_6DL_CONTIG_209593"));
    assertFalse(unplacedEntryNames.contains("IWGSC_CSS_6DL_CONTIG_209591"));
    assertFalse(unplacedEntryNames.contains("IWGSC_CSS_6DL_CONTIG_209592"));
  }

  @Test
  public void writesUnplacedListForInternalNonAgpFlatfileOnlyGenomeSubmission() throws Exception {
    Path processDir = temporaryFolder.newFolder("flatfile-process").toPath();

    options = createInternalGenomeOptions(processDir, null);
    options.locusTagPrefixes = Optional.of(Collections.singletonList("SPLC1"));
    options.submissionFiles = Optional.of(flatfileOnlySubmission());

    ValidationResult result = new SubmissionValidationPlan(options).execute();

    assertTrue(result.getMessages().toString(), result.isValid());
    Set<?> unplacedEntryNames = readUnplacedList(processDir.resolve("unplaced.txt"));
    assertTrue(
        "Expected flatfile object to be unplaced: " + unplacedEntryNames,
        unplacedEntryNames.contains("ENTRY_NAME1"));
  }

  @Test
  public void excludesUnlocalisedNonAgpFastaObjectsFromUnplacedList() throws Exception {
    Path processDir = temporaryFolder.newFolder("unlocalised-process").toPath();

    options = createInternalGenomeOptions(processDir, null);
    options.submissionFiles = Optional.of(fastaWithUnlocalisedObject(processDir));

    ValidationResult result = new SubmissionValidationPlan(options).execute();

    assertTrue(result.getMessages().toString(), result.isValid());
    Set<?> unplacedEntryNames = readUnplacedList(processDir.resolve("unplaced.txt"));
    assertTrue(
        "Expected unassigned FASTA object to be unplaced: " + unplacedEntryNames,
        unplacedEntryNames.contains("UNPLACED_CONTIG_1"));
    assertFalse(unplacedEntryNames.contains("UNLOCALISED_CONTIG_1"));
  }

  @Test
  public void writesOnlySequenceNamesForNonAgpFastaWithAnnotationOnlyFlatfile() throws Exception {
    Path processDir = temporaryFolder.newFolder("annotation-only-process").toPath();

    options = createInternalGenomeOptions(processDir, null);
    options.locusTagPrefixes = Optional.of(Collections.singletonList("SPLC1"));
    options.submissionFiles = Optional.of(fastaWithAnnotationOnlyFlatfile());

    ValidationResult result = new SubmissionValidationPlan(options).execute();

    assertTrue(result.getMessages().toString(), result.isValid());
    Set<?> unplacedEntryNames = readUnplacedList(processDir.resolve("unplaced.txt"));
    assertTrue(unplacedEntryNames.contains("IWGSC_CSS_6DL_CONTIG_209591"));
    assertTrue(unplacedEntryNames.contains("IWGSC_CSS_6DL_CONTIG_209592"));
    assertTrue(unplacedEntryNames.contains("IWGSC_CSS_6DL_CONTIG_209593"));
  }

  @Test
  public void skipsUnplacedListForInternalNonAgpExcludedDistributionSubmission() throws Exception {
    Path processDir = temporaryFolder.newFolder("excluded-process").toPath();

    options = createInternalGenomeOptions(processDir, AssemblyType.BINNEDMETAGENOME);
    options.submissionFiles = Optional.of(fastaOnlySubmission());

    ValidationResult result = new SubmissionValidationPlan(options).execute();

    assertTrue(result.getMessages().toString(), result.isValid());
    assertFalse(Files.exists(processDir.resolve("unplaced.txt")));
  }

  private SubmissionOptions createInternalGenomeOptions(
      Path processDir, AssemblyType assemblyType) {
    SubmissionOptions options = new SubmissionOptions();
    options.context = Optional.of(Context.genome);
    options.isWebinCLI = false;
    options.ignoreErrors = true;
    options.reportDir = Optional.of(processDir.toString());
    options.processDir = Optional.of(processDir.toString());
    options.getEntryValidationPlanProperty().analysis_id.set(null);
    options.getEntryValidationPlanProperty().taxonClient.set(null);

    options.enproConnection = Optional.of(mock(Connection.class));
    options.eraproConnection = Optional.of(mock(Connection.class));
    options.webinRestUri = Optional.of("http://localhost");
    options.webinUsername = Optional.of("user");
    options.webinPassword = Optional.of("password");
    options.webinAuthUri = Optional.of("http://localhost/auth");
    options.biosamplesUri = Optional.of("http://localhost/biosamples");
    options.biosamplesWebinUsername = Optional.of("biosamples-user");
    options.biosamplesWebinPassword = Optional.of("biosamples-password");

    if (assemblyType != null) {
      AssemblyInfoEntry assemblyInfoEntry = getAssemblyinfoEntry();
      assemblyInfoEntry.setAssemblyType(assemblyType.getValue());
      options.assemblyInfoEntry = Optional.of(assemblyInfoEntry);
      options.assemblyType = assemblyType;
    }

    options.analysisId = Optional.of("analysis-id");
    return options;
  }

  private SubmissionFiles fastaOnlySubmission() {
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionTestFile("valid_genome_fasta_2.txt", FileType.FASTA));
    return submissionFiles;
  }

  private SubmissionFiles flatfileOnlySubmission() {
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(initSubmissionTestFile("valid_genome_flatfile.txt", FileType.FLATFILE));
    return submissionFiles;
  }

  private SubmissionFiles fastaWithPartialChromosomeAssignment(Path processDir) throws Exception {
    Path chromosomeList = processDir.resolve("chromosome_list.txt");
    Files.writeString(
        chromosomeList,
        "IWGSC_CSS_6DL_contig_209591 I chromosome\n"
            + "IWGSC_CSS_6DL_contig_209592 II chromosome\n",
        StandardCharsets.UTF_8);

    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA));
    submissionFiles.addFile(new SubmissionFile(FileType.CHROMOSOME_LIST, chromosomeList.toFile()));
    return submissionFiles;
  }

  private SubmissionFiles fastaWithUnlocalisedObject(Path processDir) throws Exception {
    Path fasta = processDir.resolve("unlocalised_and_unplaced.fasta");
    Files.writeString(
        fasta,
        ">unlocalised_contig_1\n"
            + "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC\n"
            + ">unplaced_contig_1\n"
            + "GTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTG\n",
        StandardCharsets.UTF_8);
    Path unlocalisedList = processDir.resolve("unlocalised_list.txt");
    Files.writeString(unlocalisedList, "unlocalised_contig_1 I\n", StandardCharsets.UTF_8);

    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(new SubmissionFile(FileType.FASTA, fasta.toFile()));
    submissionFiles.addFile(
        new SubmissionFile(FileType.UNLOCALISED_LIST, unlocalisedList.toFile()));
    return submissionFiles;
  }

  private SubmissionFiles fastaWithAnnotationOnlyFlatfile() {
    SubmissionFiles submissionFiles = new SubmissionFiles();
    submissionFiles.addFile(
        initSubmissionTestFile("valid_fastaforAnnotationOnly.txt", FileType.FASTA));
    submissionFiles.addFile(
        initSubmissionTestFile("valid_AnnotationOnlyFlatfile.txt", FileType.FLATFILE));
    return submissionFiles;
  }

  private Entry createMasterEntry() {
    Entry masterEntry = new Entry();
    masterEntry.setDataClass(Entry.SET_DATACLASS);
    masterEntry.setDescription(new Text("Micrococcus sp. 5 genome assembly"));
    masterEntry.setDivision("PRO");
    masterEntry.addProjectAccession(new Text("PRJEB0"));
    masterEntry.addXRef(new XRef("BioSample", "SMEA091"));

    Sequence sequence = new SequenceFactory().createSequence();
    sequence.setMoleculeType("genomic DNA");
    sequence.setTopology(Sequence.Topology.LINEAR);
    masterEntry.setSequence(sequence);

    SourceFeature source = getSource();
    source.addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
    source.setMasterLocation();
    masterEntry.addFeature(source);
    return masterEntry;
  }

  private Set<?> readUnplacedList(Path unplacedList) throws Exception {
    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(unplacedList))) {
      return (Set<?>) ois.readObject();
    }
  }
}
