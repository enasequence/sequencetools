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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.reference.ElectronicReference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class EmblElectronicResourceWriterTest extends EmblWriterTest {

  public void testWrite_ElectronicResource() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    ElectronicReference electronicReference =
        referenceFactory.createElectronicReference(
            "The mouse heart sodium channel (mH1): cloning and characterization of alternatively spliced variants",
            "The American Journal of Physiology - Heart and Circulatory Physiology: DOI, 10.1152/ajpheart.00644.2001");
    StringWriter writer = new StringWriter();
    assertTrue(new RLWriter(entry, electronicReference, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "RL   (er) The American Journal of Physiology - Heart and Circulatory Physiology:\n"
            + "RL   DOI, 10.1152/ajpheart.00644.2001\n",
        writer.toString());
  }

  public void testWrite_EmptyElectronicResource() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    ElectronicReference electronicReference =
        referenceFactory.createElectronicReference(null, null);
    StringWriter writer = new StringWriter();
    assertTrue(new RLWriter(entry, electronicReference, wrapType).write(writer));
    // System.out.print(writer.toString());
    assertEquals("RL   (er) \n", writer.toString());
  }
}
