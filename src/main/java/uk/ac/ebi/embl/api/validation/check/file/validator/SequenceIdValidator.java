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
package uk.ac.ebi.embl.api.validation.check.file.validator;

import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

/** Validator for Sequence_id column values. Rule: Only alphabet letters and numbers are allowed. */
public class SequenceIdValidator implements ColumnValidator {

  private static final String COLUMN_NAME = "Sequence_id";

  @Override
  public ValidationResult validate(String value, int rowNumber, int columnIndex) {
    ValidationResult result = new ValidationResult();

    // Check for null or empty
    if (value == null || value.trim().isEmpty()) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Row %d, column %d (%s): Value must not be empty",
              rowNumber, columnIndex, COLUMN_NAME));
      result.append(message);
      return result;
    }

    // Check for alphanumeric only (letters, numbers, and common allowed characters like underscore,
    // hyphen, dot)
    // Allow alphanumeric plus underscore, hyphen, and dot which are common in sequence IDs
    if (!value.matches("^[a-zA-Z0-9_\\-\\.]+$")) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Row %d, column %d (%s): Invalid value '%s' - only alphabet letters and numbers are allowed",
              rowNumber, columnIndex, COLUMN_NAME, value));
      result.append(message);
    }

    return result;
  }

  @Override
  public String getColumnName() {
    return COLUMN_NAME;
  }
}
