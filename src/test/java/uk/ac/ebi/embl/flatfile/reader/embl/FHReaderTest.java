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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.IOException;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class FHReaderTest extends EmblReaderTest {

  public void testRead_Empty() throws IOException {
    initLineReader("FH   ");
    ValidationResult result = (new FHReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
  }

  public void testRead_NotEmpty() throws IOException {
    initLineReader("FH   everything here is simply ignored");
    ValidationResult result = (new FHReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
  }
}
