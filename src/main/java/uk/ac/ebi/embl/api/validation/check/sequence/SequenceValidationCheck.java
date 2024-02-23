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
package uk.ac.ebi.embl.api.validation.check.sequence;

import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;

public abstract class SequenceValidationCheck extends EmblEntryValidationCheck<Sequence> {

  private Origin origin;

  protected SequenceValidationCheck() {}

  public void init() {
    // no implementation - override when needed
  }

  public Origin getOrigin() {
    return origin;
  }

  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  /**
   * Creates an error validation message for the sequence and adds it to the validation result.
   *
   * @param messageKey a message key
   * @param params message parameters
   */
  protected void reportError(ValidationResult result, String messageKey, Object... params) {
    reportMessage(result, Severity.ERROR, messageKey, params);
  }

  /**
   * Creates a warning validation message for the sequence and adds it to the validation result.
   *
   * @param messageKey a message key
   * @param params message parameters
   */
  protected void reportWarning(ValidationResult result, String messageKey, Object... params) {
    reportMessage(result, Severity.WARNING, messageKey, params);
  }

  /**
   * Creates a validation message for the sequence and adds it to the validation result.
   *
   * @param severity message severity
   * @param messageKey a message key
   * @param params message parameters
   */
  protected void reportMessage(
      ValidationResult result, Severity severity, String messageKey, Object... params) {
    result.append(EntryValidations.createMessage(origin, severity, messageKey, params));
  }
}
