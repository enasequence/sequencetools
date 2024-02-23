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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CSVWriter {
  public static final String UPLOAD_DELIMITER = "\t";

  private PrintWriter lineWriter;

  /** goes at the top of the upload files as guidance */
  private static final String UPLOAD_COMMENTS =
      "#Fields must be single-line. Do not put return characters into fields.\n"
          + "#If you save as CSV as columns are separated with the comma character use ; to represent commas - these will be converted automatically\n"
          + "#If fields are the same in all entries copy the values down using your spreadsheet software.";

  /** Writes the download file for variables for spreadsheet import */
  public void writeTemplateSpreadsheetDownloadFile(
      List<TemplateTokenInfo> tokenNames, TemplateVariablesSet variablesSet, String filePath)
      throws TemplateException {

    prepareWriter(filePath);
    writeDownloadSpreadsheetHeader(tokenNames);

    for (int i = 1; i < variablesSet.getEntryCount() + 1; i++) {
      TemplateVariables entryVariables = variablesSet.getEntryValues(i);
      writeSpreadsheetVariableRow(tokenNames, i, entryVariables);
    }

    flushAndCloseWriter();
  }

  /**
   * Used to write spreadsheets where entry number exceeds the MEGABULK size. Does not use the
   * variables map stored in the database, just writes constants.
   */
  public void writeLargeTemplateSpreadsheetDownloadFile(
      List<TemplateTokenInfo> tokenNames,
      int entryCount,
      TemplateVariables constants,
      String filePath)
      throws TemplateException {

    prepareWriter(filePath);
    writeDownloadSpreadsheetHeader(tokenNames);

    for (int i = 1; i < entryCount + 1; i++) {
      writeSpreadsheetVariableRow(tokenNames, i, constants);
    }

    flushAndCloseWriter();
  }

  public void prepareWriter(String filePath) throws TemplateException {
    try {
      File file = new File(filePath);
      file.createNewFile();
      this.lineWriter = new PrintWriter(file);
    } catch (IOException e) {
      throw new TemplateException(e);
    }
  }

  public void writeSpreadsheetVariableRow(
      List<TemplateTokenInfo> tokenNames, int entryNumber, TemplateVariables entryVariables) {

    StringBuilder delimeterString = new StringBuilder();
    for (TemplateTokenInfo tokenInfo : tokenNames) {
      delimeterString.append(UPLOAD_DELIMITER);
      if (entryVariables.containsToken(tokenInfo.getName())
          && entryVariables.getTokenValue(tokenInfo.getName())
              != null) { // if there is a value, add it
        String value = entryVariables.getTokenValue(tokenInfo.getName());
        value = value.replaceAll("\n", "<br>");
        value = value.replaceAll(",", ";");
        delimeterString.append(value);
      }
    }

    String string = entryNumber + delimeterString.toString();
    this.lineWriter.println(string);
  }

  /**
   * writes the header for the example download spreadsheet
   *
   * @param variableTokenNames
   */
  public void writeDownloadSpreadsheetHeader(List<TemplateTokenInfo> variableTokenNames) {
    StringBuilder headerBuilder = new StringBuilder();
    headerBuilder.append(UPLOAD_COMMENTS);
    headerBuilder.append("\n");
    for (TemplateTokenInfo tokenInfo : variableTokenNames) {
      if (tokenInfo.getName().equals(TemplateProcessorConstants.COMMENTS_TOKEN)) {
        headerBuilder.append(
            "#The field '"
                + tokenInfo.getDisplayName()
                + "' allows return characters. To add return characters add '<br>'; e.g. line1<br>line2");
        headerBuilder.append("\n");
      }
    }
    for (TemplateTokenInfo variable : variableTokenNames) {
      headerBuilder.append(variable.getDisplayName());
      headerBuilder.append(UPLOAD_DELIMITER);
    }

    String headerLine = headerBuilder.toString();
    headerLine = headerLine.substring(0, headerLine.length() - 1); // get rid of trailing delimiter

    this.lineWriter.println(headerLine);
  }

  public void flushAndCloseWriter() {
    lineWriter.flush();
    lineWriter.close();
  }
}
