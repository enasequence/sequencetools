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
package uk.ac.ebi.embl.api.storage.tsv;

import java.io.*;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;

/**
 * A file with rows with tab (\t) separated values. Comment lines starting with a hash (#) and empty
 * lines will be ignored.
 */
public class TSVReader {

  public String separator = "\t";
  public String comment = "#";
  public static final String EMPTY_COL = "(null)";

  public TSVReader() {}

  public TSVReader(String separator, String comment) {
    this.separator = separator;
    this.comment = comment;
  }

  public DataSet readDataSetAsStream(String fileName) throws IOException {
    InputStream stream = this.getClass().getResourceAsStream(fileName);

    if (stream == null) {
      throw new IOException("Failed to load stream for resource: " + fileName);
    }

    return readDataSetAsStream(stream);
  }

  public DataSet readDataSetAsStream(InputStream stream) throws IOException {

    if (stream == null) {
      throw new IOException("stream is null");
    }

    InputStreamReader streamReader = new InputStreamReader(stream);
    return readTSV(streamReader);
  }

  public DataSet readDataSetAsFile(String fileName) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      throw new IOException("Failed to load file for resource: " + fileName);
    }

    InputStreamReader streamReader = new FileReader(file);
    return readTSV(streamReader);
  }

  private DataSet readTSV(InputStreamReader streamReader) throws IOException {
    BufferedReader reader = new BufferedReader(streamReader);
    DataSet result = new DataSet();
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      if (line.trim().length() == 0) {
        continue; // ignore empty lines
      }
      if (line.startsWith(comment)) {
        continue; // ignore comment lines
      }
      result.addRow(new DataRow(line.split(separator)));
    }
    // Need to close the stream as eventually load stream for resource will
    // fail
    reader.close();
    return result;
  }
}
