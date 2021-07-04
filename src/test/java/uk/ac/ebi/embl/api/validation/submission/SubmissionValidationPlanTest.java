package uk.ac.ebi.embl.api.validation.submission;

import org.junit.*;
import org.junit.rules.ExpectedException;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubmissionValidationPlanTest extends SubmissionValidationTest 
{
    SubmissionOptions options =null;

    @Rule
	public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
	public static void beforeClass() {
    	//to clear out changes made by other tests that might interfere with tests in this class.
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
	}

	// Make sure we store the references given in Webin-CLI manifest.
	@Test
	public void testGenomeWithFlatfileAddReference() throws ValidationEngineException, FlatFileComparatorException, ParseException {
		String rootPath = "genome"+ File.separator+ "flatfile_add_reference" + File.separator;
		String fileName = "valid_genome_flatfile.txt";
		options.context = Optional.of(Context.genome);
		options.assemblyInfoEntry.get().setAuthors("Kirstein I., Wichels A.;");
		options.assemblyInfoEntry.get().setAddress("Biologische Anstalt Helgoland, Alfred-Wegener-Institut, Helmholtz Zentrum " +
				"fur Polar- und Meeresforschung, Kurpromenade 27498 Helgoland, Germany");

		DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		options.assemblyInfoEntry.get().setDate(format.parse("17-JUL-2019"));
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fileName , FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile(rootPath, fileName, FileType.FLATFILE).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath, fileName, FileType.FLATFILE).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		String expectedFile =  "valid_genome_flatfile_ref_change.txt.expected";
		FlatFileComparator comparator=new FlatFileComparator(new FlatFileComparatorOptions());
		assertTrue( comparator.compare(initSubmissionFixedTestFile(rootPath, expectedFile, FileType.FLATFILE).getFile().getAbsolutePath(),
				initSubmissionTestFile(rootPath, fileName, FileType.FLATFILE).getFile().getAbsolutePath()+".fixed"));
	}

	
	@Test
	public void testGenomeWithFastaAGPMultiLevel() throws FlatFileComparatorException, ValidationEngineException
	{
		String rootPath = "genome"+ File.separator+ "multilevel_scaffold" + File.separator;
		String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_scafoold_levels.txt";
		String chrListFileName = "chromosome_list_scaffold_levels.txt";
		options.context = Optional.of(Context.genome);

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, chrListFileName, FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertEquals(1, plan.getUnplacedEntryNames().size());
		assertTrue( plan.getUnplacedEntryNames().contains("IWGSC_CSS_6DL_scaff_3330718".toUpperCase()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath, "valid_fastaforAGP_contigs.reduced.expected"),
				getFileFullPath1(rootPath, FileValidationCheck.contigFileName).getAbsolutePath()));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"valid_fastaforAGP_scaffolds.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.scaffoldFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"valid_fastaforAGP_chromosome.flatfile.expected"),
				getFileFullPath(rootPath, FileValidationCheck.chromosomeFileName)));
	}


	@Test
	public void testGenomeWithFastaFlatfile() throws ValidationEngineException, FlatFileComparatorException
	{
		String rootPath = "genome"+ File.separator+ "fasta_flatfile" + File.separator;
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath , "valid_genome_flatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile(rootPath ,"valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath ,"valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath ,"valid_genome_fasta.txt", FileType.FASTA).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath ,"valid_genome_flatfile.txt", FileType.FASTA).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath, "contigs.reduced.expected"),
				getFileFullPath(rootPath ,FileValidationCheck.contigFileName)));
	}

	@Test
	public void testGenomeWithFastaChromosomeListErrorSequenceless() throws ValidationEngineException, IOException
	{
		String rootPath = "genome"+ File.separator+ "sequenceless_chr" + File.separator;
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,"valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,"chromosome_list_sequenceless.txt", FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile(rootPath,"valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath,"valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

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
		//String rootPath = "genome"+ File.separator+ "sequenceless_chr" + File.separator;
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
	public void testGenomeWithFlatfileAGP() throws FlatFileComparatorException, ValidationEngineException, IOException, InterruptedException
	{
		String rootPath = "genome"+ File.separator+ "agp_flatfile" + File.separator;
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,"valid_flatfileforAgp.txt", FileType.FLATFILE));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,"valid_agp.txt", FileType.AGP));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile(rootPath,"valid_flatfileforAgp.txt", FileType.FLATFILE).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath,"valid_flatfileforAgp.txt", FileType.FLATFILE).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,"valid_flatfileforAgp.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,"valid_agp.txt", FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"contigs.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"scaffolds.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.scaffoldFileName)));
	}

	@Test
	public void testGenomeWithFastaAGP() throws FlatFileComparatorException, ValidationEngineException
	{
		String rootPath = "genome"+ File.separator+ "agp_fasta" + File.separator;
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, "valid_fastaforAgp.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,"valid_fastaagp.txt", FileType.AGP));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile(rootPath,"valid_fastaforAgp.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath,"valid_fastaforAgp.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,"valid_fastaforAgp.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,"valid_fastaagp.txt", FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"contigs.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"scaffolds.reduced.expected"),
				getFileFullPath(rootPath,FileValidationCheck.scaffoldFileName)));
	}

	@Test
	public void testGenomeWithFastaAGPUnlocalisedList() throws FlatFileComparatorException, ValidationEngineException, IOException
	{
		String rootPath = "genome"+ File.separator+ "agp_unlocalised" + File.separator;

		String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_scaffold_levels.txt";
		String chrListFileName = "chromosome_list_scaffold_levels.txt";
		String unlocalisedListFile = "unlocalised_list_agp.txt";

		options.context = Optional.of(Context.genome);

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,chrListFileName, FileType.CHROMOSOME_LIST));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,unlocalisedListFile, FileType.UNLOCALISED_LIST));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(rootPath,fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath,fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(plan.getUnplacedEntryNames().isEmpty());
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"valid_fastaforAGP_contigs.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"valid_fastaforAGP_scaffolds.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.scaffoldFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"valid_fastaforAGP_chromosome.flatfile.expected"),
				getFileFullPath(rootPath,FileValidationCheck.chromosomeFileName)));
	}

	@Test
	public void testGenomeWithFastaAGPChromosomeList() throws FlatFileComparatorException, ValidationEngineException
	{
		String rootPath = "genome"+ File.separator+ "agp_fasta_chr_list" + File.separator;

		String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_scafoold_levels_1.txt";
		String chrListFileName = "chromosome_list_scaffold_levels_1.txt";

		options.context = Optional.of(Context.genome);

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, chrListFileName, FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath, fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(plan.getUnplacedEntryNames().isEmpty());
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath, agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"contigs.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"chromosome.flatfile.expected"),
				getFileFullPath(rootPath, FileValidationCheck.chromosomeFileName)));
		assertTrue(plan.getUnplacedEntryNames().isEmpty());
	}

	@Test
	public void testGenomeWithFasta() throws ValidationEngineException, FlatFileComparatorException
	{
		String rootPath = "genome"+ File.separator+ "fasta" + File.separator;
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath, "valid_genome_fasta.txt", FileType.FASTA).getFile()));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"contigs.reduced.expected"),
				getFileFullPath(rootPath,  FileValidationCheck.contigFileName)));
	}

	@Test
	public void testGenomeSubmissionFastaAnnotationOnly() throws FlatFileComparatorException, ValidationEngineException
	{
		String rootPath = "genome"+ File.separator+ "annotation_only" + File.separator;
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, "valid_fastaforAnnotationOnly.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,"valid_AnnotationOnly_flatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile(rootPath,"valid_fastaforAnnotationOnly.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath,"valid_fastaforAnnotationOnly.txt", FileType.FASTA).getFile().getParent());
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,"valid_fastaforAnnotationOnly.txt", FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"contigs.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.contigFileName)));
	}

	@Test
	public void testGenomeWithFastaAGPAnnotationOnly() throws FlatFileComparatorException, ValidationEngineException
	{
		//anootation file has annotations for 1 contig, 1 scaffold and 1 chromosome ,
		// after execution this will be verified by comparing contigs and scaffolds reduced file and chromosome flatfile.
		// also verified by comparing existing enapro loading flatfiles.
		String rootPath = "genome"+ File.separator+ "agp_annotation_only" + File.separator;
		String fastaFileName = "valid_fasta.txt";
		String agpFileName = "valid_agp.txt";
		String chrListFileName = "valid_chromosome_list.txt";
		String annotationFileName = "valid_annotation_only_flatfile.txt";

		options.context = Optional.of(Context.genome);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));

		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath, fastaFileName, FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,agpFileName, FileType.AGP));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,chrListFileName, FileType.CHROMOSOME_LIST));
		submissionFiles.addFile(initSubmissionFixedTestFile(rootPath,annotationFileName, FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);

		options.reportDir = Optional.of(initSubmissionTestFile(rootPath,fastaFileName, FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile(rootPath,fastaFileName, FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();

		assertEquals(plan.getUnplacedEntryNames().size(), 1);
		assertEquals(plan.getUnplacedEntryNames().toArray()[0].toString(), "IWGSC_CSS_6DL_SCAFF_3330717");

		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,fastaFileName, FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile(rootPath,agpFileName, FileType.FLATFILE).getFile()));

		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"contigs.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.contigFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"scaffolds.reduced.expected"),
				getFileFullPath(rootPath, FileValidationCheck.scaffoldFileName)));
		assertTrue(compareOutputFixedFiles(
				getFileFullPath(rootPath,"chromosome.flatfile.expected"),
				getFileFullPath(rootPath,FileValidationCheck.chromosomeFileName)));
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

	@Test
	public void testGenomeSubmissionwitFastawithValidChromosomeListMultipleTimesInParallel() {
		List<CompletableFuture<Void>> futs = new ArrayList<>();
		for (int i = 0; i < 32; i++) {
			int taskId = i + 1;

			futs.add(CompletableFuture.runAsync(() -> {
				SubmissionFiles submissionFiles = new SubmissionFiles();
				submissionFiles.addFile(initSubmissionFixedTestFile(
						"valid_genome_fasta_2.txt", FileType.FASTA));
				submissionFiles.addFile(initSubmissionFixedTestFile(
						"chromosome_list_2.txt", FileType.CHROMOSOME_LIST));

				File reportProcessDir = submissionFiles.getFiles().get(0).getFile().getParentFile().toPath().resolve("" + taskId).toFile();
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
}
