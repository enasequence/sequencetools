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
package uk.ac.ebi.embl.api.validation.helper;

import java.util.List;
import uk.ac.ebi.embl.api.validation.ValidationMessage;

public class FlattenedMessageResult {
  private final List<ValidationMessage> flattenedMessages;
  private final List<ValidationMessage> unFlattenedMessages;

  public FlattenedMessageResult(
      List<ValidationMessage> flattenedMessages, List<ValidationMessage> unFlattenedMessages) {
    this.flattenedMessages = flattenedMessages;
    this.unFlattenedMessages = unFlattenedMessages;
  }

  public List<ValidationMessage> getFlattenedMessages() {
    return flattenedMessages;
  }

  public List<ValidationMessage> getUnFlattenedMessages() {
    return unFlattenedMessages;
  }
}
