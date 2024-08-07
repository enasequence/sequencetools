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

import java.io.Serializable;

public class ExtendedMessage<T> extends ValidationMessage implements Serializable {

  private static final long serialVersionUID = 8142177734253096258L;

  private T extension = null;

  public ExtendedMessage(Severity severity, String messageKeyParam, Object... params) {
    super(severity, messageKeyParam, params);
  }

  public ExtendedMessage(ValidationMessage message) {
    super(message.getSeverity(), message.getMessageKey(), message.getParams());
    addOrigins(message.getOrigins());
  }

  public T getExtension() {
    return extension;
  }

  public void setExtension(T message) {
    this.extension = message;
  }

  public boolean isExtendedResult() {
    return true;
  }
}
