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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class AccessionReaderTest extends GenbankReaderTest {

  public void testRead_PrimaryAccession() throws IOException {
    initLineReader("ACCESSION   A00001");
    ValidationResult result = (new AccessionReader(lineReader)).read(entry);
    assertEquals("A00001", entry.getPrimaryAccession());
    assertEquals(0, result.count(Severity.ERROR));
  }

  public void testRead_NoPrimaryAccession() throws IOException {
    initLineReader("ACCESSION");
    ValidationResult result = (new AccessionReader(lineReader)).read(entry);
    assertNull(entry.getPrimaryAccession());
    assertEquals(0, result.count(Severity.ERROR));
  }

  public void testRead_SecondaryAccessionWithPrimaryAccession() throws IOException {
    initLineReader(
        "ACCESSION   A00001 A00002\n" + "            A00003 A00004\n" + "            A00005\n");
    ValidationResult result = (new AccessionReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals("A00001", entry.getPrimaryAccession());

    assertEquals(4, entry.getSecondaryAccessions().size());
    assertEquals("A00002", entry.getSecondaryAccessions().get(0).getText());
    FlatFileOrigin origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(0).getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(3, origin.getLastLineNumber());

    assertEquals("A00003", entry.getSecondaryAccessions().get(1).getText());
    origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(1).getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(3, origin.getLastLineNumber());

    assertEquals("A00004", entry.getSecondaryAccessions().get(2).getText());
    origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(2).getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(3, origin.getLastLineNumber());

    assertEquals("A00005", entry.getSecondaryAccessions().get(3).getText());
    origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(3).getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(3, origin.getLastLineNumber());
  }

  public void testRead_XXX() throws IOException {
    initLineReader("ACCESSION   XXX A00002");
    ValidationResult result = (new AccessionReader(lineReader)).read(entry);
    assertNull(entry.getPrimaryAccession());
    assertEquals(1, entry.getSecondaryAccessions().size());
    assertEquals("A00002", entry.getSecondaryAccessions().get(0).getText());
    FlatFileOrigin origin = (FlatFileOrigin) entry.getSecondaryAccessions().get(0).getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(1, origin.getLastLineNumber());
    assertEquals(0, result.count(Severity.ERROR));
  }
}
