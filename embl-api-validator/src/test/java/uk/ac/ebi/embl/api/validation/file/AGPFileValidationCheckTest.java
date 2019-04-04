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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFiles;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public class AGPFileValidationCheckTest extends SubmissionValidationTest
{
	 @Before
	   public void init() throws SQLException
	   {   
		   options = new SubmissionOptions();
	       options.source= Optional.of(getSource());
	       options.assemblyInfoEntry= Optional.of(getAssemblyinfoEntry());
	       options.isRemote = true;

	   }
	
		@Test
		public void testGenomeSubmissionwithFlatfileAGP() throws FlatFileComparatorException, ValidationEngineException, IOException
		{  
			validateMaster(Context.genome);
			options.context = Optional.of(Context.genome);
			SubmissionFiles submissionFiles = new SubmissionFiles();
			submissionFiles.addFile(initSubmissionFixedTestFile("valid_flatfileagp.txt", FileType.AGP));
			options.submissionFiles = Optional.of(submissionFiles);
			options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
			options.reportDir = Optional.of(initSubmissionTestFile("valid_flatfileagp.txt", FileType.AGP).getFile().getParent());
			options.processDir = Optional.of(initSubmissionTestFile("valid_flatfileagp.txt", FileType.AGP).getFile().getParent());

        	options.init();
			AGPFileValidationCheck check= new AGPFileValidationCheck(options);
			check.setSequenceDB(DBMaker.fileDB(options.reportDir.get()+File.separator+".sequence").deleteFilesAfterClose().closeOnJvmShutdown().transactionEnable().make());
			check.setContigDB(DBMaker.fileDB(options.reportDir.get()+File.separator+".contig").deleteFilesAfterClose().closeOnJvmShutdown().transactionEnable().make());
			check.getAGPEntries();
			validateContig("valid_flatfileforAgp.txt",  FileType.FLATFILE,check.getContigDB());
			assertTrue(check.check(submissionFiles.getFiles().get(0)));
			assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_flatfileforAgp.txt", FileType.FLATFILE).getFile()));
			assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_flatfileagp.txt", FileType.AGP).getFile()));
			//assertTrue(compareOutputSequenceFiles(initSubmissionFixedSequenceTestFile("valid_flatfileagp.txt.fixed", FileType.FLATFILE).getFile()));
			ConcurrentMap map = check.getSequenceDB().hashMap("map").createOrOpen();
			assertEquals("gggactctccaacggctccccgaggagctcgagaggacgattaagtcatcctcgagggacctcgcccgaggagcggtggagctcgtactggcgagttaccaggccaggaccccgacttctccccatggacggcgctggacgagttccctcccgggaccgaggacggcgcgcgcgcgcaggtccgggacgccgccgaccacatcgtccacagcttcgagggttcggcccctcagctcgcgttctccctcaactccgacgaggaggacgatgacggcggagtgggcgacagtggcgacgaggctggcgatccgggtgcatcggagtgagcccnnnnnnnnnnnnnnnnnnnnnnnngggactctccaacggctccccgaggagctcgagaggacgattaagtcatcctcgagggacctcgcccgaggagcggtggagctcgtactggcgagttaccaggccaggaccccgacttctccccatggacggcgctggacgagttccctcccgggaccgaggacggcgcgcgcgcgcaggtccgggacgccgccgaccacatcgtccacagcttcgagggttcggcccctcagctcgcgttctccctcaactccgacgaggaggacgatgacggcggagtgggcgacagtggcgacgagg",map.get("IWGSC_CSS_6DL_scaff_3330716".toUpperCase()));
			assertEquals("gggactctccaacggctccccgaggagctcgagaggacgattaagtcatcctcgagggacctcgcccgaggagcggtggagctcgtactggcgagttaccaggccaggaccccgacttctccccatggacggcgctggacgagttccctcccgggaccgaggacggcgcgcgcgcgcaggtccgggacgccgccgaccacatcgtccacagcttcgagggttcggcccctcagctcgcgttctccctcaactccgacgaggaggacgatgacggcggagtgggcgacagtggcgacgaggctggcgatccgggtgcatcggagtgagccc",map.get("IWGSC_CSS_6DL_scaff_3330717".toUpperCase()));
	        check.getSequenceDB().close();
	        check.getContigDB().close();
		}
		
		@Test
		public void testGenomeSubmissionwithFastafileAGP() throws FlatFileComparatorException, ValidationEngineException
		{
			validateMaster(Context.genome);
			options.context = Optional.of(Context.genome);
			SubmissionFiles submissionFiles = new SubmissionFiles();
			submissionFiles.addFile(initSubmissionFixedTestFile("valid_fastaagp.txt", FileType.AGP));
			options.submissionFiles = Optional.of(submissionFiles);
			options.reportDir = Optional.of(initSubmissionTestFile("valid_fastaagp.txt", FileType.AGP).getFile().getParent());
			options.processDir = Optional.of(initSubmissionTestFile("valid_fastaagp.txt", FileType.AGP).getFile().getParent());
			options.init();
			AGPFileValidationCheck check= new AGPFileValidationCheck(options);
			check.setContigDB(DBMaker.fileDB(options.reportDir.get()+File.separator+".contig").deleteFilesAfterClose().closeOnJvmShutdown().transactionEnable().make());
			check.getAGPEntries();
			validateContig("valid_fastaforAgp.txt",  FileType.FASTA,check.getContigDB());
			assertTrue(check.check(submissionFiles.getFiles().get(0)));
			assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaforAgp.txt", FileType.FLATFILE).getFile()));
			assertTrue(compareOutputFixedFiles(initSubmissionFixedTestFile("valid_fastaagp.txt", FileType.FLATFILE).getFile()));
			//assertTrue(compareOutputSequenceFiles(initSubmissionFixedSequenceTestFile("valid_fastaagp.txt.fixed", FileType.FLATFILE).getFile()));
			check.getContigDB().close();
		}
	 
    	
		private void validateContig(String contigFileName,FileType fileType,DB fileDB) throws ValidationEngineException
		{
			SubmissionFile file=initSubmissionFixedTestFile(contigFileName,fileType);
			SubmissionFiles submissionFiles = new SubmissionFiles();
			submissionFiles.addFile(file);
	        options.submissionFiles =Optional.of(submissionFiles);
	        options.reportDir = Optional.of(file.getFile().getParent());
	        options.context = Optional.of(Context.genome);
			FileValidationCheck check =null;
			if(fileType==FileType.FASTA)
			check=new FastaFileValidationCheck(options);
			if(fileType==FileType.FLATFILE)
			check=new FlatfileFileValidationCheck(options);
			check.setContigDB(fileDB);
            check.check(file);
            options.submissionFiles.get().clear();
         }
		
}
