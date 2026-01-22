/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.check.file;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.storage.tsv.TSVReader;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;
import uk.ac.ebi.embl.template.*;

@Description("")
public class TSVFileValidationCheck extends FileValidationCheck {
  public static final String TEMPLATE_FILE_NAME = "TEMPLATE_";

  public TSVFileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
    super(options, sharedInfo);
  }

  @Override
  public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException {

    if (isPolySampleSubmission(submissionFile)) {
      return validatePolySampleTSV(submissionFile);
    } else if (isSequenceTaxSubmission(submissionFile)) {
      return validateSequenceTaxTSV(submissionFile);
    } else {
      return validateTemplateSubmission(submissionFile);
    }
  }

  public ValidationResult validateTemplateSubmission(SubmissionFile submissionFile)
      throws ValidationEngineException {
    ValidationResult validationResult = new ValidationResult();
    try (PrintWriter fixedFileWriter = getFixedFileWriter(submissionFile)) {
      clearReportFile(getReportFile(submissionFile));

      String templateId = getTemplateIdFromTsvFile(submissionFile.getFile());
      if (StringUtils.isBlank(templateId)) {
        throw new ValidationEngineException(
            "Missing template id", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
      }

      File submittedDataFile = submissionFile.getFile();
      String templateDir = submittedDataFile.getParent();
      File templateFile = getTemplateFromResourceAndWriteToProcessDir(templateId, templateDir);
      TemplateLoader templateLoader = new TemplateLoader();
      if (!submittedDataFile.exists())
        throw new ValidationEngineException(
            submittedDataFile.getAbsolutePath() + " file does not exist",
            ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
      TemplateInfo templateInfo = templateLoader.loadTemplateFromFile(templateFile);
      options.setTemplateId(templateId);
      TemplateProcessor templateProcessor;
      if (options.isWebinCLI) templateProcessor = new TemplateProcessor(templateInfo, options);
      else {
        templateProcessor = new TemplateProcessor(templateInfo, options);
        templateInfo.setAnalysisId(options.analysisId.get());
      }
      FileInputStream submittedDataFis = new FileInputStream(submittedDataFile);
      BufferedInputStream bufferedInputStremSubmittedData =
          new BufferedInputStream(new GZIPInputStream(submittedDataFis));
      CSVReader csvReader =
          new CSVReader(bufferedInputStremSubmittedData, templateInfo.getTokens(), 0);
      CSVLine csvLine;
      Entry entry;
      TemplateProcessorResultSet templateProcessorResultSet;
      while ((csvLine = csvReader.readTemplateSpreadsheetLine()) != null) {
        templateProcessorResultSet = templateProcessor.process(csvLine.getEntryTokenMap(), options);
        entry = templateProcessorResultSet.getEntry();
        if (entry != null) {
          appendHeader(entry);
        }
        if (options.context.get() == Context.sequence
            && !validateSequenceCountForTemplate(validationResult, submissionFile)) {
          return validationResult;
        }
        ValidationResult planResult = templateProcessorResultSet.getValidationResult();
        validationResult.append(planResult);
        if (!planResult.isValid()) {
          if (getOptions().reportDir.isPresent())
            getReporter()
                .writeToFile(
                    getReportFile(submissionFile),
                    planResult,
                    "Sequence: " + csvLine.getLineNumber().toString() + " ");
        }
        if (fixedFileWriter != null) new EmblEntryWriter(entry).write(fixedFileWriter);
        sharedInfo.sequenceCount++;
      }

    } catch (TemplateUserError e) {
      ValidationMessage<Origin> validationMessage =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      validationMessage.setMessage(e.getMessage());
      validationResult.append(validationMessage);
      try {
        if (getOptions().reportDir.isPresent())
          getReporter().writeToFile(getReportFile(submissionFile), validationResult);

      } catch (Exception ex) {
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

  private File getTemplateFromResourceAndWriteToProcessDir(String templateId, String templateDir)
      throws ValidationEngineException {
    try {
      String template = new TemplateProcessor().getTemplate(templateId);
      if (template == null || template.isEmpty())
        throw new ValidationEngineException(
            "- Method getTemplateFromResourceAndWriteToProcessDir(): ");
      if (template.contains("encoding=\"\""))
        template = template.replace("encoding=\"\"", "encoding=\"UTF-8\"");
      PrintWriter out = null;
      Path path = Paths.get(templateDir + File.separator + TEMPLATE_FILE_NAME + templateId);
      Files.deleteIfExists(path);
      Files.createFile(path);
      Files.write(path, template.getBytes());
      return path.toFile();
    } catch (Exception e) {
      throw new ValidationEngineException(
          "Method getTemplateFromResourceAndWriteToProcessDir: " + e, e);
    }
  }

  private String getTemplateIdFromTsvFile(File submittedFile) throws ValidationEngineException {
    String templateId = null;
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                new GZIPInputStream(new FileInputStream(submittedFile)), StandardCharsets.UTF_8))) {
      Optional<String> templateIdOpt =
          reader
              .lines()
              .limit(10)
              .map(line -> CSVReader.getChecklistIdFromIdLine(line))
              .filter(id -> id != null)
              .findFirst();

      if (templateIdOpt.isPresent()) {
        templateId = templateIdOpt.orElse(null);
        if (StringUtils.isEmpty(templateId))
          throw new ValidationEngineException(
              "Template id: '" + templateId + " is not valid. Example id is ERT000003",
              ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return templateId;
  }

  // Polysample TSV is validated in this method.
  private ValidationResult validatePolySampleTSV(SubmissionFile submissionFile)
      throws ValidationEngineException {

    ValidationResult validationResult = new ValidationResult();
    DataSet polysampleDataSet = new TSVReader().getPolySampleDataSet(submissionFile.getFile());

    // If TSV file is not valid
    if (polysampleDataSet == null || polysampleDataSet.getRows().size() <= 1) {
      throw new ValidationEngineException(
          "Submitted file is not a valid TSV file: " + submissionFile.getFile());
    }

    DataRow headerRow = polysampleDataSet.getRows().get(0);
    List<PolySample> polySampleList = new ArrayList<>();
    if (isValidPolySampleHeader(headerRow)) {
      try {
        polySampleList.addAll(
            polysampleDataSet.getRows().stream()
                .skip(1)
                .map(
                    dataRow ->
                        new PolySample(
                            dataRow.getString(0),
                            dataRow.getString(1),
                            Long.parseLong(dataRow.getString(2))))
                .collect(Collectors.toList()));
      } catch (NumberFormatException e) {
        validationResult.append(
            new ValidationMessage(
                Severity.ERROR, "Invalid frequency value in polysample submission: "));
        throw new ValidationEngineException(
            "Invalid frequency value in polysample submission: ", e);
      }
    }
    if (polySampleList.isEmpty()) {
      validationResult.append(
          new ValidationMessage(Severity.ERROR, "Empty polysample submission "));
    }
    return validationResult;
  }

  // Sequence tax TSV is validated in this method.
  private ValidationResult validateSequenceTaxTSV(SubmissionFile submissionFile)
      throws ValidationEngineException {

    ValidationResult validationResult = new ValidationResult();
    DataSet dataSet = new TSVReader().getPolySampleDataSet(submissionFile.getFile());

    if (dataSet == null || dataSet.getRows() == null || dataSet.getRows().size() <= 1) {
      validationResult.append(
          new ValidationMessage(
              Severity.ERROR,
              "Submitted file is not a valid TSV file: " + submissionFile.getFile()));
      return validationResult;
    }

    List<DataRow> rows = dataSet.getRows();
    DataRow header = rows.get(0);

    int headerLength = header.getLength();
    boolean hasScientificName = headerLength == 3;

    if (!isValidSequenceTaxHeader(header)) {
      validationResult.append(
          new ValidationMessage(Severity.ERROR, "Invalid header for sequence tax submission"));
      return validationResult;
    }

    Pattern sequenceIdPattern = Pattern.compile("^[A-Za-z0-9]+$");
    Pattern taxIdPattern = Pattern.compile("^\\d+$");

    boolean hasAnyDataRow = false;

    for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
      hasAnyDataRow = true;

      DataRow row = rows.get(rowIndex);
      int lineNumber = rowIndex + 1;

      if (row.getLength() < 2) {
        validationResult.append(
            new ValidationMessage(
                Severity.ERROR, "Invalid row " + lineNumber + ": expected at least 2 columns"));
        continue;
      }

      String sequenceId = row.getString(0);
      String taxId = row.getString(1);

      if (sequenceId == null || !sequenceIdPattern.matcher(sequenceId).matches()) {
        validationResult.append(
            new ValidationMessage(
                Severity.ERROR, "Invalid Sequence_id at row " + lineNumber + ": " + sequenceId));
      }

      if (taxId == null || !taxIdPattern.matcher(taxId).matches()) {
        validationResult.append(
            new ValidationMessage(
                Severity.ERROR, "Invalid Tax_id at row " + lineNumber + ": " + taxId));
      }

      if (hasScientificName) {
        if (row.getLength() < 3) {
          validationResult.append(
              new ValidationMessage(
                  Severity.ERROR, "Invalid row " + lineNumber + ": expected 3 columns"));
          continue;
        }
        String scientificName = row.getString(2);
        if (scientificName == null || scientificName.trim().isEmpty()) {
          validationResult.append(
              new ValidationMessage(
                  Severity.ERROR, "Invalid Scientific_name at row " + lineNumber));
        }
      }
    }

    if (!hasAnyDataRow) {
      validationResult.append(
          new ValidationMessage(Severity.ERROR, "Empty sequence tax submission"));
    }

    return validationResult;
  }
}
