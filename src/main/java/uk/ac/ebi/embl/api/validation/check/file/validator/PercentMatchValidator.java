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

/**
 * Validator for percent_match column values. Rule: Must be a float or a range of floats (e.g.,
 * "99.8" or "98.7-99.2"). The percentage of sequence identity between a query sequence and a
 * reference sequence. Optional field - can be empty.
 */
public class PercentMatchValidator implements ColumnValidator {

  private static final String COLUMN_NAME = "percent_match";

  @Override
  public ValidationResult validate(String value, int rowNumber, int columnIndex) {
    ValidationResult result = new ValidationResult();

    // Optional field - empty is allowed
    if (value == null || value.trim().isEmpty()) {
      return result;
    }

    // Check if value is a valid float or range of floats (e.g., "99.8" or "98.7-99.2")
    // Pattern: single float or two floats separated by hyphen
    if (!value.matches("^\\d+(\\.\\d+)?(\\s*-\\s*\\d+(\\.\\d+)?)?$")) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Row %d, column %d (%s): Invalid value '%s' - must be a number or range (e.g., '99.8' or '98.7-99.2')",
              rowNumber, columnIndex, COLUMN_NAME, value));
      result.append(message);
      return result;
    }

    // Validate that percentages are in valid range (0-100)
    try {
      String[] parts = value.split("-");
      for (String part : parts) {
        double val = Double.parseDouble(part.trim());
        if (val < 0 || val > 100) {
          ValidationMessage<Origin> message =
              new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
          message.setMessage(
              String.format(
                  "Row %d, column %d (%s): Invalid value '%s' - percentage must be between 0 and 100",
                  rowNumber, columnIndex, COLUMN_NAME, value));
          result.append(message);
          return result;
        }
      }
    } catch (NumberFormatException e) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Row %d, column %d (%s): Invalid value '%s' - must be a valid number or range",
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
