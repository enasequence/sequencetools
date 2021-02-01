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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.AnnotationOnlyFlatfileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFiles;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

@Description("")
public class AnnotationOnlyFlatFileValidationCheckTest extends SubmissionValidationTest
{
	FileValidationCheck  check =null;
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
	public void testAnnotationOnlyFlatFile() throws ValidationEngineException, FlatFileComparatorException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionFixedTestFile("valid_AnnotationOnlyFlatfile.txt",SubmissionFile.FileType.ANNOTATION_ONLY_FLATFILE);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(file);
		options.reportDir = Optional.of(file.getFile().getParent());
		options.processDir = Optional.of(file.getFile().getParent());
		options.locusTagPrefixes = Optional.of(new ArrayList<>(Collections.singletonList("SPLC1")));
		FileValidationCheck.setHasAnnotationOnlyFlatfile(true);
		DB db=DBMaker.fileDB(options.reportDir.get()+File.separator+".sequence3").deleteFilesAfterClose().closeOnJvmShutdown().transactionEnable().make();
		validateContig("valid_fastaforAnnotationOnly.txt",FileType.FASTA,db);
		options.submissionFiles =Optional.of(submissionFiles);
		options.context = Optional.of(Context.genome);
		options.init();
		check = new AnnotationOnlyFlatfileValidationCheck(options);
		check.setSequenceDB(db);
		assertTrue(!check.check(file).hasError());
		assertTrue(compareOutputFixedFiles(file.getFile()));
		db.close();
	}
	
	@Test
	public void testAnnotationOnlyFlatFilemissingSequence() throws ValidationEngineException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionFixedTestFile("invalid_AnnotationOnlyFlatfile.txt",SubmissionFile.FileType.FLATFILE);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(file);
		options.reportDir = Optional.of(file.getFile().getParent());
		options.processDir = Optional.of(file.getFile().getParent());
		FileValidationCheck.setHasAnnotationOnlyFlatfile(true);
		DB db=DBMaker.fileDB(options.reportDir.get()+File.separator+".sequence2").deleteFilesAfterClose().closeOnJvmShutdown().transactionEnable().make();
		validateContig("valid_fastaforAnnotationOnly.txt",FileType.FASTA,db);
		options.submissionFiles =Optional.of(submissionFiles);
		options.context = Optional.of(Context.genome);
		options.init();
		check = new AnnotationOnlyFlatfileValidationCheck(options);
		check.setSequenceDB(db);
		assertTrue(check.check(file).hasError());
		assertEquals(1l,check.getMessageStats().get("SequenceExistsCheck").get());
		db.close();
	}



	private void validateContig(String contigFileName,FileType fileType,DB db) throws ValidationEngineException
	{
		SubmissionFile file=initSubmissionFixedTestFile(contigFileName,fileType);
		SubmissionFiles submissionFiles = new SubmissionFiles();
		submissionFiles.addFile(file);
		options.submissionFiles =Optional.of(submissionFiles);
		options.reportDir = Optional.of(file.getFile().getParent());
		options.processDir = Optional.of(file.getFile().getParent());
		options.context = Optional.of(Context.genome);
		if(fileType==FileType.FASTA)
			check=new FastaFileValidationCheck(options);
		if(fileType==FileType.FLATFILE)
			check=new FlatfileFileValidationCheck(options);
		check.setSequenceDB(db);
		check.check(file);
		options.submissionFiles.get().clear();
	}

}
