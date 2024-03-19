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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.Text;

public class DEWriterTest extends EmblWriterTest {

  public void testWrite_Description() throws IOException {
    entry.setDescription(
        new Text(
            "Felis \tcatus target 36 genomic scaffold "
                + "Felis \ncatus target 36 genomic scaffold "
                + "Felis \rcatus target 36 genomic scaffold "
                + "Felis catus target 36 genomic scaffold "
                + "Felis catus target 36 genomic scaffold "
                + "Felis catus target 36 genomic scaffold "
                + "Felis catus target 36 genomic scaffold "
                + "Felis catus target 36 genomic scaffold "
                + "Felis catus target 36 genomic scaffold"));
    StringWriter writer = new StringWriter();
    assertTrue(new DEWriter(entry, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "DE   Felis  catus target 36 genomic scaffold Felis  catus target 36 genomic\n"
            + "DE   scaffold Felis  catus target 36 genomic scaffold Felis catus target 36\n"
            + "DE   genomic scaffold Felis catus target 36 genomic scaffold Felis catus target\n"
            + "DE   36 genomic scaffold Felis catus target 36 genomic scaffold Felis catus\n"
            + "DE   target 36 genomic scaffold Felis catus target 36 genomic scaffold\n",
        writer.toString());
  }

  public void testWrite_NoDescription() throws IOException {
    entry.setDescription(null);
    StringWriter writer = new StringWriter();
    assertTrue(new DEWriter(entry, wrapType).write(writer));
    assertEquals("DE   .\n", writer.toString());
  }

  public void testWrite_Long_Description() throws IOException {
    entry.setDescription(
        new Text(
            "Styela plicata mitochondrion genomic DNA containing cox2-trnT-trnD-nad1-nad4l-trnG_1-cob_1-trnM_1-trnL1_1-cox1_2-cob_2a-trnL1_2-cob_2b-trnC-trnA-trnS2-trnM_2-trnL1_2-cox1_1-nad6-trnR-nad2-trnL2-nad5-nad3-trnN-trnW-trnV-trnY-cox3-trnI-trnS1-trnF-trnM_3-trnH-trnK-atp6-atp8-trnP-nad4-rrnS-rrnL-trnQ-trnG_2-trnE\n"
                + "region"));
    StringWriter writer = new StringWriter();
    assertTrue(new DEWriter(entry, wrapType).write(writer));
    assertEquals(
        "DE   Styela plicata mitochondrion genomic DNA containing\n"
            + "DE   cox2-trnT-trnD-nad1-nad4l-trnG_1-cob_1-trnM_1-trnL1_1-cox1_2-cob_2a-trnL1_2\n"
            + "DE   -cob_2b-trnC-trnA-trnS2-trnM_2-trnL1_2-cox1_1-nad6-trnR-nad2-trnL2-nad5-nad\n"
            + "DE   3-trnN-trnW-trnV-trnY-cox3-trnI-trnS1-trnF-trnM_3-trnH-trnK-atp6-atp8-trnP-\n"
            + "DE   nad4-rrnS-rrnL-trnQ-trnG_2-trnE region\n",
        writer.toString());
  }
}
