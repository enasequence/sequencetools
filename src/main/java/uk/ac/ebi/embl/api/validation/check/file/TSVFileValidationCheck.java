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

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.fixer.entry.EntryNameFix;
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
	public ValidationPlanResult check(SubmissionFile submissionFile) throws ValidationEngineException {
		ValidationPlanResult validationPlanResult = new ValidationPlanResult();
		try (PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile)) {
             clearReportFile(getReportFile(submissionFile));
			String templateId = getTemplateIdFromTsvFile(submissionFile.getFile());
			if(StringUtils.isBlank(templateId ) && !options.isWebinCLI) {
				EraproDAOUtils eraDaoUtils = new EraproDAOUtilsImpl(options.eraproConnection.get());
				templateId = eraDaoUtils.getTemplateId(options.analysisId.get());
			}
			if(templateId == null)
				throw new ValidationEngineException("Missing template id", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
			File submittedDataFile =  submissionFile.getFile();
			String templateDir = submittedDataFile.getParent();
			File templateFile = getTemplateFromResourceAndWriteToProcessDir(templateId, templateDir);
			TemplateLoader templateLoader = new TemplateLoader();
			if (!submittedDataFile.exists())
				throw new ValidationEngineException(submittedDataFile.getAbsolutePath() +  " file does not exist", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
			TemplateInfo templateInfo = templateLoader.loadTemplateFromFile(templateFile);
			TemplateProcessor templateProcessor;
			if (options.isWebinCLI)
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
				templateProcessorResultSet = templateProcessor.process(csvLine.getEntryTokenMap(), options.getProjectId());
				entry = templateProcessorResultSet.getEntry();
				if(entry != null)
				{
					entry.setSubmitterAccession(EntryNameFix.getFixedEntryName(entry.getSubmitterAccession()));
					appendHeader(entry);
				}
				if (sequenceCount == MAX_SEQUENCE_COUNT) {
					ValidationResult validationResult = new ValidationResult();
					ValidationMessage<Origin> validationMessage = new ValidationMessage<>(Severity.ERROR, "Data file has exceeded the maximum permitted number of sequencies (" + MAX_SEQUENCE_COUNT + ")" + " that are allowed in one data file.");
					validationResult.append(validationMessage);
					if(getOptions().reportDir.isPresent())
						getReporter().writeToFile(getReportFile(submissionFile), validationResult, "Sequence: " + csvLine.getLineNumber().toString() + " ");
					validationPlanResult.append(validationResult);
					validationPlanResult.setHasError(true);
					break;
				}
				ValidationPlanResult planResult = templateProcessorResultSet.getValidationPlanResult();
				validationPlanResult.append(planResult);
				if (!planResult.isValid()) {
					validationPlanResult.setHasError(true);
					if (getOptions().reportDir.isPresent())
						getReporter().writeToFile(getReportFile(submissionFile), planResult, "Sequence: " + csvLine.getLineNumber().toString() + " ");
				}
				if(fixedFileWriter!=null)
					new EmblEntryWriter(entry).write(fixedFileWriter);
				sequenceCount++;
			}

		} catch (TemplateUserError e) {
			ValidationResult validationResult = new ValidationResult();
			ValidationMessage<Origin> validationMessage = new ValidationMessage<>(Severity.ERROR, e.getMessage());
			validationResult.append(validationMessage);
			validationPlanResult.append(validationResult);
			validationPlanResult.setHasError(true);
			try
			{
			if (getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), validationResult);
			
			}catch(Exception ex)
			{
				throw new ValidationEngineException(ex);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ValidationEngineException( e);
		}
		return validationPlanResult;
	}

	@Override
	public ValidationPlanResult check() throws ValidationEngineException {
		throw new UnsupportedOperationException();
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
			throw new ValidationEngineException("Method getTemplateFromResourceAndWriteToProcessDir: " + e.toString(), e);
		}
	}

	private String getTemplateIdFromTsvFile( File submittedFile ) throws ValidationEngineException {
		String templateId = null;
		try( BufferedReader reader = new BufferedReader( new InputStreamReader(new GZIPInputStream(new FileInputStream( submittedFile)), StandardCharsets.UTF_8)) ){
			Optional<String> optional =  reader.lines()
					.filter(line -> line.startsWith( TEMPLATE_ACCESSION_LINE))
					.findFirst();
			if (optional.isPresent()) {
				templateId = optional.get().replace(TEMPLATE_ACCESSION_LINE, "").trim();
				if (templateId.isEmpty() || !templateId.matches(TEMPLATE_ID_PATTERN))
					throw new ValidationEngineException(TEMPLATE_ACCESSION_LINE + " template id '" + templateId + " is missing or not in the correct format. Example id is ERT000003",
							ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return templateId;
	}

}
