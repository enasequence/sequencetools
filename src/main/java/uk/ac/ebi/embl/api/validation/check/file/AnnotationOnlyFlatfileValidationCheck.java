/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.check.file;

import java.io.BufferedReader;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.common.CommonUtil;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader.Format;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankEntryReader;

public class AnnotationOnlyFlatfileValidationCheck extends FileValidationCheck {
  public AnnotationOnlyFlatfileValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
    super(options, sharedInfo);
  }

  @Override
  public ValidationResult check(SubmissionFile submissionFile) throws ValidationEngineException {
    ValidationResult validationResult = new ValidationResult();

    try (BufferedReader fileReader = CommonUtil.bufferedReaderFromFile(submissionFile.getFile())) {
      clearReportFile(getReportFile(submissionFile));
      boolean isGenbankFile = isGenbank(submissionFile.getFile());

      if (!isGenbankFile
          && !validateFileFormat(
              submissionFile.getFile(),
              uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType.FLATFILE)) {
        addErrorAndReport(validationResult, submissionFile, "InvalidFileFormat", "flatfile");
        return validationResult;
      }
      Format format =
          options.context.get() == Context.genome
              ? Format.ASSEMBLY_FILE_FORMAT
              : Format.EMBL_FORMAT;
      EntryReader entryReader =
          isGenbankFile
              ? new GenbankEntryReader(fileReader)
              : new EmblEntryReader(fileReader, format, submissionFile.getFile().getName());
      ValidationResult parseResult = entryReader.read();
      validationResult.append(parseResult);
      ConcurrentMap<String, Entry> annotationMap =
          (ConcurrentMap<String, Entry>) sharedInfo.annotationDB.hashMap("map").createOrOpen();
      while (entryReader.isEntry()) {
        if (!parseResult.isValid()) {
          getReporter().writeToFile(getReportFile(submissionFile), parseResult);
          addMessageStats(parseResult.getMessages());
        }

        Entry entry = entryReader.getEntry();
        if (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null) {
          if (StringUtils.isBlank(entry.getSubmitterAccession())) {
            throw new ValidationEngineException(
                "Missing submitter sequence name for annotation only entry.");
          }
          entry.setDataClass(getDataclass(entry.getSubmitterAccession()));
          getOptions()
              .getEntryValidationPlanProperty()
              .validationScope
              .set(getValidationScope(entry.getSubmitterAccession()));
          getOptions()
              .getEntryValidationPlanProperty()
              .fileType
              .set(uk.ac.ebi.embl.api.validation.FileType.EMBL);
          appendHeader(entry);
          addSubmitterSeqIdQual(entry);
          if (entry.getSubmitterAccession() != null) {
            annotationMap.put(entry.getSubmitterAccession().toUpperCase(), entry);
          }
          parseResult = entryReader.read();
          validationResult.append(parseResult);
        } else {
          throw new ValidationEngineException(
              "File has some entries with only annotations and some entries with sequences, If you intend to provide annotations"
                  + " separately for some sequences, please submit annotations and sequences in different files",
              ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
        }
      }

    } catch (ValidationEngineException vee) {
      throw vee;
    } catch (Exception e) {
      throw new ValidationEngineException(e.getMessage(), e);
    } finally {
      if (sharedInfo.annotationDB != null) {
        sharedInfo.annotationDB.commit();
      }
    }
    return validationResult;
  }

  @Override
  public ValidationResult check() throws ValidationEngineException {
    throw new UnsupportedOperationException();
  }
}
