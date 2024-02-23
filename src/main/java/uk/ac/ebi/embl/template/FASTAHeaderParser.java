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

import java.util.Map;

public class FASTAHeaderParser {
  public static final String FASTA_DELIMITER = ";";
  public static final String FASTA_KEY_VALUE_DELIMITER = "\\[";

  public void processFastaHeaderWithOrders(
      Map<Integer, TemplateToken> tokenOrders,
      String currentLine,
      TemplateVariables currentTokenVals) {
    String[] headerTokens = currentLine.split(FASTA_DELIMITER);
    for (int i = 1; i < headerTokens.length + 1; i++) {
      String tokenValue = headerTokens[i - 1];
      tokenValue = tokenValue.replaceAll("<br>", "\n");
      if (tokenOrders.containsKey(i)) {
        TemplateToken token = tokenOrders.get(i);
        String tokenName = token.getName();
        currentTokenVals.addToken(tokenName, tokenValue);
      }
    }
  }

  public void processFastaHeaderWithKeyValuePairs(
      Map<String, TemplateToken> variableTokenDisplayNames,
      String currentLine,
      TemplateVariables currentTokenVals)
      throws TemplateException {
    String[] headerTokens = currentLine.split(FASTA_KEY_VALUE_DELIMITER);
    for (int i = 1; i < headerTokens.length + 1; i++) {
      String keyTokenValue = headerTokens[i - 1];
      keyTokenValue = keyTokenValue.trim();
      if (keyTokenValue.endsWith("]")) {
        keyTokenValue = keyTokenValue.replaceAll("\\]", "");
      }
      keyTokenValue = keyTokenValue.replaceAll("<br>", "\n");
      if (keyTokenValue.contains("=")) {
        String[] tokens = keyTokenValue.split("=");
        if (tokens.length != 2) {
          throw new TemplateException(
              "Token does not have value before/following '=' " + keyTokenValue);
        }
        String tokenDisplayName = tokens[0];
        String tokenValue = tokens[1];
        if (variableTokenDisplayNames.containsKey(tokenDisplayName)) {
          String tokenName = variableTokenDisplayNames.get(tokenDisplayName).getName();
          currentTokenVals.addToken(tokenName, tokenValue);
        }
      }
    }
  }
}
