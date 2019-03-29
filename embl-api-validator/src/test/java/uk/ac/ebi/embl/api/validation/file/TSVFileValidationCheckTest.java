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

import static org.junit.Assert.assertTrue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.TSVFileValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class TSVFileValidationCheckTest {
	private static final String PROJECT_ID = "PRJEB13102";
	private SubmissionOptions options;
	private FileValidationCheck fileValidationCheck;
	private SubmissionFile submissionFile;
	private Path path = Paths.get(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/sequenceFixed.txt");
	private String reportsPath = System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template";
	private final static String[] allTemplatesA = {"ERT000002-rRNA.tsv.gz",
													"ERT000003-EST-1.tsv.gz",
													"ERT000006-SCM.tsv.gz",
													"ERT000009-ITS.tsv.gz",
													"ERT000020-COI.tsv.gz",
													"ERT000024-GSS-1.tsv.gz",
													"ERT000028-SVC.tsv.gz",
													"ERT000029-SCGD.tsv.gz",
													"ERT000030-MHC1.tsv.gz",
													"ERT000031-viroid.tsv.gz",
													"ERT000032-matK.tsv.gz",
													"ERT000034-Dloop.tsv.gz",
													"ERT000035-IGS.tsv.gz",
													"ERT000036-MHC2.tsv.gz",
													"ERT000037-intron.tsv.gz",
													"ERT000038-hyloMarker.tsv.gz",
													"ERT000039-Sat.tsv.gz",
													"ERT000042-ncRNA.tsv.gz",
													"ERT000047-betasat.tsv.gz",
													"ERT000050-ISR.tsv.gz",
													"ERT000051-poly.tsv.gz",
													"ERT000052-ssRNA.tsv.gz",
													"ERT000053-ETS.tsv.gz",
													"ERT000054-prom.tsv.gz",
													"ERT000055-STS.tsv.gz",
													"ERT000056-mobele.tsv.gz",
													"ERT000057-alphasat.tsv.gz",
													"ERT000058-MLmarker.tsv.gz",
													"ERT000060-vUTR.tsv.gz"};

	@Before
	public void init() throws Exception	{
		try {
			if (Files.exists(path))
				Files.delete(path);
			Files.createFile(path);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		options = new SubmissionOptions();
		options.isRemote = true;
		options.setProjectId(PROJECT_ID);
		options.reportDir = Optional.of(reportsPath);
		fileValidationCheck = new TSVFileValidationCheck(options);
	}

	@Test
	public void allTemplates() {
		try {
			boolean valid = true;
			for (String tsvFile: allTemplatesA) {
				try {
					if (Files.exists(path))
						Files.delete(path);
					Files.createFile(path);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/" + tsvFile), path.toFile());
				if (!fileValidationCheck.check(submissionFile)) {
					valid = false;
					System.out.println("Failed: " + tsvFile);
				}
			}
			assertTrue(valid);
			System.out.println("Finished.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void mandatoryFieldsPresent()  {
		try {
			boolean valid = true;
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/Sequence-mandatory-field-missing-expected-results.txt"), path.toFile());
			fileValidationCheck.check(submissionFile);
//			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void singleCDSInvalidLocation()  {
		try {
			String fileName = "cds29.tsv.gz";
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(reportsPath + File.separator + fileName), path.toFile());
			Assert.assertFalse(fileValidationCheck.check(submissionFile));
			Assert.assertTrue(checkReport(new File(reportsPath + File.separator + fileName +".report"),
					"ERROR: Invalid feature location: <yes..>yes [Sequence: 1 ,  line: 22]"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkReport(File file, String s) throws Exception {
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if(line.contains(s))
					return true;
			}
		}
		return false;
	}

	@Test
	public void ppGenePassedAsMarker()  {
		try {
			boolean valid = true;
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/Sequence-PP_GENE-as-MARKER.tsv.gz"), path.toFile());
			fileValidationCheck.check(submissionFile);
//			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void invalidAlphanumericEntrynumber()  {
		try {
			boolean valid = true;
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/Sequence-invalid-alphanumeric-entrynumber-.tsv.gz"), path.toFile());
			fileValidationCheck.check(submissionFile);
//			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void invalidEntrynumberStart()  {
		try {
			boolean valid = true;
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/Sequence-invalid-entrynumber-start-.tsv.gz"), path.toFile());
			fileValidationCheck.check(submissionFile);
//			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void invalidMarker()  {
		try {
			boolean valid = true;
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File("Sequence-invalid-marker.tsv.gz"), path.toFile());
			fileValidationCheck.check(submissionFile);
//			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void invalidSediment()  {
		try {
			boolean valid = true;
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/Sequence-invalid-sediment.tsv.gz"), path.toFile());
			fileValidationCheck.check(submissionFile);
//			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void nonAsciiCharacters() {
		try {
			boolean valid = true;
			submissionFile = new SubmissionFile(SubmissionFile.FileType.TSV, new File(System.getProperty("user.dir") +"/src/test/resources/uk/ac/ebi/embl/api/validation/file/template/Sequence-non-ascii-characters.gz"), path.toFile());
			fileValidationCheck.check(submissionFile);
//			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void missingHeaderValues() {
		/*
		try {
			StringBuilder validationResults = new StringBuilder();
			SequenceValidator sequenceValidator = new SequenceValidator(10, 1000);
			BufferedInputStream bufferedInputStremSubmittedData = new BufferedInputStream(new GZIPInputStream(new FileInputStream(SEQUENCE_BASE_DIR + "Sequence-missingHeaderValues.gz")));
			HttpStatus httpStatus = sequenceValidator.doSequenceTsvFileValidation(bufferedInputStremSubmittedData, "ERT000029", validationResults);
			assertEquals(HttpStatus.OK, httpStatus);
			String expectedResults = new String(Files.readAllBytes(Paths.get(SEQUENCE_BASE_DIR + "Sequence-missingHeaderValues-expected-results.txt")));
			assertEquals(expectedResults, validationResults.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}

}
