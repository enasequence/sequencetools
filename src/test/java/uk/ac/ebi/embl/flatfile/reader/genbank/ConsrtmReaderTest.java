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
import uk.ac.ebi.embl.flatfile.reader.embl.RGReader;

public class ConsrtmReaderTest extends GenbankReaderTest {

  public void testRead_Consortium() throws IOException {
    initLineReader("  CONSRTM   consortium  consortium\n" + "            consortium");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new RGReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals("consortium consortium consortium", publication.getConsortium());
  }

  public void testRead_NoConsortium() throws IOException {
    initLineReader("  CONSRTM   ");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new RGReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNull(publication.getConsortium());
  }
}
