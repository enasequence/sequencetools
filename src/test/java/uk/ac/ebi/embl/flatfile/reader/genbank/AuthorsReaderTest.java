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
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class AuthorsReaderTest extends GenbankReaderTest {

  public void testRead_Authors() throws IOException {
    initLineReader(
        "  AUTHORS   Raza H, Okazaki,Y., Furuno,M., Birney,E., Hayashizaki Y. and St. John,P.L.\n");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new AuthorsReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(6, publication.getAuthors().size());
    // Unable to separate initials
    assertEquals("Raza H", publication.getAuthors().get(0).getSurname());
    assertNull(publication.getAuthors().get(0).getFirstName());
    assertEquals("Okazaki", publication.getAuthors().get(1).getSurname());
    assertEquals("Y.", publication.getAuthors().get(1).getFirstName());
    assertEquals("Furuno", publication.getAuthors().get(2).getSurname());
    assertEquals("M.", publication.getAuthors().get(2).getFirstName());
    assertEquals("Birney", publication.getAuthors().get(3).getSurname());
    assertEquals("E.", publication.getAuthors().get(3).getFirstName());
    assertEquals("Hayashizaki", publication.getAuthors().get(4).getSurname());
    assertEquals("Y.", publication.getAuthors().get(4).getFirstName());
    // Unable to separate initials
    assertEquals("St. John P.L.", publication.getAuthors().get(5).getSurname());
    assertNull(publication.getAuthors().get(5).getFirstName());
  }

  public void testRead_NoAuthors() throws IOException {
    initLineReader("AUTHORS\n");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new AuthorsReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(0, publication.getAuthors().size());
  }
}
