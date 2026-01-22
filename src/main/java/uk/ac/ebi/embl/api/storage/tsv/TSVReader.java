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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.template.PolySample;
import uk.ac.ebi.embl.template.SequenceTax;

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
    try (InputStream closeableStream = stream;
        Reader reader = new InputStreamReader(closeableStream, StandardCharsets.UTF_8)) {
      return readTSV(reader);
    }
  }

  public DataSet readDataSetAsFile(String fileName) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      throw new IOException("Failed to load file for resource: " + fileName);
    }

    try (InputStream stream = openFileStream(file);
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      return readTSV(reader);
    }
  }

  private InputStream openFileStream(File file) throws IOException {
    InputStream fileStream = new FileInputStream(file);
    InputStream bufferedStream = new BufferedInputStream(fileStream);
    if (GzipUtils.isCompressedFilename(file.getName())) {
      return new GZIPInputStream(bufferedStream);
    }
    return bufferedStream;
  }

  private DataSet readTSV(Reader reader) throws IOException {
    String separatorRegex = normalizeSeparatorRegex(separator);
    String commentPrefix = comment == null ? "" : comment;

    DataSet result = new DataSet();

    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        if (line.trim().isEmpty()) {
          continue;
        }
        if (!commentPrefix.isEmpty() && line.startsWith(commentPrefix)) {
          continue;
        }
        result.addRow(new DataRow(line.split(separatorRegex, -1)));
      }
    }

    return result;
  }

  private String normalizeSeparatorRegex(String separator) {
    if (separator == null || separator.isEmpty()) {
      return "\t";
    }
    if ("\\t".equals(separator)) {
      return "\t";
    }
    return separator;
  }

  public List<PolySample> getPolySamples(File tsv) throws ValidationEngineException {
    try {
      DataSet dataSet = readDataSetAsFile(tsv.getAbsolutePath());

      if (dataSet == null || dataSet.getRows().size() <= 1) {
        return Collections.emptyList();
      }

      return dataSet.getRows().stream()
          .skip(1)
          .map(
              dataRow ->
                  new PolySample(
                      dataRow.getString(0),
                      dataRow.getString(1),
                      Long.parseLong(dataRow.getString(2))))
          .collect(Collectors.toList());

    } catch (NumberFormatException e) {
      throw new ValidationEngineException("Polysample Frequency must be a valid number");
    } catch (Exception e) {
      throw new ValidationEngineException(e);
    }
  }

  public Map<String, SequenceTax> getSequenceTax(File tsv) throws ValidationEngineException {
    try {
      DataSet dataSet = readDataSetAsFile(tsv.getAbsolutePath());

      if (dataSet == null || dataSet.getRows().size() <= 1) {
        return Collections.emptyMap();
      }

      Map<String, SequenceTax> result = new HashMap<>();

      for (DataRow dataRow : dataSet.getRows().subList(1, dataSet.getRows().size())) {
        String sequenceId = dataRow.getString(0);
        String taxId = dataRow.getString(1);

        if (result.containsKey(sequenceId)) {
          throw new ValidationEngineException("DuplicateDuplicate sequenceId found: " + sequenceId);
        }

        result.put(sequenceId, new SequenceTax(sequenceId, taxId));
      }

      return result;
    } catch (Exception e) {
      throw new ValidationEngineException(e);
    }
  }

  public DataSet getPolySampleDataSet(File tsv) throws ValidationEngineException {
    try {
      return readDataSetAsFile(tsv.getAbsolutePath());
    } catch (Exception e) {
      throw new ValidationEngineException(e);
    }
  }
}
