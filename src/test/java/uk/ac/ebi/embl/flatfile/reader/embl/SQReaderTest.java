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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.IOException;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class SQReaderTest extends EmblReaderTest {

  public void testRead() throws IOException {
    initLineReader("SQ   test test test");
    ValidationResult result = (new SQReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
  }

  public void testRead_Empty() throws IOException {
    initLineReader("SQ   ");
    ValidationResult result = (new SQReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
  }

  public void testRead_Coverage() throws IOException {
    initLineReader("SQ   Sequence 315242 BP; 87432 A; 72431 C; 71123 G; 84256 T; 0 other;");
    ValidationResult result = (new SQReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(entry.getSequenceCoverage().get("BP").longValue(), 315242l);
    assertEquals(entry.getSequenceCoverage().get("A").longValue(), 87432l);
    assertEquals(entry.getSequenceCoverage().get("C").longValue(), 72431l);
    assertEquals(entry.getSequenceCoverage().get("G").longValue(), 71123l);
    assertEquals(entry.getSequenceCoverage().get("T").longValue(), 84256l);
    assertEquals(entry.getSequenceCoverage().get("other").longValue(), 0l);
  }
}
