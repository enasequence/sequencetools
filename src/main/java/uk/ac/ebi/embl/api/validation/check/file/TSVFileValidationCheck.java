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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.fixer.entry.EntryNameFix;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;
import uk.ac.ebi.embl.template.*;

@Description("")
public class TSVFileValidationCheck extends FileValidationCheck {
	public final static String TEMPLATE_FILE_NAME = "TEMPLATE_";
	public final static String TEMPLATE_ID_PATTERN = "(ERT[0-9]+)";
	private final static String TEMPLATE_ACCESSION_LINE = "#template_accession";
	public final static String CHECKLIST_TEMPLATE_LINE_PREFIX = "Checklist";
	public static final String SPACE_TOKEN = "\\s+";

	public TSVFileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
		super(options, sharedInfo);
	}

	@Override
	public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException {
		ValidationResult validationResult = new ValidationResult();
		try (PrintWriter fixedFileWriter=getFixedFileWriter(submissionFile)) {
             clearReportFile(getReportFile(submissionFile));
			String templateId = getTemplateIdFromTsvFile(submissionFile.getFile());
			if(!options.isWebinCLI && StringUtils.isBlank(templateId ) ) {
				 templateId = new EraproDAOUtilsImpl(options.eraproConnection.get()).getTemplateId(options.analysisId.get());
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
				templateProcessorResultSet = templateProcessor.process(csvLine.getEntryTokenMap(), options);
				entry = templateProcessorResultSet.getEntry();
				if(entry != null)
				{
					entry.setSubmitterAccession(EntryNameFix.getFixedEntryName(entry.getSubmitterAccession()));
					appendHeader(entry);
				}
				if( options.context.get() == Context.sequence && !validateSequenceCountForTemplate(validationResult, submissionFile)) {
					return validationResult;
				}
				ValidationResult planResult = templateProcessorResultSet.getValidationResult();
				validationResult.append(planResult);
				if (!planResult.isValid()) {
					if (getOptions().reportDir.isPresent())
						getReporter().writeToFile(getReportFile(submissionFile), planResult, "Sequence: " + csvLine.getLineNumber().toString() + " ");
				}
				if(fixedFileWriter!=null)
				new EmblEntryWriter(entry).write(fixedFileWriter);
				sharedInfo.sequenceCount++;
			}

		} catch (TemplateUserError e) {
			ValidationMessage<Origin> validationMessage = new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
			validationMessage.setMessage( e.getMessage() );
			validationResult.append(validationMessage);
			try
			{
			if (getOptions().reportDir.isPresent())
				getReporter().writeToFile(getReportFile(submissionFile), validationResult);
			
			}catch(Exception ex)
			{
				throw new ValidationEngineException(ex.getMessage(), ex);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ValidationEngineException(e.toString(), e);
		}
		return validationResult;
	}

	@Override
	public ValidationResult check() throws ValidationEngineException {
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
					.filter(line -> line.startsWith( TEMPLATE_ACCESSION_LINE) || CSVReader.isValidChecklistIdLine(line))
					.findFirst();
			if (optional.isPresent()) {
				String templateIdLine = optional.get().replace(TEMPLATE_ACCESSION_LINE, "").replace(CHECKLIST_TEMPLATE_LINE_PREFIX,"").trim();
				templateId = getTemplateId(templateIdLine);
				if (templateId.isEmpty() || !templateId.matches(TEMPLATE_ID_PATTERN))
					throw new ValidationEngineException(TEMPLATE_ACCESSION_LINE + " template id '" + templateId + " is missing or not in the correct format. Example id is ERT000003",
							ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return templateId;
	}

	private String getTemplateId(String line) {
		Pattern pattern = Pattern.compile(TEMPLATE_ID_PATTERN);
		Matcher matcher = pattern.matcher(line);
		String templateId = matcher.find() ? matcher.group(1) : "";
		return templateId;
	}
}
