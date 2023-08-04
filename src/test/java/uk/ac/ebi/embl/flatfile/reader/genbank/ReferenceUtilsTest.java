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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;
import java.util.Collection;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class ReferenceUtilsTest extends GenbankReaderTest {

  public void testRead_ReferenceNumber() throws IOException {
    initLineReader("REFERENCE   4");
    ValidationResult result = (new ReferenceReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(lineReader.getCache().getReference());
    assertEquals(Integer.valueOf(4), lineReader.getCache().getReference().getReferenceNumber());
  }

  public void testRead_NoReferenceNumber() throws IOException {
    initLineReader("REFERENCE   ");
    ValidationResult result = (new ReferenceReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNull(lineReader.getCache().getReference().getReferenceNumber());
  }

  public void testRead_FormatError() throws IOException {
    initLineReader("REFERENCE   sdfsd");
    ValidationResult result = (new ReferenceReader(lineReader)).read(entry);
    assertEquals(1, result.count("FF.1", Severity.ERROR));
    assertNull(lineReader.getCache().getReference().getReferenceNumber());
  }

  public void testRead_Origin() throws IOException {
    initLineReader("REFERENCE   4");
    ValidationResult result = (new ReferenceReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(lineReader.getCache().getReference());
    FlatFileOrigin origin = (FlatFileOrigin) lineReader.getCache().getReference().getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(1, origin.getLastLineNumber());
  }

  public void testRead_Position() throws IOException {
    initLineReader("REFERENCE   4 (bases 1 to 34 ;\n" + "            ahaha ; 54 to 222)");
    ValidationResult result = (new ReferenceReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(lineReader.getCache().getReference());
    assertEquals(2, lineReader.getCache().getReference().getLocations().getLocations().size());
    LocalRange location1 =
        lineReader.getCache().getReference().getLocations().getLocations().get(0);
    LocalRange location2 =
        lineReader.getCache().getReference().getLocations().getLocations().get(1);
    assertEquals(new Long(1), location1.getBeginPosition());
    assertEquals(new Long(34), location1.getEndPosition());
    assertEquals(new Long(54), location2.getBeginPosition());
    assertEquals(new Long(222), location2.getEndPosition());
  }
}
