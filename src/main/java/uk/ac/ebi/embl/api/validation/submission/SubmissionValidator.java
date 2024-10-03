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

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;
import uk.ac.ebi.embl.api.service.WebinSampleRetrievalService;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoNameCheck;
import uk.ac.ebi.embl.api.validation.helper.SourceFeatureUtils;
import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.flatfile.reader.FeatureReader;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.webin.cli.validator.api.ValidationResponse;
import uk.ac.ebi.ena.webin.cli.validator.api.Validator;
import uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.SequenceManifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.TranscriptomeManifest;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;

public class SubmissionValidator implements Validator<Manifest, ValidationResponse> {

  private SubmissionOptions options;
  private static final int ERROR_MAX_LENGTH = 2000;
  private static final Integer COVID_19_OUTBREAK_TAX_ID = 2697049;

  public SubmissionValidator() {}

  public SubmissionValidator(SubmissionOptions options) {
    this.options = options;
  }

  public void validate() throws ValidationEngineException {

    if (options.context.get() == Context.sequence) {
      // Initialise SampleRetrievalService.
      SequenceToolsServices.init(
          new WebinSampleRetrievalService(
              options.webinRestUri.get(),
              options.biosamplesUri.get(),
              options.webinAuthToken.get(),
              options.biosamplesWebinAuthToken.get()));
    }

    ValidationResult validationResult = new SubmissionValidationPlan(options).execute();
    if (!options.isWebinCLI && !validationResult.isValid()) {
      StringBuilder sb = new StringBuilder();
      for (ValidationMessage<Origin> error : validationResult.getMessages(Severity.ERROR)) {
        if ((sb.length() + error.getMessage().length()) > ERROR_MAX_LENGTH) break;
        sb.append(error.getMessage());
        sb.append("\n");
        if(!error.getCuratorMessage().isEmpty()) {
          sb.append("Curator message: ");
          sb.append(error.getCuratorMessage());
        }
      }
      throw new ValidationEngineException(
          StringUtils.chomp(sb.toString()),
          ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
    }
  }

  /**
   * Manifest to SubmissionOptions mapping.This is only for webin-cli.
   *
   * @param manifest
   * @return ValidationResponse
   */
  @Override
  public ValidationResponse validate(Manifest manifest) {
    ValidationResponse response =
        new ValidationResponse(ValidationResponse.status.VALIDATION_SUCCESS);
    try {
      options = mapManifestToSubmissionOptions(manifest);
      FeatureReader.isWebinCli = true;
      validate();
    } catch (ValidationEngineException vee) {
      if (vee.getErrorType() == ValidationEngineException.ReportErrorType.VALIDATION_ERROR) {
        response.setStatus(ValidationResponse.status.VALIDATION_ERROR);
      } else {
        new DefaultSubmissionReporter(new HashSet<>(Collections.singletonList(Severity.ERROR)))
            .writeToFile(manifest.getReportFile(), Severity.ERROR, vee.getMessage());
        throw new RuntimeException(vee);
      }
    }
    return response;
  }

  SubmissionOptions mapManifestToSubmissionOptions(Manifest manifest)
      throws ValidationEngineException {
    if (manifest == null)
      throw new ValidationEngineException(
          "Manifest can not be null.", ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
    if (manifest.getReportFile() == null) {
      throw new ValidationEngineException(
          "Report file is missing.", ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
    }
    DefaultSubmissionReporter reporter =
        new DefaultSubmissionReporter(new HashSet<>(Collections.singletonList(Severity.ERROR)));
    if (manifest.getProcessDir() == null) {
      reporter.writeToFile(
          manifest.getReportFile(), Severity.ERROR, "Process directory is missing.");
      throw new ValidationEngineException(
          "Process directory is missing.", ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
    }
    SubmissionOptions options = new SubmissionOptions();
    // Set all common options
    AssemblyInfoEntry assemblyInfo = new AssemblyInfoEntry();

    assemblyInfo.setName(manifest.getName());
    assemblyInfo.setAuthors(manifest.getAuthors());
    assemblyInfo.setAddress(manifest.getAddress());

    if (manifest.getStudy() != null) {
      assemblyInfo.setStudyId(manifest.getStudy().getBioProjectId());
      if (manifest.getStudy().getLocusTags() != null) {
        options.locusTagPrefixes = Optional.of(manifest.getStudy().getLocusTags());
      }
    }
    if (manifest.getSample() != null) {
      assemblyInfo.setBiosampleId(manifest.getSample().getBioSampleId());
      assemblyInfo.setOrganism(manifest.getSample().getOrganism());

      SourceFeature sourceFeature =
          new SourceFeatureUtils()
              .constructSourceFeature(manifest.getSample(), new TaxonomyClient());
      sourceFeature.addQualifier(
          Qualifier.DB_XREF_QUALIFIER_NAME, String.valueOf(manifest.getSample().getTaxId()));

      options.source = Optional.of(sourceFeature);
    }
    options.isWebinCLI = true;
    options.ignoreErrors = manifest.isIgnoreErrors();
    options.reportDir =
        Optional.of(new File(manifest.getReportFile().getAbsolutePath()).getParent());
    options.reportFile = Optional.of(manifest.getReportFile());
    options.processDir = Optional.of(manifest.getProcessDir().getAbsolutePath());

    // Set options specific to context
    if (manifest instanceof GenomeManifest) {
      if (!new AssemblyInfoNameCheck().isValidName(manifest.getName())) {
        reporter.writeToFile(
            manifest.getReportFile(),
            Severity.ERROR,
            "Invalid assembly name:" + manifest.getName());
        throw new ValidationEngineException(
            "Invalid assembly name:" + manifest.getName(),
            ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
      }
      AssemblyType assemblyTypeFromManifest =
          getAssemblyType(((GenomeManifest) manifest).getAssemblyType());
      if (assemblyTypeFromManifest != null) {
        options.assemblyType = assemblyTypeFromManifest;
      }
      if (options.assemblyType.equals(AssemblyType.COVID_19_OUTBREAK)
          && !manifest.getSample().getTaxId().equals(COVID_19_OUTBREAK_TAX_ID)) {
        String msg =
            String.format(
                "Sample organism must be 'Severe acute respiratory syndrome coronavirus 2' (taxid %d) for %s genomes.",
                COVID_19_OUTBREAK_TAX_ID, AssemblyType.COVID_19_OUTBREAK);
        reporter.writeToFile(manifest.getReportFile(), Severity.ERROR, msg);
        throw new ValidationEngineException(
            msg, ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
      }

      options.context = Optional.of(Context.genome);
      options.submissionFiles =
          Optional.of(setGenomeOptions((GenomeManifest) manifest, assemblyInfo));
    } else if (manifest instanceof TranscriptomeManifest) {
      options.context = Optional.of(Context.transcriptome);
      options.submissionFiles =
          Optional.of(setTranscriptomeOptions((TranscriptomeManifest) manifest, assemblyInfo));
    } else {
      options.context = Optional.of(Context.sequence);
      options.submissionFiles = Optional.of(setSequenceOptions((SequenceManifest) manifest));
    }
    options.assemblyInfoEntry = Optional.of(assemblyInfo);
    if (manifest.getWebinAuthToken() != null) {
      options.webinAuthToken = Optional.of(manifest.getWebinAuthToken());
      options.biosamplesWebinAuthToken = Optional.of(manifest.getWebinAuthToken());
    }

    if (manifest.getWebinRestUri() != null) {
      options.webinRestUri = Optional.of(manifest.getWebinRestUri());
    }

    if (manifest.getBiosamplesUri() != null) {
      options.biosamplesUri = Optional.of(manifest.getBiosamplesUri());
    }

    return options;
  }

  private Map<String, String> attributesListToMap(List<Attribute> attributesList) {
    Map<String, String> attributesMap = new HashMap<>();
    for (Attribute attribute : attributesList) {
      attributesMap.put(attribute.getName(), attribute.getValue());
    }
    return attributesMap;
  }

  public static AssemblyType getAssemblyType(String assemblyTypeStr)
      throws ValidationEngineException {
    try {
      if (assemblyTypeStr != null) {
        for (AssemblyType assemblyType : AssemblyType.values()) {
          if (assemblyType.getValue().equalsIgnoreCase(assemblyTypeStr)) {
            return assemblyType;
          }
        }
      }
    } catch (IllegalArgumentException ex) {
      throw new ValidationEngineException("No AssemblyType found for type: " + assemblyTypeStr);
    }
    return null;
  }

  private SubmissionFiles setGenomeOptions(
      GenomeManifest manifest, AssemblyInfoEntry assemblyInfo) {
    assemblyInfo.setAssemblyType(manifest.getAssemblyType());
    assemblyInfo.setPlatform(manifest.getPlatform());
    assemblyInfo.setProgram(manifest.getProgram());
    assemblyInfo.setMoleculeType(manifest.getMoleculeType());
    assemblyInfo.setCoverage(manifest.getCoverage());
    assemblyInfo.setMinGapLength(manifest.getMinGapLength());
    assemblyInfo.setTpa(manifest.isTpa());

    SubmissionFiles submissionFiles = new SubmissionFiles();
    manifest
        .files()
        .get(GenomeManifest.FileType.FASTA)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.FASTA,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    manifest
        .files()
        .get(GenomeManifest.FileType.AGP)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.AGP,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    manifest
        .files()
        .get(GenomeManifest.FileType.FLATFILE)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.FLATFILE,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    manifest
        .files()
        .get(GenomeManifest.FileType.CHROMOSOME_LIST)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.CHROMOSOME_LIST,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    manifest
        .files()
        .get(GenomeManifest.FileType.UNLOCALISED_LIST)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.UNLOCALISED_LIST,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    return submissionFiles;
  }

  private SubmissionFiles setSequenceOptions(SequenceManifest manifest) {
    SubmissionFiles submissionFiles = new SubmissionFiles();
    manifest
        .files()
        .get(SequenceManifest.FileType.FLATFILE)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.FLATFILE,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    manifest
        .files()
        .get(SequenceManifest.FileType.TAB)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.TSV,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    return submissionFiles;
  }

  private SubmissionFiles setTranscriptomeOptions(
      TranscriptomeManifest manifest, AssemblyInfoEntry assemblyInfo) {
    assemblyInfo.setName(manifest.getName());
    assemblyInfo.setPlatform(manifest.getPlatform());
    assemblyInfo.setProgram(manifest.getProgram());
    assemblyInfo.setTpa(manifest.isTpa());

    SubmissionFiles submissionFiles = new SubmissionFiles();
    manifest
        .files()
        .get(TranscriptomeManifest.FileType.FASTA)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.FASTA,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    manifest
        .files()
        .get(TranscriptomeManifest.FileType.FLATFILE)
        .forEach(
            file ->
                submissionFiles.addFile(
                    new SubmissionFile(
                        SubmissionFile.FileType.FLATFILE,
                        file.getFile(),
                        new File(file.getFile() + SequenceEntryUtils.FIXED_FILE_SUFFIX),
                        file.getReportFile())));
    return submissionFiles;
  }
}
