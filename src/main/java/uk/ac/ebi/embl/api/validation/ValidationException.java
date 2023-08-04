/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation;

/**
 * @author dlorenc
 *     <p>This is validation exception which provides details on validation result. It contains a
 *     validation message.
 */
public class ValidationException extends Exception {

  private static final long serialVersionUID = -3183534639462274123L;

  private ValidationMessage<Origin> validationMessage;

  public ValidationException(ValidationMessage<Origin> validationMessage) {
    this.validationMessage = validationMessage;
  }

  public ValidationMessage<Origin> getValidationMessage() {
    return validationMessage;
  }

  @Override
  public String getMessage() {
    return validationMessage.getMessage();
  }

  /**
   * Creates a ValidationException which contains a ValidationMessage error.
   *
   * @param messageKey a message key
   * @param params message parameters
   * @return a validation exception with a ValidationMessage error
   */
  public static ValidationException error(String messageKey, Object... params) {
    return new ValidationException(ValidationMessage.error(messageKey, params));
  }

  /**
   * Creates a ValidationException which contains a ValidationMessage warning.
   *
   * @param messageKey a message key
   * @param params message parameters
   * @return a validation exception with a ValidationMessage warning
   */
  public static ValidationException warning(String messageKey, Object... params) {
    return new ValidationException(ValidationMessage.warning(messageKey, params));
  }

  /**
   * Creates a ValidationException which contains a ValidationMessage info.
   *
   * @param messageKey a message key
   * @param params message parameters
   * @return a validation exception with a ValidationMessage info
   */
  public static ValidationException info(String messageKey, Object... params) {
    return new ValidationException(ValidationMessage.info(messageKey, params));
  }

  /**
   * Throws a ValidationException which contains a ValidationMessage error.
   *
   * @param messageKey a message key
   * @param params message parameters
   * @throws ValidationException always thrown
   */
  public static void throwError(String messageKey, Object... params) throws ValidationException {
    throw error(messageKey, params);
  }

  /**
   * Throws a ValidationException which contains a ValidationMessage warning.
   *
   * @param messageKey a message key
   * @param params message parameters
   * @throws ValidationException always thrown
   */
  public static void throwWarning(String messageKey, Object... params) throws ValidationException {
    throw warning(messageKey, params);
  }

  /**
   * Throws a ValidationException which contains a ValidationMessage info.
   *
   * @param messageKey a message key
   * @param params message parameters
   * @throws ValidationException always thrown
   */
  public static void throwInfo(String messageKey, Object... params) throws ValidationException {
    throw info(messageKey, params);
  }
}
