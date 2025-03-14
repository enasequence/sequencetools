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
package uk.ac.ebi.embl.template;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class TemplateProcessorResultSet {
  private ValidationResult validationResult;
  private String entryString;
  private Entry entry;

  public TemplateProcessorResultSet(ValidationResult validationResult, String entryString) {
    this.validationResult = validationResult;
    this.entryString = entryString;
  }

  public TemplateProcessorResultSet(String entryString) {
    this.entryString = entryString;
    this.validationResult = new ValidationResult();
  }

  public TemplateProcessorResultSet() {
    this.validationResult = new ValidationResult();
  }

  public ValidationResult getValidationResult() {
    return validationResult;
  }

  public void setValidationResult(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  public void setEntryString(String entryString) {
    this.entryString = entryString;
  }

  public String getEntryString() {
    return entryString;
  }

  public Entry getEntry() {
    return entry;
  }

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  @Override
  public String toString() {
    return "TemplateProcessorResultSet{"
        + "validationPlanResult="
        + validationResult
        + ", entryString='"
        + entryString
        + '\''
        + '}';
  }
}
