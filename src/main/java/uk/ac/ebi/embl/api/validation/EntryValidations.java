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
package uk.ac.ebi.embl.api.validation;

public class EntryValidations {

  private EntryValidations() { // prevent instantiations
  }

  public static void init() {}

  /**
   * Creates a validation message for validation.
   *
   * @param origin - the origin for the message
   * @param severity severity of the message
   * @param messageKey a key of the message
   * @param params parameters of the message
   * @return a validation message
   */
  public static ValidationMessage<Origin> createMessage(
      Origin origin, Severity severity, String messageKey, Object... params) {
    return ValidationMessage.message(severity, messageKey, params).append(origin);
  }

  /**
   * Creates a validation message for validation.
   *
   * @param origins - the origins for the message
   * @param severity severity of the message
   * @param messageKey a key of the message
   * @param params parameters of the message
   * @return a validation message
   */
  //	public static ValidationMessage<Origin> createMessage(Collection<Origin> origins, Severity
  // severity,
  //                                                          String messageKey, Object... params) {
  //		return ValidationMessage.message(severity, messageKey, params).append(origins);
  //	}

}
