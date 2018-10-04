package uk.ac.ebi.embl.api.validation.submission;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.file.SubmissionValidationTest;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public class SubmissionValidationPlanTest extends SubmissionValidationTest 
{
    SubmissionOptions options =null;
	
	@Before
	public void init()
	{
       options = new SubmissionOptions();
       options.isRemote = true;
	   options.assemblyInfoEntry =Optional.of(getAssemblyinfoEntry());
	   options.source = Optional.of(getSource());
    }
	
	@Test
	public void testGenomeSubmissionwithFastaFlatfile() throws ValidationEngineException
	{
		options.context = Optional.of(Context.genome);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(initSubmissionTestFile("valid_genome_fasta.txt", FileType.FASTA));
		submissionFiles.addFile(initSubmissionTestFile("valid_genome_flatfile.txt", FileType.FLATFILE));
		options.submissionFiles = Optional.of(submissionFiles);
		SubmissionValidationPlan plan = new SubmissionValidationPlan(options);
		plan.execute();
	}
	
	@Test
	public void testGenomeSubmissionwithnoChromosomeList()
	{
		
	}
	
	@Test
	public void testGenomeSubmissionwitChromosomeList()
	{
		
	}
	
	@Test
	public void testGenomeSubmissionwithAGP()
	{
		
	}
	
	@Test
	public void testValidTranscriptomSubmission()
	{
		
	}

	@Test
	public void testInvalidTranscriptomSubmission()
	{
		
	}

}
