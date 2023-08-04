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
package uk.ac.ebi.embl.template;

import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class ITSTemplatePreProcessor implements TemplatePreProcessor {
  public static final String WRONG_TOKENS_MESSAGE = "ITSTemplatePreProcessor";

  public ValidationResult process(TemplateVariables variablesMap) {
    ValidationResult validationResult = new ValidationResult();
    String token18s = variablesMap.getTokenValue("18S");
    String tokenITS1 = variablesMap.getTokenValue("ITS1");
    String token5point8S = variablesMap.getTokenValue("5.8S");
    String tokenITS2 = variablesMap.getTokenValue("ITS2");
    String token28S = variablesMap.getTokenValue("28S");
    /** build the NOTES token based on the selection of tokens */
    boolean proceedingToken = false;
    /** keep a record of whether all the tokens are false or not */
    boolean allTokensFalse = true;
    StringBuilder builder = new StringBuilder();
    if (token18s != null
        && !token18s.isEmpty()
        && token18s.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
      builder.append("18S rRNA gene");
      proceedingToken = true;
      allTokensFalse = false;
    }
    if (tokenITS1 != null
        && !tokenITS1.isEmpty()
        && tokenITS1.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
      if (proceedingToken) {
        builder.append(", ");
      }
      builder.append("ITS1");
      proceedingToken = true;
      allTokensFalse = false;
    }
    if (token5point8S != null
        && !token5point8S.isEmpty()
        && token5point8S.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
      if (proceedingToken) builder.append(", ");
      builder.append("5.8S rRNA gene");
      proceedingToken = true;
      allTokensFalse = false;
    }
    if (tokenITS2 != null
        && !tokenITS2.isEmpty()
        && tokenITS2.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
      if (proceedingToken) builder.append(", ");
      builder.append("ITS2");
      proceedingToken = true;
      allTokensFalse = false;
    }
    if (token28S != null
        && !token28S.isEmpty()
        && token28S.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE)) {
      if (proceedingToken) builder.append(", ");
      builder.append("28S rRNA gene");
      allTokensFalse = false;
    }
    if (!builder.toString().isEmpty())
      variablesMap.addToken("PP_NOTES", "sequence contains " + builder.toString());
    /** Add error if none of the ITS tokens are selected */
    if (allTokensFalse) {
      ValidationMessage<Origin> message =
          ValidationMessage.message(Severity.ERROR, WRONG_TOKENS_MESSAGE);
      validationResult.append(message);
    }
    return validationResult;
  }
}
