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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.genbank.AuthorsReader;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankLineReader;

public class AuthorsWriterTest extends GenbankWriterTest {

  public void testWrite_OneAuthors() throws IOException {
    entry.removeReferences();
    GenbankLineReader lineReader = new GenbankLineReader();
    lineReader.setReader(new BufferedReader(new StringReader("  AUTHORS   Antonellis,A.")));
    lineReader.readLine();
    Publication publication = lineReader.getCache().getPublication();
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new AuthorsReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    entry.addReference(reference);
    StringWriter writer = new StringWriter();
    assertTrue(new AuthorsWriter(entry, publication, wrapType).write(writer));
    assertEquals("  AUTHORS   Antonellis,A.\n", writer.toString());
  }

  public void testWrite_ManyAuthors() throws IOException {
    entry.removeReferences();
    GenbankLineReader lineReader = new GenbankLineReader();
    lineReader.setReader(
        new BufferedReader(
            new StringReader(
                "  AUTHORS   Antonellis,A., Ayele,K., Benjamin,B., Blakesley,R.W., Boakye,A., Bouffard,G.G., Brinkley,C., Young,A., Green,E.D.")));
    lineReader.readLine();
    Publication publication = lineReader.getCache().getPublication();
    Reference reference = lineReader.getCache().getReference();
    ValidationResult result = (new AuthorsReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    entry.addReference(reference);
    StringWriter writer = new StringWriter();
    assertTrue(new AuthorsWriter(entry, publication, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "  AUTHORS   Antonellis,A., Ayele,K., Benjamin,B., Blakesley,R.W., Boakye,A.,\n"
            + "            Bouffard,G.G., Brinkley,C., Young,A. and Green,E.D.\n",
        writer.toString());
  }

  public void testWrite_NoAuthors() throws IOException {
    entry.removeReferences();
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Publication publication = (new ReferenceFactory()).createPublication();
    Reference reference = referenceFactory.createReference(publication, 1);
    entry.addReference(reference);
    StringWriter writer = new StringWriter();
    assertTrue(new AuthorsWriter(entry, publication, wrapType).write(writer));
    assertEquals("  AUTHORS   \n", writer.toString());
  }
}
