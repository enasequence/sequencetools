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
package uk.ac.ebi.embl.api.validation.submission;

import static uk.ac.ebi.embl.api.validation.check.file.FileValidationCheck.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.mapdb.DBMaker;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationEngineException.ReportErrorType;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.*;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;

public class SubmissionValidationPlan {
  private final SubmissionOptions options;

  private final FileValidationCheck.SharedInfo sharedInfo = new FileValidationCheck.SharedInfo();

  FileValidationCheck check = null;
  AGPFileValidationCheck agpCheck = null;
  MasterEntryValidationCheck masterCheck = null;

  public SubmissionValidationPlan(SubmissionOptions options) {
    this.options = options;
  }

  public ValidationResult execute() throws ValidationEngineException {
    ValidationResult validationResult = new ValidationResult();
    try {
      // TODO: check for a way to log INFO messages
      options.init();
      sharedInfo.hasAgp = options.submissionFiles.get().getFiles(FileType.AGP).size() > 0;
      sharedInfo.assemblyType =
          options.assemblyInfoEntry.map(AssemblyInfoEntry::getAssemblyType).orElse(null);
      // Validation Order shouldn't be changed
      if (options.context.get().getFileTypes().contains(FileType.MASTER)) createMaster();
      if (options.context.get().getFileTypes().contains(FileType.CHROMOSOME_LIST))
        validateChromosomeList();
      if (options.context.get().getFileTypes().contains(FileType.UNLOCALISED_LIST))
        validateUnlocalisedList();
      if (options.context.get().getFileTypes().contains(FileType.AGP)) {
        agpCheck = new AGPFileValidationCheck(options, sharedInfo);
        if (sharedInfo.hasAgp) {
          sharedInfo.contigDB =
              DBMaker.fileDB(options.reportDir.get() + File.separator + getcontigDbname())
                  .fileDeleteAfterClose()
                  .closeOnJvmShutdown()
                  .make();
          agpCheck.createContigDB();
        }
      }
      if (options.context.get().getFileTypes().contains(FileType.ANNOTATION_ONLY_FLATFILE)) {
        validationResult = validateAnnotationOnlyFlatfile();
        if (!validationResult.isValid()) {
          return validationResult;
        }
      }
      if (options.context.get().getFileTypes().contains(FileType.FASTA)) {
        validationResult = validateFasta();
        if (!validationResult.isValid()) return validationResult;
      }

      if (options.context.get().getFileTypes().contains(FileType.FLATFILE)) {
        validationResult = validateFlatfile();
        if (!validationResult.isValid()) return validationResult;
      }

      if (options.context.get().getFileTypes().contains(FileType.AGP)) {
        validationResult = validateAGP();
        if (!validationResult.isValid()) return validationResult;
      }
      if (options.context.get().getFileTypes().contains(FileType.TSV)
          || options.context.get().getFileTypes().contains(FileType.SAMPLE_TSV)
          || options.context.get().getFileTypes().contains(FileType.TAX_TSV)) {
        List<SubmissionFile> tsvFiles = new ArrayList<>();
        tsvFiles.addAll(options.submissionFiles.get().getFiles(FileType.TSV));
        tsvFiles.addAll(options.submissionFiles.get().getFiles(FileType.SAMPLE_TSV));
        tsvFiles.addAll(options.submissionFiles.get().getFiles(FileType.TAX_TSV));

        validationResult = validateTsvfile(tsvFiles);
        if (!validationResult.isValid()) {
          return validationResult;
        }
      }

      validateDuplicateEntryNames(sharedInfo);
      validateUnlocalisedEntryNames(sharedInfo);
      if (Context.genome == options.context.get()) {
        registerSequences();
        validateCovid19GenomeSize(sharedInfo);
        validateSequencelessChromosomes(sharedInfo);
        verifyUnlocalisedObjectNames(sharedInfo);

        String assemblyType =
            options.assemblyInfoEntry.map(AssemblyInfoEntry::getAssemblyType).orElse(null);
        throwValidationResult(
            uk.ac.ebi.embl.api.validation.helper.Utils.validateAssemblySequenceCount(
                options.ignoreErrors,
                getSequencecount(0),
                getSequencecount(1),
                getSequencecount(2),
                assemblyType));

        if (!options.isWebinCLI && !EntryUtils.excludeDistribution(sharedInfo.assemblyType)) {
          writeUnplacedList();
        }
      } else {
        writeSequenceInfo();
      }

    } catch (ValidationEngineException e) {
      try {
        if (options.reportFile.isPresent()) {
          new DefaultSubmissionReporter(
                  new HashSet<>(
                      Arrays.asList(Severity.ERROR, Severity.WARNING, Severity.FIX, Severity.INFO)))
              .writeToFile(
                  options.reportFile.get(),
                  Severity.ERROR,
                  e.getMessage() + (e.getCause() == null ? "" : " Caused by:" + e.getCause()));
        }
        if (!options.isWebinCLI
            && options.context.isPresent()
            && options.context.get() == Context.genome
            && check != null
            && check.getMessageStats() != null)
          check
              .getReporter()
              .writeToFile(Paths.get(options.reportDir.get()), check.getMessageStats());
      } catch (Exception ex) {
        e =
            new ValidationEngineException(
                e.getMessage() + "\n Failed to write error message stats: " + ex.getMessage(), e);
        e.setErrorType(e.getErrorType());
      }
      throw e;
    } finally {
      if (sharedInfo.contigDB != null) {
        sharedInfo.contigDB.close();
      }
      if (sharedInfo.annotationDB != null) {
        sharedInfo.annotationDB.close();
      }
      if (check != null) {
        check.flushAndCloseFileWriters();
      }
    }
    return validationResult;
  }

