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

import java.util.Arrays;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.validator.FrequencyValidator;
import uk.ac.ebi.embl.api.validation.check.file.validator.SampleIdValidator;
import uk.ac.ebi.embl.api.validation.check.file.validator.SequenceIdValidator;

/**
 * Header definition for PolySample TSV files. Expected format: 3 columns (Sequence_id, Sample_id,
 * Frequency)
 */
public class PolySampleHeaderDefinition extends TSVHeaderDefinition {

  private static final String[] EXPECTED_HEADERS = {"Sequence_id", "Sample_id", "Frequency"};

  public PolySampleHeaderDefinition() {
    this.expectedHeaders = EXPECTED_HEADERS;
    this.columnValidators =
        Arrays.asList(new SequenceIdValidator(), new SampleIdValidator(), new FrequencyValidator());
    this.allowExtraColumns = false;
  }

  @Override
  public ValidationResult validateHeader(DataRow headerRow) {
    ValidationResult result = new ValidationResult();

    // Check column count
    if (headerRow.getLength() != 3) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Invalid PolySample file: expected 3 columns, found %d", headerRow.getLength()));
      result.append(message);
      return result;
    }

    // Check column names (case-sensitive)
    for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
      if (!headerRow.getColumn(i).equals(EXPECTED_HEADERS[i])) {
        ValidationMessage<Origin> message =
            new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
        message.setMessage(
            String.format(
                "Invalid PolySample header: column %d should be '%s', found '%s'",
                i, EXPECTED_HEADERS[i], headerRow.getColumn(i)));
        result.append(message);
      }
    }

    return result;
  }

  @Override
  public ValidationResult validateRow(DataRow dataRow, int rowNumber) {
    ValidationResult result = new ValidationResult();

    // Check column count
    if (dataRow.getLength() != 3) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format("Row %d: expected 3 columns, found %d", rowNumber, dataRow.getLength()));
      result.append(message);
      return result;
    }

    // Validate each column
    for (int i = 0; i < columnValidators.size(); i++) {
      String value = dataRow.getString(i);
      ValidationResult colResult = columnValidators.get(i).validate(value, rowNumber, i);
      result.append(colResult);
    }

    return result;
  }
}
