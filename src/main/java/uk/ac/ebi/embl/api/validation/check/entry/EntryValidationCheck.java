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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.*;

public abstract class EntryValidationCheck extends EmblEntryValidationCheck<Entry> {

  protected ValidationResult result;

  protected EntryValidationCheck() {
    result = new ValidationResult();
  }

  public void init() {
    // no implementation, override when needed
  }

  /**
   * Creates an error validation message for the entry and adds it to the validation result.
   *
   * @param origin
   * @param messageKey a message key
   * @param params message parameters
   */
  protected ValidationMessage<Origin> reportError(
      Origin origin, String messageKey, Object... params) {
    return reportMessage(Severity.ERROR, origin, messageKey, params);
  }

  /**
   * Creates a warning validation message for the entry and adds it to the validation result.
   *
   * @param origin
   * @param messageKey a message key
   * @param params message parameters
   */
  protected ValidationMessage<Origin> reportWarning(
      Origin origin, String messageKey, Object... params) {
    return reportMessage(Severity.WARNING, origin, messageKey, params);
  }

  /**
   * Creates a validation message for the entry and adds it to the validation result.
   *
   * @param severity message severity
   * @param origin
   * @param messageKey a message key
   * @param params message parameters
   */
  protected ValidationMessage<Origin> reportMessage(
      Severity severity, Origin origin, String messageKey, Object... params) {
    ValidationMessage<Origin> message =
        EntryValidations.createMessage(origin, severity, messageKey, params);
    result.append(message);
    return message;
  }
}
