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
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class DblinkReaderTest extends GenbankReaderTest {

  @Test
  public void testRead_XrefWithSingleline() throws IOException {
    initLineReader("DBLINK      BioProject: PRJNA28847\n");
    ValidationResult result = (new DblinkReader(lineReader)).read(entry);
    lineReader.readLine();
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(0, entry.getXRefs().size());
    assertEquals("PRJNA28847", entry.getProjectAccessions().get(0).getText());
  }

  @Test
  public void testRead_XrefWithMultipleLines() throws IOException {
    initLineReader(
        "DBLINK      BioProject: PRJNA28847\n" + "            BioSample: SAMN02436234\n");
    ValidationResult result = (new DblinkReader(lineReader)).read(entry);
    lineReader.readLine();
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(1, entry.getXRefs().size());
    assertEquals("PRJNA28847", entry.getProjectAccessions().get(0).getText());
    assertEquals("BioSample", entry.getXRefs().get(0).getDatabase());
    assertEquals("SAMN02436234", entry.getXRefs().get(0).getPrimaryAccession());
  }

  @Test
  public void testRead_XrefNoPrimaryAccession() throws IOException {
    initLineReader("DBLINK      BioProject: \n");
    ValidationResult result = (new DblinkReader(lineReader)).read(entry);
    assertEquals(1, result.count("FF.1", Severity.ERROR));
    assertEquals(0, entry.getXRefs().size());
  }

  @Test
  public void testMultipleBioSampleAndSeqReadArchive() throws IOException {
    initLineReader(
        "BioProject: PRJNA272557\n"
            + "            BioSample: SAMN03225534, SAMN03225535, SAMN03225536, SAMN03225537,\n"
            + "            SAMN03225538, SAMN03225539, SAMN03225540, SAMN03283743,\n"
            + "            SAMN03283744, SAMN03283745, SAMN03283746, SAMN03283747\n"
            + "            Sequence Read Archive: SRR1665102, SRR1665103, SRR1665104,\n"
            + "            SRR1665105, SRR1665106, SRR1665107, SRR1665108, SRR1759781,\n"
            + "            SRR1759785, SRR1759786, SRR1759787, SRR1759788");
    ValidationResult result = (new DblinkReader(lineReader)).read(entry);
    assertEquals(0, result.count("FF.1", Severity.ERROR));
    assertEquals(24, entry.getXRefs().size());
    assertEquals("PRJNA272557", entry.getProjectAccessions().get(0).getText());
  }

  @Test
  public void testInvalidMultipleBioSampleAndSeqReadArchive() throws IOException {
    initLineReader(
        "BioProject: PRJNA272557\n"
            + "            BioSample: SAMN03225534, SAMN03225535, SAMN03225536, SAMN03225537,\n"
            + "            SAMN03225538, SAMN03225539, SAMN03225540, SAMN03283743,\n"
            + "            SAMN03283744, SAMN03283745, SAMN03283746, SAMN03283747," // invalid as
        // line endswith
        // comma
        );
    ValidationResult result = (new DblinkReader(lineReader)).read(entry);
    assertEquals(1, result.count("FF.1", Severity.ERROR));
    assertEquals(0, entry.getXRefs().size());
  }

  @Test
  public void testInvalidBioSampleAndSeqReadArchive() throws IOException {
    initLineReader(
        "BioProject: PRJNA272557\n" + "            BioSample: ," // invalid as line endswith comma
        );
    ValidationResult result = (new DblinkReader(lineReader)).read(entry);
    assertEquals(1, result.count("FF.1", Severity.ERROR));
    assertEquals("PRJNA272557", entry.getProjectAccessions().get(0).getText());
    assertEquals(0, entry.getXRefs().size());
  }
}
