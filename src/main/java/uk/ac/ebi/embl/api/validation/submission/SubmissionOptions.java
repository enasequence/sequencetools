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
package uk.ac.ebi.embl.api.validation.submission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class SubmissionOptions {
  public Optional<SubmissionFiles> submissionFiles = Optional.empty();
  public Optional<Context> context = Optional.empty();
  public Optional<AssemblyInfoEntry> assemblyInfoEntry = Optional.empty();
  public Optional<List<String>> locusTagPrefixes = Optional.empty();
  public Optional<SourceFeature> source = Optional.empty();
  public Optional<String> analysisId = Optional.empty();
  public Optional<Connection> enproConnection = Optional.empty();
  public Optional<Connection> eraproConnection = Optional.empty();
  public Optional<String> reportDir = Optional.empty();
  public Optional<Integer> minGapLength = Optional.empty();
  public Optional<String> processDir = Optional.empty();
  public Optional<File> reportFile = Optional.empty();
  public Optional<Boolean> ignoreError = Optional.empty();
  public Optional<String> webinRestUri = Optional.empty();
  public Optional<String> webinAuthToken = Optional.empty();
  public Optional<String> webinUsername = Optional.empty();
  public Optional<String> webinPassword = Optional.empty();

  public Optional<ServiceConfig> serviceConfig = Optional.empty();

  public Optional<String> webinAuthUri = Optional.empty();
  public Optional<String> biosamplesUri = Optional.empty();
  public Optional<String> biosamplesWebinAuthToken = Optional.empty();
  public Optional<String> biosamplesWebinUsername = Optional.empty();
  public Optional<String> biosamplesWebinPassword = Optional.empty();

  private EmblEntryValidationPlanProperty property = null;

  public boolean isDevMode = false;
  public boolean isFixMode = true;
  public boolean isFixCds = true;
  public boolean ignoreErrors = false;
  public boolean isWebinCLI = false;
  public boolean forceReducedFlatfileCreation = false;
  private String projectId;
  private String templateId;

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public void init() throws ValidationEngineException {
    if (!submissionFiles.isPresent()
        || submissionFiles.get().getFiles() == null
        || submissionFiles.get().getFiles().isEmpty())
      throw new ValidationEngineException("SubmissionOptions:submissionFiles must be provided");
    if (!context.isPresent())
      throw new ValidationEngineException("SubmissionOptions:context must be provided");
    if (!assemblyInfoEntry.isPresent() && isWebinCLI)
      throw new ValidationEngineException("SubmissionOptions:assemblyinfoentry must be provided");
    if (!source.isPresent() && isWebinCLI) {
      if (Context.sequence != context.get())
        throw new ValidationEngineException("SubmissionOptions:source must be provided");
    }
    if (!reportDir.isPresent())
      throw new ValidationEngineException("SubmissionOptions:reportDir must be provided");
    if (!isWebinCLI || isDevMode) {
      if (!(new File(reportDir.get())).isDirectory())
        throw new ValidationEngineException("SubmissionOptions:invalid ReportDir");
    } else {
      for (SubmissionFile file : submissionFiles.get().getFiles()) {
        if (file.getReportFile() == null) {
          throw new ValidationEngineException(
              "SubmissionOptions:reportFile is mandatory for each file.");
        }
      }
    }
    if (!analysisId.isPresent() && !isWebinCLI)
      throw new ValidationEngineException("SubmissionOptions:analysisId must be provided.");
    if (!processDir.isPresent()
        && !isWebinCLI
        && (context.get() == Context.genome || context.get() == Context.transcriptome))
      throw new ValidationEngineException(
          "SubmissionOptions:processDir must be provided to write master file");
    if (processDir.isPresent()) {
      try {
        if (Files.notExists(Paths.get(processDir.get(), "reduced"))) {
          Files.createDirectory(Paths.get(processDir.get(), "reduced"));
        }
      } catch (IOException e) {
        throw new ValidationEngineException("Could not create a reduced file directory", e);
      }
    }
    if (!enproConnection.isPresent() || !eraproConnection.isPresent()) {
      if (!isWebinCLI) {
        throw new ValidationEngineException(
            "SubmissionOptions:Database connections(ENAPRO,ERAPRO) must be given when validating submission internally");
      }
    }

    if (!isWebinCLI && ignoreError.isPresent()) {
      ignoreErrors = ignoreError.get();
    }

    if (!isWebinCLI && context.get() == Context.sequence && !serviceConfig.isPresent()) {
      throw new ValidationEngineException(
          "SubmissionOptions:Service configuration is mandatory for sequence context");
    }
  }

  public EmblEntryValidationPlanProperty getEntryValidationPlanProperty() {
    if (property != null) return property;

    property = new EmblEntryValidationPlanProperty();
    property.isFixMode.set(isFixMode);
    property.isFixCds.set(isFixCds);
    if (locusTagPrefixes.isPresent()) property.locus_tag_prefixes.set(locusTagPrefixes.get());
    if (enproConnection.isPresent()) property.enproConnection.set(enproConnection.get());
    if (eraproConnection.isPresent()) property.eraproConnection.set(eraproConnection.get());
    if (analysisId.isPresent()) property.analysis_id.set(analysisId.get());
    if (assemblyInfoEntry.isPresent()) {
      Integer mgl =
          minGapLength.isPresent() ? minGapLength.get() : assemblyInfoEntry.get().getMinGapLength();
      if (mgl != null) property.minGapLength.set(mgl);
    }
    if (Context.genome.equals(context.get())) {
      property.sequenceNumber.set(1);
    }
    property.ignore_errors.set(ignoreErrors);
    property.taxonClient.set(new TaxonomyClient());
    property.isRemote.set(isWebinCLI);

    if (webinRestUri.isPresent() && webinUsername.isPresent() && webinPassword.isPresent()) {
      property.webinRestUri.set(webinRestUri.get());
      property.webinUsername.set(webinUsername.get());
      property.webinPassword.set(webinPassword.get());
    }

    if (webinAuthUri.isPresent()
        && biosamplesUri.isPresent()
        && biosamplesWebinUsername.isPresent()
        && biosamplesWebinPassword.isPresent()) {
      property.webinAuthUri.set(webinAuthUri.get());
      property.biosamplesUri.set(biosamplesUri.get());
      property.biosamplesWebinUsername.set(biosamplesWebinUsername.get());
      property.biosamplesWebinPassword.set(biosamplesWebinPassword.get());
    }

    return property;
  }

  public String getWebinERAServiceUrl() {
    return serviceConfig.get().getEraServiceUrl();
  }

  public String getWebinERAServiceUser() {
    return serviceConfig.get().getEraServiceUser();
  }

  public String getWebinERAServicePassword() {
    return serviceConfig.get().getEraServicePassword();
  }
}
