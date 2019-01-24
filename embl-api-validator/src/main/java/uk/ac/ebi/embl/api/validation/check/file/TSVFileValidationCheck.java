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
package uk.ac.ebi.embl.api.validation.check.file;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;
import uk.ac.ebi.embl.template.*;

@Description("")
public class TSVFileValidationCheck extends FileValidationCheck {
	public final static String TEMPLATE_FILE_NAME = "TEMPLATE_";
	private final static String TEMPLATE_ID_PATTERN = "(ERT[0-9]+)";
	private final static String TEMPLATE_ACCESSION_LINE = "#template_accession";
	private final static int MAX_SEQUENCE_COUNT = 100000;

	public TSVFileValidationCheck(SubmissionOptions options) {
		super(options);
	}

	@Override
	public boolean check(SubmissionFile submissionFile) throws ValidationEngineException {
		boolean valid = true;
		try (PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile)) {
             clearReportFile(getReportFile(submissionFile));
			String templateId = getTemplateIdFromTsvFile(submissionFile.getFile());
			File submittedDataFile =  submissionFile.getFile();
			String templateDir = submittedDataFile.getParent();
			File templateFile = getTemplateFromResourceAndWriteToProcessDir(templateId, templateDir);
			TemplateLoader templateLoader = new TemplateLoader();
			if (!submittedDataFile.exists())
				throw new ValidationEngineException(submittedDataFile.getAbsolutePath() +  " file does not exist");
			TemplateInfo templateInfo = templateLoader.loadTemplateFromFile(templateFile);
			TemplateProcessor templateProcessor;
			if (options.isRemote)
				templateProcessor = new TemplateProcessor(templateInfo, null);
			else {
				templateProcessor = new TemplateProcessor(templateInfo, options.eraproConnection.get());
				templateInfo.setAnalysisId(options.analysisId.get());
			}
			FileInputStream submittedDataFis = new FileInputStream(submittedDataFile);
			BufferedInputStream bufferedInputStremSubmittedData = new BufferedInputStream(new GZIPInputStream(submittedDataFis));
			CSVReader csvReader = new CSVReader(bufferedInputStremSubmittedData, templateInfo.getTokens(), 0);
			CSVLine csvLine;
			Entry entry;
			TemplateProcessorResultSet templateProcessorResultSet;
			while ((csvLine = csvReader.readTemplateSpreadsheetLine()) != null) {
				templateProcessorResultSet = templateProcessor.process(csvLine.getEntryTokenMap());
				entry = templateProcessorResultSet.getEntry();
				if(entry!=null)
				{
					entry.addProjectAccession(new Text(options.getProjectId()));
					appendHeader(entry);
				}
				if (sequenceCount == MAX_SEQUENCE_COUNT) {
					ValidationResult validationResult = new ValidationResult();
					ValidationMessage<Origin> validationMessage = new ValidationMessage<>(Severity.ERROR, "Data file has exceeded the maximum permitted number of sequencies (" + MAX_SEQUENCE_COUNT + ")" + " that are allowed in one data file.");
					validationResult.append(validationMessage);
					if(getOptions().reportDir.isPresent())
						getReporter().writeToFile(getReportFile(submissionFile), validationResult, "Sequence: " + csvLine.getLineNumber().toString() + " ");
					valid = false;
					break;
				}
				ValidationPlanResult validationPlanResult = templateProcessorResultSet.getValidationPlanResult();
				if (!validationPlanResult.isValid()) {
					if (getOptions().reportDir.isPresent())
						getReporter().writeToFile(getReportFile(submissionFile), validationPlanResult, "Sequence: " + csvLine.getLineNumber().toString() + " ");
					valid = false;
				}
				if(fixedFileWriter!=null)
				new EmblEntryWriter(entry).write(fixedFileWriter);
				sequenceCount++;
			}
			return valid;
		} catch (TemplateUserError e) {
			ValidationResult validationResult = new ValidationResult();
			ValidationMessage<Origin> validationMessage = new ValidationMessage<>(Severity.ERROR, e.getMessage());
			validationResult.append(validationMessage);
			try
			{
			if (getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), validationResult);
			
			}catch(Exception ex)
			{
				throw new ValidationEngineException(ex.getMessage());
			}
			return false;
		} catch (Exception e) {
			throw new ValidationEngineException(e.toString());
		}
	}

	@Override
	public boolean check() throws ValidationEngineException {
		return false;
	}

	private File getTemplateFromResourceAndWriteToProcessDir(String templateId, String templateDir) throws ValidationEngineException {
		try {
			String template = new TemplateProcessor().getTemplate(templateId);
			if (template == null || template.isEmpty())
				throw  new ValidationEngineException("- Method getTemplateFromResourceAndWriteToProcessDir(): ");
			if (template.contains("encoding=\"\""))
				template = template.replace("encoding=\"\"", "encoding=\"UTF-8\"");
			PrintWriter out = null;
			Path path = Paths.get(templateDir + File.separator + TEMPLATE_FILE_NAME + templateId);
			Files.deleteIfExists(path);
			Files.createFile(path);
			Files.write(path, template.getBytes());
			return path.toFile();
		} catch (Exception e) {
			throw new ValidationEngineException("Method getTemplateFromResourceAndWriteToProcessDir: " + e.toString());
		}
	}

	private String getTemplateIdFromTsvFile( File submittedFile ) throws ValidationEngineException {
		String templateId = "";
		try( BufferedReader reader = new BufferedReader( new InputStreamReader(new GZIPInputStream(new FileInputStream( submittedFile)), StandardCharsets.UTF_8)) ){
			Optional<String> optional =  reader.lines()
					.filter(line -> line.startsWith( TEMPLATE_ACCESSION_LINE))
					.findFirst();
			if (optional.isPresent()) {
				templateId = optional.get().replace(TEMPLATE_ACCESSION_LINE, "").trim();
				if (templateId.isEmpty() || !templateId.matches(TEMPLATE_ID_PATTERN))
					throw new ValidationEngineException(TEMPLATE_ACCESSION_LINE + " template id '" + templateId + " is missing or not in the correct format. Example id is ERT000003");
			} else
				throw new ValidationEngineException("File " + submittedFile + " is missing the '" +  TEMPLATE_ACCESSION_LINE + "' line, please add it followed by the template id");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return templateId;
	}

}
