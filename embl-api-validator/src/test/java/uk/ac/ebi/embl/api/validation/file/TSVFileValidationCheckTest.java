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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.ChromosomeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.TSVFileValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class TSVFileValidationCheckTest  extends FileValidationCheckTest {
	public static final String PROJECT_ID = "PRJEB13102";

	@Before
	public void init() throws SQLException	{
		options = new SubmissionOptions();
		options.isRemote = true;
		options.setProjectId(PROJECT_ID);
	}

	@Test
	public void testValidationTemplate() throws ValidationEngineException {
//		String SUBMITTED_DATA_FILE_NAME = "ERT000003-EST.tsv.gz";
		String SUBMITTED_DATA_FILE_NAME = "ERT000002-rRNA.tsv.gz";
		Path path = Paths.get(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/sequenceFixed.txt");
		try {
			if (Files.exists(path))
                Files.delete(path);
			Files.createFile(path);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		FileValidationCheck fileValidationCheck = new TSVFileValidationCheck(options);
		SubmissionFile submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/ERT000002-rRNA.tsv.gz"), path.toFile());
		fileValidationCheck.check(submissionFile);
		System.out.println("Finished.");
	}

}
