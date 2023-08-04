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
package uk.ac.ebi.embl.flatfile.reader;

import java.io.IOException;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

/** Reader for flat file lines. */
public abstract class FlatFileLineReader {

  protected FlatFileLineReader(LineReader lineReader) {
    this.lineReader = lineReader;
  }

  protected LineReader lineReader;
  protected Entry entry;
  private ValidationResult validationResult;

  public final ValidationResult read(Entry entry) throws IOException {
    this.entry = entry;
    validationResult = new ValidationResult();
    readLines();
    return validationResult;
  }

  protected abstract void readLines() throws IOException;

  protected void error(String messageKey, Object... params) {
    validationResult.append(FlatFileValidations.error(lineReader, messageKey, params));
  }

  protected void warning(String messageKey, Object... params) {
    validationResult.append(FlatFileValidations.warning(lineReader, messageKey, params));
  }

  protected LineReader getLineReader() {
    return lineReader;
  }

  public LineReaderCache getCache() {
    return lineReader.getCache();
  }
}
