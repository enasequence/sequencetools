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
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;

public class CCReaderTest extends EmblReaderTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    EntryReader.getBlockCounter().put("CC", 1);
  }

  public void testRead_ShortComment() throws IOException {
    initLineReader("CC     comment comment comment ");
    ValidationResult result = (new CCReader(lineReader)).read(entry);
    assertEquals("  comment comment comment", entry.getComment().getText());
    assertEquals(0, result.count(Severity.ERROR));

    FlatFileOrigin origin = (FlatFileOrigin) entry.getComment().getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(1, origin.getLastLineNumber());
  }

  public void testRead_LongComment() throws IOException {
    initLineReader(
        "CC     comment comment comment \n"
            + "CC             comment comment \n"
            + "CC     comment comment\n"
            + "CC             comment comment comment comment comment comment comment comment comment comment comment comment \n");
    ValidationResult result = (new CCReader(lineReader)).read(entry);
    assertEquals(
        "  comment comment comment\n"
            + "          comment comment\n"
            + "  comment comment\n"
            + "          comment comment comment comment comment comment comment comment comment comment comment comment",
        entry.getComment().getText());
    assertEquals(0, result.count(Severity.ERROR));

    FlatFileOrigin origin = (FlatFileOrigin) entry.getComment().getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(4, origin.getLastLineNumber());
  }

  public void testRead_TwoComments() throws IOException {
    initLineReader("CC     comment comment comment \n" + "CC             comment comment \n");
    ValidationResult result = (new CCReader(lineReader)).read(entry);
    initLineReader(
        "CC     comment comment\n"
            + "CC             comment comment comment comment comment comment comment comment comment comment comment comment \n");
    ValidationResult result2 = (new CCReader(lineReader)).read(entry);
    assertEquals(
        "  comment comment comment\n"
            + "          comment comment\n"
            + "\n"
            + "  comment comment\n"
            + "          comment comment comment comment comment comment comment comment comment comment comment comment",
        entry.getComment().getText());
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(0, result2.count(Severity.ERROR));
  }

  public void testRead_EmptyLine() throws IOException {
    initLineReader("CC");
    ValidationResult result = (new CCReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNull(entry.getComment().getText());
    initLineReader("CC   ");
    result = (new CCReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNull(entry.getComment().getText());
    initLineReader("CC         ");
    result = (new CCReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNull(entry.getComment().getText());
  }
}
