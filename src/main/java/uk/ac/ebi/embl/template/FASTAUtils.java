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
import java.util.List;

public class FASTAUtils {
  public boolean isFASTAFileSequin(String filePath) throws IOException, TemplateException {
    BufferedReader reader = null;
    try {
      File inputFile = new File(filePath);
      reader = new BufferedReader(new FileReader(inputFile));
      String currentLine = reader.readLine();
      if (currentLine == null) throw new TemplateException("FASTA file is empty");
      while (currentLine != null) {
        if (currentLine.isEmpty()) {
          currentLine = reader.readLine();
          continue;
        }
        if (currentLine.startsWith(FastaSpreadsheetConverter.FASTA_HEADER_TOKEN))
          return currentLine.contains("=") && currentLine.contains("[");
        else currentLine = reader.readLine();
      }
    } finally {
      if (reader != null) reader.close();
    }
    return false;
  }

  public void writeTemplateSequinFASTAHeaderExampleFile(
      List<TemplateTokenInfo> tokenNames, String filePath) throws IOException {
    File file = new File(filePath);
    file.createNewFile();
    PrintWriter headerWriter = new PrintWriter(file);
    StringBuilder headerString = new StringBuilder();
    headerString.append(">this is not used ");
    if (!tokenNames.isEmpty()) {
      for (TemplateTokenInfo variable : tokenNames) writeTokenToHeader(headerString, variable);
      headerString.append("\n");
      headerString.append("aaaaaaaaaaaaaaaaaaaaaaaaa");
      headerWriter.write(headerString.toString());
    }
    headerWriter.flush();
    headerWriter.close();
  }

  private void writeTokenToHeader(StringBuilder headerString, TemplateTokenInfo tokenInfo) {
    if (tokenInfo.getName().equals(TemplateProcessorConstants.SEQUENCE_TOKEN)) return;
    headerString.append("[");
    headerString.append(tokenInfo.getDisplayName());
    headerString.append("=");
    headerString.append("value_here");
    headerString.append("] ");
  }
}
