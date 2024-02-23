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

public class TitleReaderTest extends GenbankReaderTest {

  public void testRead_Title() throws IOException {
    initLineReader(
        "  TITLE     Cloning and sequence of REV7, a gene whose function is required for\n"
            + "            DNA damage-induced mutagenesis in Saccharomyces cerevisiae");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new TitleReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    // System.out.print(publication.getTitle());
    assertEquals(
        "Cloning and sequence of REV7, a gene whose function is required for DNA damage-induced mutagenesis in Saccharomyces cerevisiae",
        publication.getTitle());
  }

  public void testRead_NoTitle() throws IOException {
    initLineReader("TITLE   \"   \n");
    Publication publication = lineReader.getCache().getPublication();
    ValidationResult result = (new TitleReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    // System.out.print(publication.getTitle());
    assertNull(publication.getTitle());
  }
}
