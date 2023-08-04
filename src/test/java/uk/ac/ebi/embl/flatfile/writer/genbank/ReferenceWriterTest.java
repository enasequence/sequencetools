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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class ReferenceWriterTest extends GenbankWriterTest {

  public void testWrite_ReferenceNumber() throws IOException {
    Reference reference = (new ReferenceFactory()).createReference();
    reference.setReferenceNumber(5);
    StringWriter writer = new StringWriter();
    assertTrue(new ReferenceWriter(entry, reference, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("REFERENCE   5\n", writer.toString());
  }

  public void testWrite_NoReferenceNumber() throws IOException {
    Reference reference = (new ReferenceFactory()).createReference();
    StringWriter writer = new StringWriter();
    assertTrue(new ReferenceWriter(entry, reference, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("", writer.toString());
  }

  public void testWrite_OneLocation() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Reference reference = referenceFactory.createReference();
    reference.setReferenceNumber(5);
    LocationFactory locationFactory = new LocationFactory();
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    StringWriter writer = new StringWriter();
    assertTrue(new ReferenceWriter(entry, reference, wrapType).write(writer));
    assertEquals("REFERENCE   5  (bases 1 to 262417)\n", writer.toString());
  }

  public void testWrite_TwoLocations() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Reference reference = referenceFactory.createReference();
    reference.setReferenceNumber(5);
    LocationFactory locationFactory = new LocationFactory();
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    StringWriter writer = new StringWriter();
    assertTrue(new ReferenceWriter(entry, reference, wrapType).write(writer));
    assertEquals("REFERENCE   5  (bases 1 to 262417; 1 to 262417)\n", writer.toString());
  }

  public void testWrite_ManyLocations() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Reference reference = referenceFactory.createReference();
    reference.setReferenceNumber(5);
    LocationFactory locationFactory = new LocationFactory();
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    reference.getLocations().addLocation(locationFactory.createLocalRange(1L, 262417L));
    StringWriter writer = new StringWriter();
    assertTrue(new ReferenceWriter(entry, reference, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "REFERENCE   5  (bases 1 to 262417; 1 to 262417; 1 to 262417; 1 to 262417;\n"
            + "            1 to 262417; 1 to 262417; 1 to 262417; 1 to 262417; 1 to 262417;\n"
            + "            1 to 262417)\n",
        writer.toString());
  }
}
