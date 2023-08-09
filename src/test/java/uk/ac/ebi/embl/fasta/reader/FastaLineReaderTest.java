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
package uk.ac.ebi.embl.fasta.reader;

import java.io.IOException;

public class FastaLineReaderTest extends FastaReaderTest {

  public void testGetCurrentTag_StandardTag() throws IOException {
    setLineReader(">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA");
    assertTrue(lineReader.readLine());
    assertTrue(lineReader.isCurrentLine());
    assertFalse(lineReader.isNextLine());
    assertEquals(">", lineReader.getCurrentTag());
  }

  public void testGetLines() throws IOException {
    setLineReader(
        ">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"
            + "GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG\n"
            + "CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG\n"
            + ">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n");

    assertTrue(lineReader.readLine());
    assertTrue(lineReader.isCurrentLine());
    assertEquals(
        "ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA", lineReader.getCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertEquals(
        "GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG", lineReader.getNextLine());
    assertEquals("", lineReader.getNextTag());
    assertEquals(">", lineReader.getCurrentTag());
    assertFalse(lineReader.isNextTag());
    // next line
    assertTrue(lineReader.readLine());
    assertTrue(lineReader.isCurrentLine());
    assertEquals(
        "GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG",
        lineReader.getCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertEquals(
        "CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG", lineReader.getNextLine());
    assertEquals("", lineReader.getNextTag());
    assertEquals(">", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertFalse(lineReader.isNextTag());
    // next line
    assertTrue(lineReader.readLine());
    assertTrue(lineReader.isCurrentLine());
    assertEquals(
        "CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG",
        lineReader.getCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertEquals(
        "ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA", lineReader.getNextLine());
    assertEquals(">", lineReader.getNextTag());
    assertEquals(">", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertTrue(lineReader.isNextTag());
  }
}
