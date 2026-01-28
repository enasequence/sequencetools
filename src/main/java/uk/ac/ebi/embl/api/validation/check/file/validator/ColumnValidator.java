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

import uk.ac.ebi.embl.api.validation.ValidationResult;

/** Interface for validating individual column values in TSV files. */
public interface ColumnValidator {

  /**
   * Validates a column value.
   *
   * @param value the value to validate
   * @param rowNumber the row number (1-indexed) for error reporting
   * @param columnIndex the column index (0-indexed) for error reporting
   * @return ValidationResult containing any validation errors
   */
  ValidationResult validate(String value, int rowNumber, int columnIndex);

  /**
   * Gets the name of the column being validated.
   *
   * @return the column name
   */
  String getColumnName();
}
