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
package uk.ac.ebi.embl.agp.writer;

import static org.junit.Assert.assertEquals;

import java.io.*;
import org.junit.Test;
import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.entry.Entry;

public class AGPFileWriterTest {
  @Test
  public void testwrite_entry() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("/test_files/agpfile.txt");
    String entryString = readFileAsString(stream);

    AGPFileReader fileReader =
        new AGPFileReader(new AGPLineReader(new BufferedReader(new StringReader(entryString))));
    fileReader.read();
    StringWriter writer = new StringWriter();
    while (fileReader.isEntry()) {
      Entry entry = fileReader.getEntry();
      AGPFileWriter fileWriter = new AGPFileWriter(entry, writer);
      fileWriter.write();
      writer.write("\n");
      fileReader.read();
    }
    assertEquals(
        writer.toString().trim().replace("\t", "").replace("\n", ""),
        entryString.trim().replace("\t", "").replace("\n", "").replace("\r", ""));
  }

  private static String readFileAsString(InputStream stream) throws java.io.IOException {
    byte[] buffer = new byte[(int) stream.available()];
    stream.read(buffer);
    return new String(buffer);
  }
}
