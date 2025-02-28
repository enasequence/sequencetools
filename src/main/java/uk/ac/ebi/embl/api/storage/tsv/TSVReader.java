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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.template.PolySample;

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

    InputStreamReader streamReader;
    if (GzipUtils.isCompressedFilename(file.getName())) {
      FileInputStream submittedDataFis = new FileInputStream(file);
      BufferedInputStream bufferedInputStrem =
          new BufferedInputStream(new GZIPInputStream(submittedDataFis));
      streamReader = new InputStreamReader(bufferedInputStrem);
    } else {
      streamReader = new FileReader(file);
    }

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

  public List<PolySample> getPolySamples(File tsv) throws ValidationEngineException {
    try {
      TSVReader reader = new TSVReader();
      DataSet dataSet = reader.readDataSetAsFile(tsv.getAbsolutePath());

      if (dataSet == null || dataSet.getRows().size() <= 1) {
        return Collections.emptyList();
      }

      // Skip the header row and collect rest.
      return dataSet.getRows().stream()
              .skip(1)
              .map(
                      dataRow ->
                              new PolySample(
                                      dataRow.getString(0),
                                      dataRow.getString(1),
                                      Long.parseLong(dataRow.getString(2))))
              .collect(Collectors.toList());

    } catch (NumberFormatException nfe) {
      throw new ValidationEngineException("Polysample Frequency must be a valid number");
    } catch (Exception e) {
      throw new ValidationEngineException(e);
    }
  }

  public DataSet getPolySampleDataSet(File tsv) throws ValidationEngineException {
    try {
      TSVReader reader = new TSVReader("\\t", "#");
      return reader.readDataSetAsFile(tsv.getAbsolutePath());

    } catch (NumberFormatException nfe) {
      throw new ValidationEngineException("Frequency must be a valid number");
    } catch (Exception e) {
      throw new ValidationEngineException(e);
    }
  }
}