  public Set<String> getUnplacedEntryNames() {
    return sharedInfo.unplacedEntryNames;
  }

  private ValidationResult createMaster() throws ValidationEngineException {
    ValidationResult result = new ValidationResult();
    try {
      masterCheck = new MasterEntryValidationCheck(options, sharedInfo);
      result = masterCheck.check();
      if (!result.isValid()) {
        if (options.isWebinCLI)
          throw new ValidationEngineException(
              "Master entry validation failed", ReportErrorType.VALIDATION_ERROR);
        return result;
      }
    } catch (Exception e) {
      throwValidationEngineException(FileType.MASTER.name(), e, "master.dat");
    }

    return result;
  }

  private ValidationResult validateChromosomeList() throws ValidationEngineException {
    ValidationResult result = new ValidationResult();
    String fileName = null;
    try {
      check = new ChromosomeListFileValidationCheck(options, sharedInfo);
      for (SubmissionFile chromosomeListFile :
          options.submissionFiles.get().getFiles(FileType.CHROMOSOME_LIST)) {
        fileName = chromosomeListFile.getFile().getName();
        result = check.check(chromosomeListFile);
        if (!result.isValid()) {
          if (options.isWebinCLI)
            throwValidationCheckException(FileType.CHROMOSOME_LIST, chromosomeListFile);
          return result;
        }
      }
    } catch (Exception e) {
      throwValidationEngineException(FileType.CHROMOSOME_LIST.name(), e, fileName);
    }
    return result;
  }

