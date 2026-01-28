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

/**
 * Validator for Scientific_name column values. Rule: Optional field - any value is allowed
 * (including empty). This column may not be present in the TSV file.
 */
public class ScientificNameValidator implements ColumnValidator {

  private static final String COLUMN_NAME = "Scientific_name";

  @Override
  public ValidationResult validate(String value, int rowNumber, int columnIndex) {
    // Scientific_name is optional and accepts any value
    // No validation errors to report
    return new ValidationResult();
  }

  @Override
  public String getColumnName() {
    return COLUMN_NAME;
  }
}
