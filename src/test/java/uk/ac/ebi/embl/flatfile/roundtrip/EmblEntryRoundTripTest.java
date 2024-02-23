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
package uk.ac.ebi.embl.flatfile.roundtrip;

import java.io.*;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

/**
 * Created by IntelliJ IDEA. User: Lawrence Date: 12-Jan-2009 Time: 11:23:15 To change this template
 * use File | Settings | File Templates.
 */
public class EmblEntryRoundTripTest extends TestCase {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testFile1() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("/test_files/testfile1.txt");
    String entryString = readFileAsString(stream);

    EmblEntryReader entryReader =
        new EmblEntryReader(new BufferedReader(new StringReader(entryString)));
    entryReader.read();
    Entry entry = entryReader.getEntry();
    EntryWriter flatFileWriter = new EmblEntryWriter(entry);
    StringWriter outString = new StringWriter();
    flatFileWriter.write(outString);

    String outStringResult = outString.toString();

    entryString = entryString.replaceAll("\r\n", "\n");

    assertEquals(entryString, outStringResult);
  }

  private static String readFileAsString(InputStream stream) throws java.io.IOException {
    byte[] buffer = new byte[(int) stream.available()];
    stream.read(buffer);
    return new String(buffer);
  }
}
