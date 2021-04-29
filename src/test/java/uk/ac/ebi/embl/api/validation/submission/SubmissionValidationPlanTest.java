package uk.ac.ebi.embl.api.validation.submission;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.file.SubmissionValidationTest;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparator;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

public class SubmissionValidationPlanTest extends SubmissionValidationTest 
{
    SubmissionOptions options =null;
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

	@Test
	public void testGenomeSubmissionWithFlatfileAddReference() throws ValidationEngineException, FlatFileComparatorException, ParseException {
		String fileName = "valid_genome_flatfile.txt";
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
		String expectedFile = "valid_genome_flatfile_ref_change.txt.expected";
		FlatFileComparator comparator=new FlatFileComparator(new FlatFileComparatorOptions());
		assertTrue( comparator.compare(initSubmissionFixedTestFile(expectedFile, FileType.FLATFILE).getFile().getAbsolutePath(),
				initSubmissionTestFile(fileName, FileType.FLATFILE).getFile().getAbsolutePath()+".fixed"));
	}

	@Test
	public void testGenomeSubmissionwithFastaFlatfile() throws ValidationEngineException, FlatFileComparatorException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_genome_flatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
        assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_genome_fasta.txt", FileType.FASTA).getFile()));
        assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_genome_flatfile.txt", FileType.FASTA).getFile()));
	}
	
	@Test
	public void testGenomeSubmissionwithFastaChromosomeListsequenceless() throws ValidationEngineException, FlatFileComparatorException, IOException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("chromosome_list_sequenceless.txt", FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile("valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());

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
	public void testGenomeSubmissionwitFastawithValidChromosomeList() throws ValidationEngineException, FlatFileComparatorException
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
	public void testGenomeSubmissionwithFlatfileAGP() throws FlatFileComparatorException, ValidationEngineException, IOException, InterruptedException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_flatfileforAgp.txt", FileType.FLATFILE));
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_flatfileagp.txt", FileType.AGP));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("valid_flatfileforAgp.txt", FileType.FLATFILE).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("valid_flatfileforAgp.txt", FileType.FLATFILE).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_flatfileforAgp.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_flatfileagp.txt", FileType.FLATFILE).getFile()));
	//	assertTrue(compareOutputSequenceFiles(initSubmissionFixedSequenceTestFile("valid_flatfileagp.txt.fixed", FileType.FLATFILE).getFile()));
	}
	
	@Test
	public void testGenomeSubmissionwithFastafileAGP() throws FlatFileComparatorException, ValidationEngineException, IOException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_fastaforAgp.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_fastaagp.txt", FileType.AGP));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile("valid_fastaforAgp.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("valid_fastaforAgp.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaforAgp.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaagp.txt", FileType.FLATFILE).getFile()));
		//assertTrue(compareOutputSequenceFiles(initSubmissionFixedSequenceTestFile("valid_fastaagp.txt.fixed", FileType.FLATFILE).getFile()));
	}

	@Test
	public void testGenomeSubmissionwithFastaAGPMultiLevel() throws FlatFileComparatorException, ValidationEngineException, IOException
	{
		String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_scafoold_levels.txt";
		String chrListFileName = "chromosome_list_scaffold_levels.txt";

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
	}

	@Test
	public void testGenomeSubmissionwithFastaAGPWithUnlocalisedList() throws FlatFileComparatorException, ValidationEngineException, IOException
	{
		String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_scafoold_levels.txt";
		String chrListFileName = "chromosome_list_scaffold_levels.txt";
		String unlocalisedListFile = "unlocalised_list_agp.txt";

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
	}

	@Test
	public void testGenomeSubmissionwithFastaAGPWithChrList() throws FlatFileComparatorException, ValidationEngineException, IOException
	{
		String fastaFileName = "valid_fastaforAgp_scaffold_levels.txt";
		String agpFileName = "agp_scafoold_levels_1.txt";
		String chrListFileName = "chromosome_list_scaffold_levels_1.txt";

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
	}

	private void compareUnplacedList(String processDir) {
		List<String> unplacedEntryNames;
		try(ObjectInputStream oos = new ObjectInputStream (new FileInputStream(processDir+File.separator+"unplaced.txt")))
		{
			unplacedEntryNames = (List<String>) oos.readObject();
			assertNotNull(unplacedEntryNames);
			assertEquals(1, unplacedEntryNames.size());
			assertEquals("IWGSC_CSS_6DL_scaff_3330718", unplacedEntryNames.get(0));

		} catch(Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}
	@Test
	public void testGenomeSubmissionwithAnnotationOnlyFile() throws FlatFileComparatorException, ValidationEngineException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_fastaforAnnotationOnly.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_AnnotationOnlyandSequenceFlatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		options.reportDir = Optional.of(initSubmissionTestFile("valid_fastaforAnnotationOnly.txt", FileType.FASTA).getFile().getParent());
		options.processDir = Optional.of(initSubmissionTestFile("valid_fastaforAnnotationOnly.txt", FileType.FASTA).getFile().getParent());

		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaforAnnotationOnly.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_AnnotationOnlyandSequenceFlatfile.txt", FileType.FLATFILE).getFile()));
		String fixedannotationOnlyflatfile = SubmissionValidationTest.class.getClassLoader().getResource( "uk/ac/ebi/embl/api/validation/file/valid_AnnotationOnlyandSequenceFlatfile.txt.annotationOnly.tmp").getPath().replaceAll("%20", " ");
		String expectedannotationOnlyflatfile = SubmissionValidationTest.class.getClassLoader().getResource( "uk/ac/ebi/embl/api/validation/file/valid_AnnotationOnlyandSequenceFlatfile.txt.annotationOnly.expected").getPath().replaceAll("%20", " ");
		FlatFileComparatorOptions options=new FlatFileComparatorOptions();
		FlatFileComparator comparator=new FlatFileComparator(options);
		assertTrue(comparator.compare(expectedannotationOnlyflatfile, fixedannotationOnlyflatfile));
	}
	
	@Test
	public void testValidTranscriptomFastaSubmission() throws ValidationEngineException, FlatFileComparatorException
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
	public void testValidTranscriptomFlatFileSubmission() throws ValidationEngineException, FlatFileComparatorException
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
