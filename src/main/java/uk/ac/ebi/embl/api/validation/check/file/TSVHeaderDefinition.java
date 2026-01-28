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

import java.util.List;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.file.validator.ColumnValidator;

/**
 * Abstract base class for TSV header definitions. Encapsulates validation logic for both header
 * structure and row values.
 */
public abstract class TSVHeaderDefinition {

  protected String[] expectedHeaders;
  protected List<ColumnValidator> columnValidators;
  protected boolean allowExtraColumns = false;

  /**
   * Validates the header row structure.
   *
   * @param headerRow the header row to validate
   * @return ValidationResult containing any validation errors
   */
  public abstract ValidationResult validateHeader(DataRow headerRow);

  /**
   * Validates a data row's values.
   *
   * @param dataRow the data row to validate
   * @param rowNumber the row number (1-indexed) for error reporting
   * @return ValidationResult containing any validation errors
   */
  public abstract ValidationResult validateRow(DataRow dataRow, int rowNumber);

  /**
   * Gets the expected header column names.
   *
   * @return array of expected header names
   */
  public String[] getExpectedHeaders() {
    return expectedHeaders;
  }

  /**
   * Gets the column validators for this header definition.
   *
   * @return list of column validators
   */
  public List<ColumnValidator> getColumnValidators() {
    return columnValidators;
  }
}
