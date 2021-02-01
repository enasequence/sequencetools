/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.file;

import org.junit.Before;
import org.junit.Test;
import org.mapdb.DBMaker;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFiles;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@Description("")
public class FastaFileValidationCheckTest extends SubmissionValidationTest
{
   @Before
   public void init() throws SQLException
   {   
	   options = new SubmissionOptions();
       options.source= Optional.of(getSource());
       options.assemblyInfoEntry= Optional.of(getAssemblyinfoEntry());
       options.isWebinCLI = true;
	   options.isDevMode = true;
   }
	
	@Test
	public void testInvalidFastaFile() throws ValidationEngineException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionTestFile("invalid_fasta_sequence.txt",SubmissionFile.FileType.FASTA);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(file);
        options.submissionFiles =Optional.of(submissionFiles);
        options.reportDir = Optional.of(file.getFile().getParent());
        options.context = Optional.of(Context.genome);
		FastaFileValidationCheck check = new FastaFileValidationCheck(options);
		assertTrue(check.check(file).hasError());
		assertTrue(check.getMessageStats().get("SQ.1")!=null);
	}
	
	@Test
	public void testTranscriptomFixedvalidFastaFile() throws ValidationEngineException, FlatFileComparatorException
	{
		validateMaster(Context.transcriptome);
		SubmissionFile file=initSubmissionFixedTestFile("valid_transcriptom_fasta.txt",SubmissionFile.FileType.FASTA);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(file);
        options.submissionFiles =Optional.of(submissionFiles);
        options.reportDir = Optional.of(file.getFile().getParent());
        options.processDir = Optional.of(file.getFile().getParent());
        options.context = Optional.of(Context.transcriptome);
        options.init();
		FastaFileValidationCheck check = new FastaFileValidationCheck(options);
		assertTrue(!check.check(file).hasError());
        assertTrue(compareOutputFixedFiles(file.getFile()));
	}
	
	@Test
	public void testgenomeFixedvalidFastaFile() throws ValidationEngineException, FlatFileComparatorException, IOException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionFixedTestFile("valid_genome_fasta.txt",SubmissionFile.FileType.FASTA);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(file);
        options.submissionFiles =Optional.of(submissionFiles);
        options.reportDir = Optional.of(file.getFile().getParent());
        options.processDir = Optional.of(file.getFile().getParent());
        options.context = Optional.of(Context.genome);
        options.init();
		FastaFileValidationCheck check = new FastaFileValidationCheck(options);
		check.setSequenceDB(DBMaker.fileDB(options.reportDir.get()+File.separator+".sequence1").closeOnJvmShutdown().fileDeleteAfterClose().transactionEnable().make());
		assertTrue(!check.check(file).hasError());
		String expectedString = new String(Files.readAllBytes(Paths.get(file.getFile().getAbsolutePath()+".expected")));
		String actualString = new String(Files.readAllBytes(Paths.get(file.getFile().getAbsolutePath()+".fixed")));
        System.out.println(expectedString);
        System.out.println("=====================================================================");
        System.out.println(actualString);
        assertTrue(compareOutputFixedFiles(file.getFile()));
     /*   ConcurrentMap map = check.getSequenceDB().hashMap("map").createOrOpen();
        assertEquals("caaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac",map.get("IWGSC_CSS_6DL_contig_209591".toUpperCase()));
        assertEquals("gttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggttttttttttttttttttttttttttttttttttttttttttttttttttttttttttggtttttttttttttttttttttttttttttttttg",map.get("IWGSC_CSS_6DL_contig_209592".toUpperCase()));
        assertEquals("aggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggggggggggggggggggggggggggggaaggggggggggggggggggggggggggggggggga",map.get("IWGSC_CSS_6DL_contig_209593".toUpperCase()));
        check.getSequenceDB().close();*/
	}
}