  private ValidationResult validateFasta() throws ValidationEngineException {
    ValidationResult result = new ValidationResult();
    check = new FastaFileValidationCheck(options, sharedInfo);
    String fileName = null;
    try {
      List<SubmissionFile> submissionFiles = options.submissionFiles.get().getFiles(FileType.FASTA);
      if (!submissionFiles.isEmpty()) {
        for (SubmissionFile fastaFile : submissionFiles) {
          fileName = fastaFile.getFile().getName();
          result = check.check(fastaFile);
          if (!result.isValid()) {
            if (options.isWebinCLI) throwValidationCheckException(FileType.FASTA, fastaFile);
            return result;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throwValidationEngineException(FileType.FASTA.name(), e, fileName);
    }
    return result;
  }

  private ValidationResult validateFlatfile() throws ValidationEngineException {
    ValidationResult result = new ValidationResult();
    check = new FlatfileFileValidationCheck(options, sharedInfo);
    String fileName = null;
    try {
      List<SubmissionFile> submissionFiles =
          options.submissionFiles.get().getFiles(FileType.FLATFILE);
      if (!submissionFiles.isEmpty()) {
        for (SubmissionFile flatfile : submissionFiles) {
          fileName = flatfile.getFile().getName();
          result = check.check(flatfile);
          if (!result.isValid()) {
            if (options.isWebinCLI) throwValidationCheckException(FileType.FLATFILE, flatfile);
            return result;
          }
        }
      }
    } catch (Exception e) {
      throwValidationEngineException(FileType.FLATFILE.name(), e, fileName);
    }
    return result;
  }

  private ValidationResult validateAGP() throws ValidationEngineException {
    ValidationResult result = new ValidationResult();
    String fileName = null;
    try {
      List<SubmissionFile> submissionFiles = options.submissionFiles.get().getFiles(FileType.AGP);
      if (!submissionFiles.isEmpty()) {
        for (SubmissionFile agpFile : submissionFiles) {
          fileName = agpFile.getFile().getName();
          result = agpCheck.check(agpFile);
          if (!result.isValid()) {
            if (options.isWebinCLI) throwValidationCheckException(FileType.AGP, agpFile);
            return result;
          }
        }
      }

    } catch (Exception e) {
      throwValidationEngineException(FileType.AGP.name(), e, fileName);
    }
    return result;
  }

  private ValidationResult validateUnlocalisedList() throws ValidationEngineException {
    ValidationResult result = new ValidationResult();
    String fileName = null;
    try {
      check = new UnlocalisedListFileValidationCheck(options, sharedInfo);
      for (SubmissionFile unlocalisedListFile :
          options.submissionFiles.get().getFiles(FileType.UNLOCALISED_LIST)) {
        fileName = unlocalisedListFile.getFile().getName();
        result = check.check(unlocalisedListFile);
        if (!result.isValid()) {
          if (options.isWebinCLI)
            throwValidationCheckException(FileType.UNLOCALISED_LIST, unlocalisedListFile);
          return result;
        }
      }
    } catch (Exception e) {
      throwValidationEngineException(FileType.UNLOCALISED_LIST.name(), e, fileName);
    }
    return result;
  }

  private void registerSequences() throws ValidationEngineException {
    sharedInfo.sequenceInfo.putAll(
        AssemblySequenceInfo.getMapObject(
            options.processDir.get(), AssemblySequenceInfo.fastafileName));
    sharedInfo.sequenceInfo.putAll(
        AssemblySequenceInfo.getMapObject(
            options.processDir.get(), AssemblySequenceInfo.flatfilefileName));
    sharedInfo.sequenceInfo.putAll(
        AssemblySequenceInfo.getMapObject(
            options.processDir.get(), AssemblySequenceInfo.agpfileName));
    AssemblySequenceInfo.writeMapObject(
        sharedInfo.sequenceInfo, options.processDir.get(), AssemblySequenceInfo.sequencefileName);
  }

  private ValidationResult validateAnnotationOnlyFlatfile() throws ValidationEngineException {
    String fileName = null;
    ValidationResult result = new ValidationResult();
    try {
      check = new AnnotationOnlyFlatfileValidationCheck(options, sharedInfo);
      sharedInfo.hasAnnotationOnlyFlatfile = hasAnnotationOnlyFlatfile(options);
      if (sharedInfo.hasAnnotationOnlyFlatfile) {
        sharedInfo.annotationDB =
            DBMaker.fileDB(options.reportDir.get() + File.separator + getAnnoationDbname())
                .fileDeleteAfterClose()
                .closeOnJvmShutdown()
                .make();
      } else {
        return result;
      }

      for (SubmissionFile annotationOnlyFlatfile :
          options.submissionFiles.get().getFiles(FileType.FLATFILE)) {
        fileName = annotationOnlyFlatfile.getFile().getName();
        result = check.check(annotationOnlyFlatfile);
        if (!result.isValid()) {
          if (options.isWebinCLI)
            throwValidationCheckException(
                FileType.ANNOTATION_ONLY_FLATFILE, annotationOnlyFlatfile);
          return result;
        }
      }
    } catch (Exception e) {
      throwValidationEngineException(
          FileType.FLATFILE.name() + "/" + FileType.ANNOTATION_ONLY_FLATFILE.name(), e, fileName);
    }
    return result;
  }

  private ValidationResult validateTsvfile(List<SubmissionFile> tsvFiles)
      throws ValidationEngineException {
    String fileName = null;
    ValidationResult result = new ValidationResult();
    FileType currentType = FileType.TSV;
    boolean isPolySample = false;
    try {
      check = new TSVFileValidationCheck(options, sharedInfo);
      for (SubmissionFile tsvFile : tsvFiles) {
        fileName = tsvFile.getFile().getName();
        currentType = tsvFile.getFileType();
        result = check.check(tsvFile);
        if (!result.isValid()) {
          if (options.isWebinCLI) throwValidationCheckException(tsvFile.getFileType(), tsvFile);
          return result;
        }
        isPolySample |= check.isPolySampleSubmission(tsvFile);
      }
      if (isPolySample) {
        PolySampleValidationCheck polySampleCheck =
            new PolySampleValidationCheck(options, sharedInfo);
        polySampleCheck.check();
      }
    } catch (Exception e) {
      throwValidationEngineException(currentType.name(), e, fileName);
    }
    return result;
  }

  private String getAnnoationDbname() {
    return ".annotation";
  }

  private String getcontigDbname() {
    return ".contig";
  }

  private void throwValidationCheckException(FileType fileTpe, SubmissionFile submissionFile)
      throws ValidationEngineException {
    Path reportFile =
        fileTpe == FileType.AGP
            ? agpCheck.getReportFile(submissionFile)
            : check.getReportFile(submissionFile);
    throw new ValidationEngineException(
        String.format(
            "%s file validation failed : %s, Please see the error report: %s",
            fileTpe.name().toLowerCase(), submissionFile.getFile().getName(), reportFile.toFile()),
        ReportErrorType.VALIDATION_ERROR);
  }

  private void throwValidationEngineException(String fileTpe, Exception e, String fileName)
      throws ValidationEngineException {
    if (options.isWebinCLI) {
      ValidationEngineException validationEngineException =
          new ValidationEngineException(
              String.format("%s file validation failed for %s", fileTpe.toLowerCase(), fileName),
              e);
      validationEngineException.setErrorType(ReportErrorType.VALIDATION_ERROR);
      throw validationEngineException;
    } else throw new ValidationEngineException(e);
  }

  @SuppressWarnings("deprecation")
  private void throwValidationResult(ValidationResult result) throws ValidationEngineException {
    if (result == null || result.isValid()) return;
    StringBuilder messages = new StringBuilder();
    for (ValidationMessage message : result.getMessages()) {
      messages.append(message.getMessage() + "\n");
    }

    throw new ValidationEngineException(
        StringUtils.chopNewline(messages.toString()), ReportErrorType.VALIDATION_ERROR);
  }

  private long getSequencecount(int assemblyLevel) {
    return sharedInfo.sequenceInfo.values().stream()
        .filter(p -> p.getAssemblyLevel() == assemblyLevel)
        .count();
  }

  private void writeUnplacedList() throws ValidationEngineException {

    try {
      Files.deleteIfExists(Paths.get(options.processDir.get(), "unplaced.txt"));
    } catch (Exception e) {
      throw new ValidationEngineException("Failed to delete unplaced file: " + e.getMessage(), e);
    }
    try (ObjectOutputStream oos =
        new ObjectOutputStream(
            new FileOutputStream(options.processDir.get() + File.separator + "unplaced.txt"))) {
      oos.writeObject(sharedInfo.unplacedEntryNames);

    } catch (Exception e) {
      throw new ValidationEngineException("Failed to write unplaced file: " + e.getMessage(), e);
    }
  }

  private void writeSequenceInfo() throws ValidationEngineException {
    if (options.processDir.isPresent()
        && Files.exists(
            Paths.get(
                String.format(
                    "%s%s%s",
                    options.processDir.get(),
                    File.separator,
                    AssemblySequenceInfo.sequencefileName)))) return;

    AssemblySequenceInfo.writeObject(
        sharedInfo.sequenceCount, options.processDir.get(), AssemblySequenceInfo.sequencefileName);
  }
}
