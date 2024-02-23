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
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class RAReaderTest extends EmblReaderTest {

  public void testRead_Authors1() throws IOException {
    initLineReader("RA   Puzio P.A., von Blau A., Ebneth M., Cook.A;\n");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new RAReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(4, publication.getAuthors().size());
    assertEquals("Puzio", publication.getAuthors().get(0).getSurname());
    assertEquals("P.A.", publication.getAuthors().get(0).getFirstName());
    assertEquals("von Blau", publication.getAuthors().get(1).getSurname());
    assertEquals("A.", publication.getAuthors().get(1).getFirstName());
    assertEquals("Ebneth", publication.getAuthors().get(2).getSurname());
    assertEquals("M.", publication.getAuthors().get(2).getFirstName());
    assertNull(publication.getAuthors().get(3).getFirstName());
    assertEquals("Cook.A", publication.getAuthors().get(3).getSurname());
  }

  public void testRead_Authors2() throws IOException {
    initLineReader(
        "RA   Antonellis ;A.,; Ayele, Ben;jamin B.. C. D., Blakesley, Boakye A.,\n"
            + "RA   Bouffard G.G., Brinkley C.,, ,, , ,, ,, ; Brooks S., Chu G., Coleman H., Engle J.,\n");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new RAReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(11, publication.getAuthors().size());
    assertEquals("Antonellis", publication.getAuthors().get(0).getSurname());
    assertEquals("A.", publication.getAuthors().get(0).getFirstName());
    assertEquals("Ayele", publication.getAuthors().get(1).getSurname());
    assertNull(publication.getAuthors().get(1).getFirstName());
    assertEquals("Benjamin", publication.getAuthors().get(2).getSurname());
    assertEquals("B. C. D.", publication.getAuthors().get(2).getFirstName());
    assertEquals("Blakesley", publication.getAuthors().get(3).getSurname());
    assertNull(publication.getAuthors().get(3).getFirstName());
    assertEquals("Boakye", publication.getAuthors().get(4).getSurname());
    assertEquals("A.", publication.getAuthors().get(4).getFirstName());
    assertEquals("Bouffard", publication.getAuthors().get(5).getSurname());
    assertEquals("G.G.", publication.getAuthors().get(5).getFirstName());
    assertEquals("Brinkley", publication.getAuthors().get(6).getSurname());
    assertEquals("C.", publication.getAuthors().get(6).getFirstName());
    assertEquals("Brooks", publication.getAuthors().get(7).getSurname());
    assertEquals("S.", publication.getAuthors().get(7).getFirstName());
    assertEquals("Chu", publication.getAuthors().get(8).getSurname());
    assertEquals("G.", publication.getAuthors().get(8).getFirstName());
    assertEquals("Coleman", publication.getAuthors().get(9).getSurname());
    assertEquals("H.", publication.getAuthors().get(9).getFirstName());
    assertEquals("Engle", publication.getAuthors().get(10).getSurname());
    assertEquals("J.", publication.getAuthors().get(10).getFirstName());
  }

  /*	public void testRead_InvalidAuthors() throws IOException {
  	initLineReader(
     		"RA   A. B. C.\n"
  	);
  	Publication publication = lineReader.getCache().getPublication();
  	ValidationResult result = (new RAReader(lineReader)).read(entry);
  	assertEquals(1, result.count("RA.1", Severity.ERROR));
  	assertEquals(0, publication.getAuthors().size());
  }*/

  public void testRead_NoAuthors() throws IOException {
    initLineReader("RA\n");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new RAReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(0, publication.getAuthors().size());
  }
}
