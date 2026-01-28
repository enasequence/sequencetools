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

import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.validator.ConfidenceScoreValidator;
import uk.ac.ebi.embl.api.validation.check.file.validator.PercentMatchValidator;
import uk.ac.ebi.embl.api.validation.check.file.validator.PercentQueryCoverValidator;
import uk.ac.ebi.embl.api.validation.check.file.validator.ScientificNameValidator;
import uk.ac.ebi.embl.api.validation.check.file.validator.SequenceIdValidator;
import uk.ac.ebi.embl.api.validation.check.file.validator.TaxIdValidator;

/**
 * Header definition for SequenceTax TSV files. Expected formats: - 2 columns: Sequence_id, Tax_id -
 * 3 columns: Sequence_id, Tax_id, Scientific_name - 4 columns: Sequence_id, Tax_id,
 * Scientific_name, percent_match - 5 columns: Sequence_id, Tax_id, Scientific_name, percent_match,
 * percent_query_cover - 6 columns: Sequence_id, Tax_id, Scientific_name, percent_match,
 * percent_query_cover, confidence_score
 *
 * <p>The first two columns are required. Scientific_name and the three metadata columns are
 * optional.
 */
public class SequenceTaxHeaderDefinition extends TSVHeaderDefinition {

  private int columnCount = 0;
  private boolean hasScientificName = false;
  private boolean hasPercentMatch = false;
  private boolean hasPercentQueryCover = false;
  private boolean hasConfidenceScore = false;

  @Override
  public ValidationResult validateHeader(DataRow headerRow) {
    ValidationResult result = new ValidationResult();

    columnCount = headerRow.getLength();

    // Determine format (2-6 columns)
    if (columnCount < 2 || columnCount > 6) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Invalid SequenceTax file: expected 2-6 columns, found %d", headerRow.getLength()));
      result.append(message);
      return result;
    }

    // Check required columns (case-sensitive)
    if (!headerRow.getColumn(0).equals("Sequence_id")) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Invalid SequenceTax header: column 0 should be 'Sequence_id', found '%s'",
              headerRow.getColumn(0)));
      result.append(message);
    }

    if (!headerRow.getColumn(1).equals("Tax_id")) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Invalid SequenceTax header: column 1 should be 'Tax_id', found '%s'",
              headerRow.getColumn(1)));
      result.append(message);
    }

    // Check optional columns based on column count
    if (columnCount >= 3) {
      hasScientificName = true;
      if (!headerRow.getColumn(2).equals("Scientific_name")) {
        ValidationMessage<Origin> message =
            new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
        message.setMessage(
            String.format(
                "Invalid SequenceTax header: column 2 should be 'Scientific_name', found '%s'",
                headerRow.getColumn(2)));
        result.append(message);
      }
    }

    if (columnCount >= 4) {
      hasPercentMatch = true;
      if (!headerRow.getColumn(3).equals("percent_match")) {
        ValidationMessage<Origin> message =
            new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
        message.setMessage(
            String.format(
                "Invalid SequenceTax header: column 3 should be 'percent_match', found '%s'",
                headerRow.getColumn(3)));
        result.append(message);
      }
    }

    if (columnCount >= 5) {
      hasPercentQueryCover = true;
      if (!headerRow.getColumn(4).equals("percent_query_cover")) {
        ValidationMessage<Origin> message =
            new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
        message.setMessage(
            String.format(
                "Invalid SequenceTax header: column 4 should be 'percent_query_cover', found '%s'",
                headerRow.getColumn(4)));
        result.append(message);
      }
    }

    if (columnCount == 6) {
      hasConfidenceScore = true;
      if (!headerRow.getColumn(5).equals("confidence_score")) {
        ValidationMessage<Origin> message =
            new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
        message.setMessage(
            String.format(
                "Invalid SequenceTax header: column 5 should be 'confidence_score', found '%s'",
                headerRow.getColumn(5)));
        result.append(message);
      }
    }

    return result;
  }

  @Override
  public ValidationResult validateRow(DataRow dataRow, int rowNumber) {
    ValidationResult result = new ValidationResult();

    // Check column count matches header
    if (dataRow.getLength() != columnCount) {
      ValidationMessage<Origin> message =
          new ValidationMessage<>(Severity.ERROR, ValidationMessage.NO_KEY);
      message.setMessage(
          String.format(
              "Row %d: expected %d columns, found %d",
              rowNumber, columnCount, dataRow.getLength()));
      result.append(message);
      return result;
    }

    // Validate required columns
    result.append(new SequenceIdValidator().validate(dataRow.getString(0), rowNumber, 0));
    result.append(new TaxIdValidator().validate(dataRow.getString(1), rowNumber, 1));

    // Validate optional columns (if present in header)
    if (hasScientificName) {
      result.append(new ScientificNameValidator().validate(dataRow.getString(2), rowNumber, 2));
    }

    if (hasPercentMatch) {
      result.append(new PercentMatchValidator().validate(dataRow.getString(3), rowNumber, 3));
    }

    if (hasPercentQueryCover) {
      result.append(new PercentQueryCoverValidator().validate(dataRow.getString(4), rowNumber, 4));
    }

    if (hasConfidenceScore) {
      result.append(new ConfidenceScoreValidator().validate(dataRow.getString(5), rowNumber, 5));
    }

    return result;
  }

  public boolean hasScientificName() {
    return hasScientificName;
  }

  public boolean hasPercentMatch() {
    return hasPercentMatch;
  }

  public boolean hasPercentQueryCover() {
    return hasPercentQueryCover;
  }

  public boolean hasConfidenceScore() {
    return hasConfidenceScore;
  }

  public int getColumnCount() {
    return columnCount;
  }
}
