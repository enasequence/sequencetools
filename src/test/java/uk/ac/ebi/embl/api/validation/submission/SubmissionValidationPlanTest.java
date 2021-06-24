package uk.ac.ebi.embl.api.validation.submission;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.file.SubmissionValidationTest;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparator;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubmissionValidationPlanTest extends SubmissionValidationTest 
{

    @Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() {
		options = new SubmissionOptions();
		options.isWebinCLI = true;
		options.assemblyInfoEntry = Optional.of(getAssemblyinfoEntry());
		options.source = Optional.of(getSource());
		options.ignoreErrors = true;
		options.isDevMode = true;
	}

	// Make sure we store the references given in Webin-CLI manifest.
	@Test
	public void testGenomeSubmissionWithFlatfileAddReference() throws ValidationEngineException, FlatFileComparatorException, ParseException {
		String fileName = "flatfile_add_reference/valid_genome_flatfile.txt";
		options.context = Optional.of(Context.genome);
		options.assemblyInfoEntry.get().setAuthors("Kirstein I., Wichels A.;");
		options.assemblyInfoEntry.get().setAddress("Biologische Anstalt Helgoland, Alfred-Wegener-Institut, Helmholtz Zentrum " +
				"fur Polar- und Meeresforschung, Kurpromenade 27498 Helgoland, Germany");

		DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		options.assemblyInfoEntry.get().setDate(format.parse("17-JUL-2019"));
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(fileName , FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile(fileName, FileType.FLATFILE).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(fileName, FileType.FLATFILE).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		String expectedFile = "flatfile_add_reference/valid_genome_flatfile_ref_change.txt.expected";
		FlatFileComparator comparator=new FlatFileComparator(new FlatFileComparatorOptions());
		assertTrue( comparator.compare(initSubmissionFixedTestFile(expectedFile, FileType.FLATFILE).getFile().getAbsolutePath(),
				initSubmissionTestFile(fileName, FileType.FLATFILE).getFile().getAbsolutePath()+".fixed"));
	}

	@Test
	public void testGenomeSubmissionWithFastaFlatfile() throws ValidationEngineException, FlatFileComparatorException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("fasta_flatfile/valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("fasta_flatfile/valid_genome_flatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("fasta_flatfile/valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("fasta_flatfile/valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
        assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("fasta_flatfile/valid_genome_fasta.txt", FileType.FASTA).getFile()));
        assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("fasta_flatfile/valid_genome_flatfile.txt", FileType.FASTA).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("fasta_flatfile/contigs.reduced.expected"),
				getFileFullPath("fasta_flatfile" + File.separator + FileValidationCheck.contigFileName)));
	}
	
	@Test
	public void testGenomeSubmissionWithFastaAGPMultiLevel() throws FlatFileComparatorException, ValidationEngineException, IOException
	{
		String fastaFileName = "multilevel_scaffold/valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "multilevel_scaffold/agp_scafoold_levels.txt";
		String chrListFileName = "multilevel_scaffold/chromosome_list_scaffold_levels.txt";
		options.context = Optional.of(Context.genome);

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(chrListFileName, FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertEquals(1, SubmissionValidationPlan.getUnplacedEntryNames().size());
		assertTrue( SubmissionValidationPlan.getUnplacedEntryNames().contains("IWGSC_CSS_6DL_scaff_3330718".toUpperCase()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("multilevel_scaffold/valid_fastaforAGP_contigs.reduced.expected"),
				getFileFullPath("multilevel_scaffold" + File.separator + FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("multilevel_scaffold/valid_fastaforAGP_scaffolds.reduced.expected"),
				getFileFullPath("multilevel_scaffold" + File.separator + FileValidationCheck.scaffoldFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("multilevel_scaffold/valid_fastaforAGP_chromosome.flatfile.expected"),
				getFileFullPath("multilevel_scaffold" + File.separator + FileValidationCheck.chromosomeFileName)));
	}

	@Test
	public void testGenomeSubmissionWithFastaChromosomeListErrorSequenceless() throws ValidationEngineException, IOException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("sequenceless_chr/valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("sequenceless_chr/chromosome_list_sequenceless.txt", FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile("sequenceless_chr/valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("sequenceless_chr/valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

		Files.deleteIfExists(Paths.get(options.reportDir.get()+File.separator+"fasta.info"));
		Files.deleteIfExists(Paths.get(options.reportDir.get()+File.separator+"agp.info"));
		Files.deleteIfExists(Paths.get(options.reportDir.get()+File.separator+"flatfile.info"));
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		thrown.expect(ValidationEngineException.class);
		thrown.expectMessage("Sequenceless chromosomes are not allowed in assembly : IWGSC_CSS_6DL_SCAFF_3330719,IWGSC_CSS_6DL_SCAFF_3330717,IWGSC_CSS_6DL_SCAFF_3330716");
		plan.execute();
	}
	
	@Test
	@Ignore
	public void testGenomeSubmissionWithFastawithValidChromosomeList() throws ValidationEngineException, FlatFileComparatorException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("chromosome_list.txt", FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		thrown.expect(ValidationEngineException.class);
		//thrown.expectMessage(getmessage("fasta",initSubmissionFixedTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA).getFile().getName(), options.reportDir.get()));
		plan.execute();
		//assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_genome_fasta_chromosome.txt", FileType.FASTA).getFile()));
	}

	@Test
	public void testGenomeSubmissionWithFlatfileAGP() throws FlatFileComparatorException, ValidationEngineException, IOException, InterruptedException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("agp_flatfile/valid_flatfileforAgp.txt", FileType.FLATFILE));
		submissionFiles.addFile(initSubmissionFixedTestFile("agp_flatfile/valid_agp.txt", FileType.AGP));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("agp_flatfile/valid_flatfileforAgp.txt", FileType.FLATFILE).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("agp_flatfile/valid_flatfileforAgp.txt", FileType.FLATFILE).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("agp_flatfile/valid_flatfileforAgp.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("agp_flatfile/valid_agp.txt", FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_flatfile/contigs.reduced.expected"),
				getFileFullPath("agp_flatfile" + File.separator + FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_flatfile/scaffolds.reduced.expected"),
				getFileFullPath("agp_flatfile" + File.separator + FileValidationCheck.scaffoldFileName)));
	}

	@Test
	public void testGenomeSubmissionFastaWithFastaAGP() throws FlatFileComparatorException, ValidationEngineException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("agp_fasta/valid_fastaforAgp.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("agp_fasta/valid_fastaagp.txt", FileType.AGP));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile("agp_fasta/valid_fastaforAgp.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("agp_fasta/valid_fastaforAgp.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("agp_fasta/valid_fastaforAgp.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("agp_fasta/valid_fastaagp.txt", FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_fasta/contigs.reduced.expected"),
				getFileFullPath("agp_fasta" + File.separator + FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_fasta/scaffolds.reduced.expected"),
				getFileFullPath("agp_fasta" + File.separator + FileValidationCheck.scaffoldFileName)));
	}

	@Test
	public void testGenomeSubmissionWithFastaAGPUnlocalisedList() throws FlatFileComparatorException, ValidationEngineException, IOException
	{
		String fastaFileName = "agp_unlocalised/valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_unlocalised/agp_scaffold_levels.txt";
		String chrListFileName = "agp_unlocalised/chromosome_list_scaffold_levels.txt";
		String unlocalisedListFile = "agp_unlocalised/unlocalised_list_agp.txt";

		options.context = Optional.of(Context.genome);

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(chrListFileName, FileType.CHROMOSOME_LIST));
		submissionFiles.addFile(initSubmissionFixedTestFile(unlocalisedListFile, FileType.UNLOCALISED_LIST));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(SubmissionValidationPlan.getUnplacedEntryNames().isEmpty());
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_unlocalised/valid_fastaforAGP_contigs.reduced.expected"),
				getFileFullPath("agp_unlocalised" + File.separator + FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_unlocalised/valid_fastaforAGP_scaffolds.reduced.expected"),
				getFileFullPath("agp_unlocalised" + File.separator + FileValidationCheck.scaffoldFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_unlocalised/valid_fastaforAGP_chromosome.flatfile.expected"),
				getFileFullPath("agp_unlocalised" + File.separator + FileValidationCheck.chromosomeFileName)));
	}

	@Test
	public void testGenomeSubmissionWithFastaAGPChromosomeList() throws FlatFileComparatorException, ValidationEngineException
	{
		String fastaFileName = "agp_fasta_chr_list/valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_fasta_chr_list/agp_scafoold_levels_1.txt";
		String chrListFileName = "agp_fasta_chr_list/chromosome_list_scaffold_levels_1.txt";

		options.context = Optional.of(Context.genome);

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(chrListFileName, FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(SubmissionValidationPlan.getUnplacedEntryNames().isEmpty());
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_fasta_chr_list/contigs.reduced.expected"),
				getFileFullPath("agp_fasta_chr_list" + File.separator + FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_fasta_chr_list/chromosome.flatfile.expected"),
				getFileFullPath("agp_fasta_chr_list" + File.separator + FileValidationCheck.chromosomeFileName)));
	}

	@Test
	public void testGenomeSubmissionFasta() throws ValidationEngineException, FlatFileComparatorException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("fasta/valid_genome_fasta.txt", FileType.FASTA));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("fasta/valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("fasta/valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("fasta/valid_genome_fasta.txt", FileType.FASTA).getFile()));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("fasta/contigs.reduced.expected"),
				getFileFullPath("fasta" + File.separator + FileValidationCheck.contigFileName)));
	}

	@Test
	public void testGenomeSubmissionFastaAnnotationOnly() throws FlatFileComparatorException, ValidationEngineException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("annotation_only/valid_fastaforAnnotationOnly.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("annotation_only/valid_AnnotationOnly_flatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("annotation_only/valid_fastaforAnnotationOnly.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("annotation_only/valid_fastaforAnnotationOnly.txt", FileType.FASTA).getFile().getParent());
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("annotation_only/valid_fastaforAnnotationOnly.txt", FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("annotation_only/contigs.reduced.expected"),
				getFileFullPath("annotation_only" + File.separator + FileValidationCheck.contigFileName)));
	}

	@Test
	public void testGenomeSubmissionFastaAGPAnnotationOnly() throws FlatFileComparatorException, ValidationEngineException
	{
		//anootation file has annotations for 1 contig, 1 scaffold and 1 chromosome ,
		// after execution this will be verified by comparing contigs and scaffolds reduced file and chromosome flatfile.
		// also verified by comparing existing enapro loading flatfiles.

		String fastaFileName = "agp_annotation_only/valid_fasta.txt";
		String agpFileName = "agp_annotation_only/valid_agp.txt";
		String chrListFileName = "agp_annotation_only/valid_chromosome_list.txt";
		String annotationFileName = "agp_annotation_only/valid_annotation_only_flatfile.txt";

		options.context = Optional.of(Context.genome);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(chrListFileName, FileType.CHROMOSOME_LIST));
		submissionFiles.addFile(initSubmissionFixedTestFile(annotationFileName, FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();

		assertEquals(SubmissionValidationPlan.getUnplacedEntryNames().size(), 1);
		assertEquals(SubmissionValidationPlan.getUnplacedEntryNames().toArray()[0].toString(), "IWGSC_CSS_6DL_SCAFF_3330717");

		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_annotation_only/contigs.reduced.expected"),
				getFileFullPath("agp_annotation_only" + File.separator + FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_annotation_only/scaffolds.reduced.expected"),
				getFileFullPath("agp_annotation_only" + File.separator + FileValidationCheck.scaffoldFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath("agp_annotation_only/chromosome.flatfile.expected"),
				getFileFullPath("agp_annotation_only" + File.separator + FileValidationCheck.chromosomeFileName)));
	}

	@Test
	public void testValidTranscriptomeFastaSubmission() throws ValidationEngineException, FlatFileComparatorException
	{
		options.context = Optional.of(Context.transcriptome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		SubmissionFile subFile = initSubmissionFixedTestFile("valid_transcriptom_fasta.txt", FileType.FASTA);
		submissionFiles.addFile(subFile);
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(subFile.getFile().getParent());
		options.processDir = Optional.of(subFile.getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(subFile.getFile()));
	}
	
	@Test
	public void testValidTranscriptomeFlatFileSubmission() throws ValidationEngineException, FlatFileComparatorException
	{
		options.context = Optional.of(Context.transcriptome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE).getFile()));
	}

	@Test
	public void testTranscriptomeSuplicateEntryNameFlatFile() throws ValidationEngineException
	{
		options.context = Optional.of(Context.transcriptome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		SubmissionFile subFile = initSubmissionFixedTestFile("transcriptom_flatfile_duplicate_entryname.txt", FileType.FLATFILE);
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

}
