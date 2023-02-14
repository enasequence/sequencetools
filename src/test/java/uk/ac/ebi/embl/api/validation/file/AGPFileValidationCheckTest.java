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
import uk.ac.ebi.embl.api.validation.check.file.AGPFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFiles;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class AGPFileValidationCheckTest extends SubmissionValidationTest
{
	 @Before
	   public void init()
	   {   
		   options = new SubmissionOptions();
	       options.source= Optional.of(getSource());
	       options.assemblyInfoEntry= Optional.of(getAssemblyinfoEntry());
	       options.isWebinCLI = true;
		   options.isDevMode = true;
	   }

		@Test
		public void testGenomeSubmissionwithFlatfileAGP() throws FlatFileComparatorException, ValidationEngineException, IOException
		{
			sharedInfo = new FileValidationCheck.SharedInfo();

			validateMaster(Context.genome);
			String fileName = "valid_flatfileagp.txt";
			options.context = Optional.of(Context.genome);
			SubmissionFiles submissionFiles = new SubmissionFiles();
			submissionFiles.addFile(initSubmissionFixedTestFile(fileName, FileType.AGP));
			options.submissionFiles = Optional.of(submissionFiles);
			options.reportDir = Optional.of(initSubmissionTestFile(fileName, FileType.AGP).getFile().getParent());
			options.processDir = Optional.of(initSubmissionTestFile(fileName, FileType.AGP).getFile().getParent());
			options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
			options.init();
			AGPFileValidationCheck check = new AGPFileValidationCheck(options, sharedInfo);
			try {
				sharedInfo.hasAgp = true;
				sharedInfo.contigDB = DBMaker.fileDB(options.reportDir.get() + File.separator + ".contig").closeOnJvmShutdown().fileDeleteAfterOpen().transactionEnable().make();
				check.createContigDB();
				validateContig("valid_flatfileforAgp.txt", FileType.FLATFILE);
				assertTrue(check.check(submissionFiles.getFiles().get(0)).isValid());
			} finally {
				sharedInfo.contigDB.close();
			}

		}
		
		@Test
		public void testGenomeSubmissionwithFastafileAGP() throws FlatFileComparatorException, ValidationEngineException
		{
			sharedInfo = new FileValidationCheck.SharedInfo();

			String agpFileName = "valid_fastaagp.txt";
			validateMaster(Context.genome);
			options.context = Optional.of(Context.genome);
			SubmissionFiles submissionFiles = new SubmissionFiles();
			submissionFiles.addFile(initSubmissionFixedTestFile(agpFileName, FileType.AGP));
			options.submissionFiles = Optional.of(submissionFiles);
			options.reportDir = Optional.of(initSubmissionTestFile(agpFileName, FileType.AGP).getFile().getParent());
			options.processDir = Optional.of(initSubmissionTestFile(agpFileName, FileType.AGP).getFile().getParent());
			options.init();
			AGPFileValidationCheck check= new AGPFileValidationCheck(options, sharedInfo);
			sharedInfo.hasAgp = true;
			sharedInfo.contigDB = DBMaker.fileDB(options.reportDir.get()+File.separator+".contig").deleteFilesAfterClose().closeOnJvmShutdown().transactionEnable().make();
			check.createContigDB();
			validateContig("valid_fastaforAgp.txt",  FileType.FASTA);
			assertTrue(check.check(submissionFiles.getFiles().get(0)).isValid());
			//assertTrue(compareOutputSequenceFiles(initSubmissionFixedSequenceTestFile("valid_fastaagp.txt.fixed", FileType.FLATFILE).getFile()));
			sharedInfo.contigDB.close();
		}
	 
    	
		private void validateContig(String contigFileName,FileType fileType) throws ValidationEngineException
		{
			SubmissionFile file=initSubmissionFixedTestFile(contigFileName,fileType);
			SubmissionFiles submissionFiles = new SubmissionFiles();
			submissionFiles.addFile(file);
	        options.submissionFiles =Optional.of(submissionFiles);
	        options.reportDir = Optional.of(file.getFile().getParent());
	        options.context = Optional.of(Context.genome);
			FileValidationCheck check =null;
			if(fileType==FileType.FASTA)
			check=new FastaFileValidationCheck(options, sharedInfo);
			if(fileType==FileType.FLATFILE)
			check=new FlatfileFileValidationCheck(options, sharedInfo);
            check.check(file);
            options.submissionFiles.get().clear();
         }
		
}
