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

import java.util.ArrayList;
import java.util.List;

public class TokenIncrementor {
  /**
   * looks in the template to see if there are incremented and/or decremented values of numeric
   * tokens - inserts the needed tokens
   */
  public void processIncrementAndDecrementTokens(
      TemplateInfo templateInfo, TemplateVariables variables) throws TemplateException {
    List<TemplateTokenInfo> tokenInfos = templateInfo.getTokens();
    List<TemplateTokenInfo> numericTokens = identifyLocationTokens(tokenInfos);
    for (TemplateTokenInfo token : numericTokens) processEntryRow(variables, token);
  }

  private void processEntryRow(TemplateVariables templateVariables, TemplateTokenInfo token)
      throws TemplateException {
    if (templateVariables.containsToken(token.getName())) {
      String tokenName = token.getName();
      String tokenValue = templateVariables.getTokenValue(tokenName);
      if (tokenValue == null) return;
      String tokenDecrementName = tokenName + "--";
      String tokenIncrementName = tokenName + "++";
      if (token.getType() == TemplateTokenType.start_location
          || token.getType() == TemplateTokenType.end_location) {
        String partialityToken = extractPartialLocationToken(tokenValue);
        int intValue = stripPartialLocationValue(tokenValue);
        templateVariables.addToken(
            tokenDecrementName, partialityToken.concat(String.valueOf(intValue - 1)));
        templateVariables.addToken(
            tokenIncrementName, partialityToken.concat(String.valueOf(intValue + 1)));
      } else {
        int intValue = stringToInt(tokenValue);
        templateVariables.addToken(tokenDecrementName, String.valueOf(intValue - 1));
        templateVariables.addToken(tokenIncrementName, String.valueOf(intValue + 1));
      }
    }
  }

  private Integer stringToInt(String tokenValue) throws TemplateException {
    try {
      return Integer.valueOf(tokenValue);
    } catch (NumberFormatException e) {
      throw new TemplateException(e);
    }
  }

  private List<TemplateTokenInfo> identifyLocationTokens(List<TemplateTokenInfo> tokenInfos) {

    List<TemplateTokenInfo> numericTokens = new ArrayList<TemplateTokenInfo>();
    for (TemplateTokenInfo tokenInfo : tokenInfos) {
      if (tokenInfo.getType() == TemplateTokenType.start_location
          || tokenInfo.getType() == TemplateTokenType.end_location) {

        numericTokens.add(tokenInfo);
      }
    }
    return numericTokens;
  }

  public String extractPartialLocationToken(String tokenValue) {
    if (tokenValue.startsWith("<")) {
      return "<";
    } else if (tokenValue.endsWith(">")) {
      return ">";
    }

    return ""; // empty string rather than null - saves having to check for null results
  }

  public static int stripPartialLocationValue(String tokenValue) throws TemplateException {
    try {
      return Integer.parseInt(tokenValue.replaceAll("<", "").replaceAll(">", ""));
    } catch (NumberFormatException e) {
      throw new TemplateException(e);
    }
  }
}
