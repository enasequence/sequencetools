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

import java.io.*;

public class FTUploadCSVReader {
  private String currentLine;
  private final BufferedReader lineReader;

  public FTUploadCSVReader(InputStream inputReader) throws IOException {
    lineReader = new BufferedReader(new InputStreamReader(inputReader));
    currentLine = lineReader.readLine(); // prime with the first line
  }

  public CSVLine readTemplateSpreadsheetLine() throws IOException {

    CSVLine result = null;
    StringBuilder currentEntryBuilder = new StringBuilder();

    while (currentLine != null) {

      if (currentLine.isEmpty()) {
        currentLine = lineReader.readLine();
        continue; // skip empty lines
      }

      if (currentLine.contains(
          TemplateProcessorConstants.DELIMITER4)) { // we have reached the end of an entry
        String trailingString =
            currentLine.substring(0, currentLine.indexOf(TemplateProcessorConstants.DELIMITER4));
        currentEntryBuilder.append(trailingString);
        /** remove the <d3> from the front of the string */
        String currentEntryString =
            currentEntryBuilder.substring(
                currentEntryBuilder.indexOf(TemplateProcessorConstants.DELIMITER3)
                    + TemplateProcessorConstants.DELIMITER3.length());
        TemplateVariables map = TokenMapConverter.stringToTemplateVariables(currentEntryString);
        result = new CSVLine(1, map);
        currentLine =
            currentLine.substring(
                currentLine.indexOf(TemplateProcessorConstants.DELIMITER4)
                    + TemplateProcessorConstants.DELIMITER4.length());
        return result;
      } else {
        currentEntryBuilder.append(currentLine);
        /**
         * we want to keep newline characters, these are stripped by the line reader so put them
         * back in.
         */
        currentEntryBuilder.append("\n");
      }

      currentLine = lineReader.readLine();
    }

    return result;
  }

  public static void main(String[] args) {
    try {
      File input = new File("C:\\tmp\\entry_upload.txt");
      FileInputStream stream = new FileInputStream(input);
      FTUploadCSVReader reader = new FTUploadCSVReader(stream);
      CSVLine result = reader.readTemplateSpreadsheetLine();
      while (result != null) {
        result = reader.readTemplateSpreadsheetLine();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace(); // To change body of catch statement use File | Settings | File
      // Templates.
    }
  }
}
