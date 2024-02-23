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

public class DRReaderTest extends EmblReaderTest {

  public void testRead_XrefWithSecondaryAccession() throws IOException {
    initLineReader(
        "DR    database ; primary accession ; secondary  accession .\n"
            + "DR    database2 ; primary accession2 ; secondary  accession2 .");
    ValidationResult result = (new DRReader(lineReader)).read(entry);
    lineReader.readLine();
    ValidationResult result2 = (new DRReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(0, result2.count(Severity.ERROR));
    assertEquals(2, entry.getXRefs().size());
    assertEquals("database", entry.getXRefs().get(0).getDatabase());
    assertEquals("primary accession", entry.getXRefs().get(0).getPrimaryAccession());
    assertEquals("secondary accession", entry.getXRefs().get(0).getSecondaryAccession());
    assertEquals("database2", entry.getXRefs().get(1).getDatabase());
    assertEquals("primary accession2", entry.getXRefs().get(1).getPrimaryAccession());
    assertEquals("secondary accession2", entry.getXRefs().get(1).getSecondaryAccession());
  }

  public void testRead_XrefWithoutSecondaryAccession() throws IOException {
    initLineReader("DR    database ; primary accession ");
    ValidationResult result = (new DRReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(1, entry.getXRefs().size());
    assertEquals("database", entry.getXRefs().get(0).getDatabase());
    assertEquals("primary accession", entry.getXRefs().get(0).getPrimaryAccession());
    assertNull(entry.getXRefs().get(0).getSecondaryAccession());
  }

  public void testRead_XrefNoDatabase() throws IOException {
    initLineReader("DR    ; primary accession ; secondary  accession .");
    ValidationResult result = (new DRReader(lineReader)).read(entry);
    assertEquals(1, result.count("FF.1", Severity.ERROR));
    assertEquals(0, entry.getXRefs().size());
  }

  public void testRead_XrefNoPrimaryAccession() throws IOException {
    initLineReader("DR    database ;  ; secondary  accession .");
    ValidationResult result = (new DRReader(lineReader)).read(entry);
    assertEquals(1, result.count("FF.1", Severity.ERROR));
    assertEquals(0, entry.getXRefs().size());
  }

  public void testRead_Origin() throws IOException {
    initLineReader(
        "\n"
            + "DR    database ; primary accession ; secondary  accession .\n"
            + "\n"
            + "DR    database2 ; primary accession2 ; secondary  accession2 .");
    ValidationResult result = (new DRReader(lineReader)).read(entry);
    lineReader.readLine();
    ValidationResult result2 = (new DRReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(0, result2.count(Severity.ERROR));
    assertEquals(2, entry.getXRefs().size());
    FlatFileOrigin origin1 = (FlatFileOrigin) entry.getXRefs().get(0).getOrigin();
    FlatFileOrigin origin2 = (FlatFileOrigin) entry.getXRefs().get(1).getOrigin();
    assertEquals(2, origin1.getFirstLineNumber());
    assertEquals(2, origin1.getLastLineNumber());
    assertEquals(4, origin2.getFirstLineNumber());
    assertEquals(4, origin2.getLastLineNumber());
  }
}
