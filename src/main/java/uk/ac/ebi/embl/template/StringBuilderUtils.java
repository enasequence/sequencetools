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

import java.util.regex.Pattern;

public class StringBuilderUtils {
  public static final String tokenRegex = "^([\\s\\S]*)(\\{\\S*\\})([\\s\\S]*)$";
  public static final Pattern tokenPattern = Pattern.compile(tokenRegex);

  public static String encloseToken(String token) {
    return TemplateProcessorConstants.TOKEN_DELIMITER
        + token
        + TemplateProcessorConstants.TOKEN_CLOSE_DELIMITER;
  }

  static boolean doesBuilderContain(String tokenName, StringBuilder stringBuilder) {

    return stringBuilder.indexOf(tokenName) != -1;
  }

  static void deleteAllOccurrences(String toDelete, StringBuilder stringBuilder) {

    while (doesBuilderContain(toDelete, stringBuilder)) {
      deleteString(toDelete, stringBuilder);
    }
  }

  static void deleteString(String tokenName, StringBuilder stringBuilder) {

    int tokenIndex = stringBuilder.indexOf(tokenName);
    if (tokenIndex != -1) {
      stringBuilder.delete(tokenIndex, tokenIndex + tokenName.length());
    }
  }

  public static void removeUnmatchedTokenLines(StringBuilder stringBuilder) {
    StringBuilderLineIterator lineIterator = new StringBuilderLineIterator(stringBuilder);
    while (lineIterator.hasNext()) {
      String currentLine = lineIterator.next();
      boolean tokenMatched = tokenPattern.matcher(currentLine).matches();
      String trimmedLine = currentLine.trim();
      if (tokenMatched || trimmedLine.isEmpty() || trimmedLine.equals("\n")) lineIterator.remove();
    }
  }

  public static void deleteBetweenStrings(
      String fromToken, String toToken, StringBuilder currentBuilder) {
    int tokenIndex = currentBuilder.indexOf(fromToken);
    // add on the length of the token as we dont want this
    int tokenCloseIndex = currentBuilder.indexOf(toToken) + toToken.length();

    if (tokenIndex != -1 && tokenCloseIndex != -1 && tokenCloseIndex > tokenIndex) {
      currentBuilder.delete(tokenIndex, tokenCloseIndex);
    }
  }

  public static void doReplace(String stringToFind, String stringToReplace, StringBuilder builder) {

    int replaceIndex = builder.indexOf(stringToFind);
    if (replaceIndex != -1) {
      builder.replace(replaceIndex, replaceIndex + stringToFind.length(), stringToReplace);
    }

    //        String currentEntryString = builder.toString();
    //        builder = new StringBuilder(currentEntryString.replace(stringToFind,
    // stringToReplace));
  }
}
