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

import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.storage.tsv.TSVReader;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.common.CommonUtil;
import uk.ac.ebi.embl.fasta.reader.FastaFileReader;
import uk.ac.ebi.embl.fasta.reader.FastaLineReader;
import uk.ac.ebi.embl.template.PolySample;
import uk.ac.ebi.embl.template.SampleTax;

public class PolySampleValidationCheck extends FileValidationCheck {
  public PolySampleValidationCheck(SubmissionOptions options, SharedInfo sharedInfo) {
    super(options, sharedInfo);
  }

  @Override
  public ValidationResult check(SubmissionFile file) throws ValidationEngineException {
    return null;
  }

  @Override
  public ValidationResult check() throws ValidationEngineException {
    return validatePolySampleSubmission();
  }

  // PolySample FASTA is validated to ensure that the submitted_acc exists in the TSV file.
  public ValidationResult validatePolySampleSubmission() {

    ValidationResult validationResult = new ValidationResult();

    File fasta = getSubmitedFileByType(SubmissionFile.FileType.FASTA);
    File sampleTsv = getSubmitedFileByType(SubmissionFile.FileType.SAMPLE_TSV);

    try (BufferedReader fileReader = CommonUtil.bufferedReaderFromFile(fasta)) {
      FastaFileReader reader = new FastaFileReader(new FastaLineReader(fileReader));
      ValidationResult parseResult = reader.read();
      validationResult.append(parseResult);
      // Validate submitted accession found in fasta
      List<PolySample> polySamples = new TSVReader().getPolySamples(sampleTsv);
      Set<String> submittedAcc =
          polySamples.stream()
              .map(polySample -> polySample.getSubmittedAccession())
              .collect(Collectors.toSet());

      while (reader.isEntry()) {
        Entry entry = reader.getEntry();
        if (!submittedAcc.contains(entry.getSubmitterAccession())) {
          validationResult.append(
              new ValidationMessage<>(
                  Severity.ERROR,
                  "Accession: "
                      + entry.getSubmitterAccession()
                      + " is not mapped in the sample TSV file."));
        }

        // sequenceCount is used for setting the accession range.
        sharedInfo.sequenceCount++;

        reader.read();
      }

      File taxTsv = getSubmitedFileByType(SubmissionFile.FileType.TAX_TSV);
      Map<String, SampleTax> sampleTaxMap = new TSVReader().getSampleTax(taxTsv);

      Set<String> polySampleIds =
          polySamples.stream().map(PolySample::getSampleId).collect(Collectors.toSet());

      List<SampleTax> missing =
          sampleTaxMap.values().stream()
              .filter(t -> !polySampleIds.contains(t.sampleId()))
              .toList();

      for (SampleTax entry : missing) {
        validationResult.append(
            new ValidationMessage<>(
                Severity.ERROR,
                "Sample: " + entry.sampleId() + " is not mapped in the sample TSV file."));
      }
    } catch (Exception e) {
      validationResult.append(new ValidationMessage(Severity.ERROR, e.getLocalizedMessage()));
    }

    return validationResult;
  }

  public File getSubmitedFileByType(SubmissionFile.FileType fileType) {
    if (options.submissionFiles.map(files -> files.getFiles(fileType)).isEmpty()) {
      return null;
    }
    return options
        .submissionFiles
        .map(files -> files.getFiles(fileType))
        .filter(list -> !list.isEmpty())
        .map(list -> list.get(0).getFile())
        .orElseThrow(() -> new IllegalStateException("No " + fileType.name() + " file found"));
  }
}
