package uk.ac.ebi.embl.api.validation.submission;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mapdb.DB;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.check.file.AnnotationOnlyFlatfileValidationCheck;
import uk.ac.ebi.embl.api.validation.file.SubmissionValidationTest;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparator;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;

public class SubmissionValidationPlanTest extends SubmissionValidationTest 
{
    SubmissionOptions options =null;
    @Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void init()
	{
       options = new SubmissionOptions();
       options.isRemote = true;
	   options.assemblyInfoEntry =Optional.of(getAssemblyinfoEntry());
	   options.source = Optional.of(getSource());
	   options.ignoreErrors = true;
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
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
        assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_genome_fasta.txt", FileType.FASTA).getFile()));
        assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_genome_flatfile.txt", FileType.FASTA).getFile()));
	}
	
	@Test
	public void testGenomeSubmissionwithFastaChromosomeListsequenceless() throws ValidationEngineException, FlatFileComparatorException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionFixedTestFile("chromosome_list_sequenceless.txt", FileType.CHROMOSOME_LIST));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile("valid_genome_fasta.txt", FileType.FASTA).getFile().getParent());
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		thrown.expect(ValidationEngineException.class);
		thrown.expectMessage("Sequenceless chromosomes are not allowed in assembly : IWGSC_CSS_6DL_scaff_3330717,IWGSC_CSS_6DL_scaff_3330716,IWGSC_CSS_6DL_scaff_3330719");
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
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaforAgp.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaagp.txt", FileType.FLATFILE).getFile()));
		//assertTrue(compareOutputSequenceFiles(initSubmissionFixedSequenceTestFile("valid_fastaagp.txt.fixed", FileType.FLATFILE).getFile()));
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
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaforAnnotationOnly.txt", FileType.FLATFILE).getFile()));
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_AnnotationOnlyandSequenceFlatfile.txt", FileType.FLATFILE).getFile()));
		String fixedannotationOnlyflatfile = SubmissionValidationTest.class.getClassLoader().getResource( "uk/ac/ebi/embl/api/validation/file/valid_AnnotationOnlyandSequenceFlatfile.txt.annotationOnly.fixed").getPath().replaceAll("%20", " ");
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
		submissionFiles.addFile(initSubmissionFixedTestFile("valid_transcriptom_fasta.txt", FileType.FASTA));
		options.submissionFiles = Optional.of(submissionFiles);
		options.reportDir = Optional.of(initSubmissionTestFile("valid_transcriptom_fasta.txt", FileType.FASTA).getFile().getParent());
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_transcriptom_fasta.txt", FileType.FASTA).getFile()));
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
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
		assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_transcriptom_flatfile.txt", FileType.FLATFILE).getFile()));
	}

	private String getmessage(String fileType,String fileName,String reportDir)
	{
		return fileType+" file validation failed : "+fileName+", Please see the error report: "+ reportDir+File.separator+fileName+".report";
	}
}
