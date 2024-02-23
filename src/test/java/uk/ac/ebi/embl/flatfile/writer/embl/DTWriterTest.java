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
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;

public class DTWriterTest extends EmblWriterTest {

  public void testWrite_Date() throws IOException {
    entry.setFirstPublic(FlatFileDateUtils.getDay("06-SEP-2006"));
    entry.setLastUpdated(FlatFileDateUtils.getDay("05-SEP-2006"));
    entry.setFirstPublicRelease(1);
    entry.setLastUpdatedRelease(2);
    entry.setVersion(1);
    StringWriter writer = new StringWriter();
    assertTrue(new DTWriter(entry).write(writer));
    assertEquals(
        "DT   06-SEP-2006 (Rel. 1, Created)\n"
            + "DT   05-SEP-2006 (Rel. 2, Last updated, Version 1)\n",
        writer.toString());
  }

  public void testWrite_NoDate() throws IOException {
    entry.setFirstPublic(null);
    entry.setLastUpdated(null);
    entry.setFirstPublicRelease(null);
    entry.setLastUpdatedRelease(null);
    entry.setVersion(null);
    StringWriter writer = new StringWriter();
    assertFalse(new DTWriter(entry).write(writer));
    assertEquals("", writer.toString());
  }
}
