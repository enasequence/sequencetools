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

public class PrimaryReaderTest extends GenbankReaderTest {

  public void testRead() throws IOException {
    initLineReader(
        "PRIMARY     TPA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n"
            + "            1-426          AC004528.1             18665-19090\n"
            + "            427-526        AC001234.2             1-100            c\n");
    ValidationResult result = (new PrimaryReader(lineReader)).read(entry);
    lineReader.readLine();
    ValidationResult result2 = (new PrimaryReader(lineReader)).read(entry);
    lineReader.readLine();
    ValidationResult result3 = (new PrimaryReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(0, result2.count(Severity.ERROR));
    assertEquals(0, result3.count(Severity.ERROR));
    assertEquals(2, entry.getAssemblies().size());
    assertEquals(
        Long.valueOf(1), entry.getAssemblies().get(0).getSecondarySpan().getBeginPosition());
    assertEquals(
        Long.valueOf(426), entry.getAssemblies().get(0).getSecondarySpan().getEndPosition());
    assertEquals("AC004528", entry.getAssemblies().get(0).getPrimarySpan().getAccession());
    assertEquals(Integer.valueOf(1), entry.getAssemblies().get(0).getPrimarySpan().getVersion());
    assertEquals(
        Long.valueOf(18665), entry.getAssemblies().get(0).getPrimarySpan().getBeginPosition());
    assertEquals(
        Long.valueOf(19090), entry.getAssemblies().get(0).getPrimarySpan().getEndPosition());
    assertFalse(entry.getAssemblies().get(0).getPrimarySpan().isComplement());
    assertEquals(
        Long.valueOf(427), entry.getAssemblies().get(1).getSecondarySpan().getBeginPosition());
    assertEquals(
        Long.valueOf(526), entry.getAssemblies().get(1).getSecondarySpan().getEndPosition());
    assertEquals("AC001234", entry.getAssemblies().get(1).getPrimarySpan().getAccession());
    assertEquals(Integer.valueOf(2), entry.getAssemblies().get(1).getPrimarySpan().getVersion());
    assertEquals(Long.valueOf(1), entry.getAssemblies().get(1).getPrimarySpan().getBeginPosition());
    assertEquals(Long.valueOf(100), entry.getAssemblies().get(1).getPrimarySpan().getEndPosition());
    assertTrue(entry.getAssemblies().get(1).getPrimarySpan().isComplement());
  }

  public void testRead_Origin() throws IOException {
    initLineReader(
        "PRIMARY     TPA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n"
            + "            1-426          AC004528.1             18665-19090\n"
            + "            427-526        AC001234.2             1-100            c\n");
    ValidationResult result = (new PrimaryReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(2, entry.getAssemblies().size());
    FlatFileOrigin origin1 = (FlatFileOrigin) entry.getAssemblies().get(0).getOrigin();
    FlatFileOrigin origin2 = (FlatFileOrigin) entry.getAssemblies().get(1).getOrigin();
    assertEquals(1, origin1.getFirstLineNumber());
    assertEquals(3, origin1.getLastLineNumber());
    assertEquals(1, origin2.getFirstLineNumber());
    assertEquals(3, origin2.getLastLineNumber());
  }
}
