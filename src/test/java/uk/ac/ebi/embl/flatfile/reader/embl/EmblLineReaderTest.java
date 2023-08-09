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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class EmblLineReaderTest extends EmblReaderTest {

  private void setLineReader(String str) {
    EmblLineReader emblLineReader = new EmblLineReader(new BufferedReader(new StringReader(str)));
    // Allow EmblLineReader to be tested without EntryReader. EntryReader initialises valid tags for
    // EmblReader and without this EmblLineReader can't parse the tag from the line.
    emblLineReader.setIsValidTag(tag -> !tag.equals("XX"));
    emblLineReader.setIsSkipTag(tag -> tag.equals("XX"));
    lineReader = emblLineReader;
  }

  public void testGetCurrentTag_StandardTag() throws IOException {
    setLineReader("ID   A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.");
    assertTrue(lineReader.readLine());
    assertTrue(lineReader.isCurrentLine());
    assertFalse(lineReader.isNextLine());
    assertEquals("ID", lineReader.getCurrentTag());
  }

  public void testGetCurrentTag_InternalTag() throws IOException {
    setLineReader("ST * A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.");
    assertTrue(lineReader.readLine());
    assertTrue(lineReader.isCurrentLine());
    assertFalse(lineReader.isNextLine());
    assertEquals("ST *", lineReader.getCurrentTag());
  }

  public void testGetNextTag_Standard() throws IOException {
    setLineReader("ID   A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.\n" + "KW");
    assertTrue(lineReader.readLine());
    assertEquals("KW", lineReader.getNextTag());
  }

  public void testGetNextTag_Internal() throws IOException {
    setLineReader("ID   A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.\n" + "ST * ");
    assertTrue(lineReader.readLine());
    assertEquals("ST *", lineReader.getNextTag());
  }

  public void testIsNextTag_NoTag() throws IOException {
    setLineReader("ID   A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.");
    assertTrue(lineReader.readLine());
    assertEquals("", lineReader.getNextTag());
  }

  public void testGetCurrentLine() throws IOException {
    setLineReader("ID    A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.   \n");
    assertTrue(lineReader.readLine());
    assertEquals(
        " A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.", lineReader.getCurrentLine());
  }

  public void testGetCurrentTrimmedLine() throws IOException {
    setLineReader("ID    A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.   \n");
    assertTrue(lineReader.readLine());
    assertEquals(
        "A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.",
        lineReader.getCurrentTrimmedLine());
  }

  public void testGetCurrentShrinkedLine() throws IOException {
    setLineReader("ID    A00001; SV 1;     linear; unassigned DNA; PaT; vrl; 335 BP.   \n");
    assertTrue(lineReader.readLine());
    assertEquals(
        "A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.",
        lineReader.getCurrentShrinkedLine());
  }

  public void testGetCurrentMaskedLine() throws IOException {
    setLineReader("ID    A00001; SV 1;     linear; unassigned DNA; PaT; vrl; 335 BP.   \n");
    assertTrue(lineReader.readLine());
    assertEquals(
        "      A00001; SV 1;     linear; unassigned DNA; PaT; vrl; 335 BP.   ",
        lineReader.getCurrentMaskedLine());
  }

  public void testGetCurrentRawLine() throws IOException {
    setLineReader("ID    A00001; SV 1;     linear; unassigned DNA; PaT; vrl; 335 BP.   \n");
    assertTrue(lineReader.readLine());
    assertEquals(
        "ID    A00001; SV 1;     linear; unassigned DNA; PaT; vrl; 335 BP.   ",
        lineReader.getCurrentRawLine());
  }

  public void testGetLineNumber() throws IOException {
    setLineReader(
        "ID   A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.   \n"
            + "ID   A00001; SV 2; linear; unassigned DNA; PaT; vrl; 335 BP.   \n"
            + "ID   A00001; SV 3; linear; unassigned DNA; PaT; vrl; 335 BP.");
    assertTrue(lineReader.readLine());
    assertEquals(1, lineReader.getCurrentLineNumber());
    assertEquals(
        "A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP.", lineReader.getCurrentLine());
    assertTrue(lineReader.readLine());
    assertEquals(2, lineReader.getCurrentLineNumber());
    assertEquals(
        "A00001; SV 2; linear; unassigned DNA; PaT; vrl; 335 BP.", lineReader.getCurrentLine());
    assertTrue(lineReader.readLine());
    assertEquals(3, lineReader.getCurrentLineNumber());
    assertEquals(
        "A00001; SV 3; linear; unassigned DNA; PaT; vrl; 335 BP.", lineReader.getCurrentLine());
  }

  public void testGet() throws IOException {
    setLineReader(
        "FT                   /note=\"satellite DNA\"\n"
            + "XX\n"
            + "CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n"
            + "XX\n"
            + "SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n"
            + "     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n"
            + "     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n"
            + "     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n"
            + "     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n"
            + "     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n"
            + "     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n"
            + "//\n");
    assertFalse(lineReader.isCurrentLine());
    assertFalse(lineReader.isNextLine());
    lineReader.readLine();
    assertFalse(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertTrue(lineReader.isCurrentTag());
    assertTrue(lineReader.isNextTag());
    assertEquals("FT", lineReader.getActiveTag());
    assertEquals("FT", lineReader.getCurrentTag());
    assertEquals("CO", lineReader.getNextTag());
    lineReader.readLine();
    assertFalse(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertTrue(lineReader.isCurrentTag());
    assertTrue(lineReader.isNextTag());
    assertEquals("CO", lineReader.getActiveTag());
    assertEquals("CO", lineReader.getCurrentTag());
    assertEquals("SQ", lineReader.getNextTag());
    lineReader.readLine();
    assertTrue(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertTrue(lineReader.isCurrentTag());
    assertFalse(lineReader.isNextTag());
    assertEquals("SQ", lineReader.getActiveTag());
    assertEquals("SQ", lineReader.getCurrentTag());
    assertEquals("", lineReader.getNextTag());
    lineReader.readLine();
    assertTrue(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertFalse(lineReader.isCurrentTag());
    assertFalse(lineReader.isNextTag());
    assertEquals("SQ", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertEquals("", lineReader.getNextTag());
    lineReader.readLine();
    assertTrue(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertFalse(lineReader.isCurrentTag());
    assertFalse(lineReader.isNextTag());
    assertEquals("SQ", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertEquals("", lineReader.getNextTag());
    lineReader.readLine();
    assertTrue(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertFalse(lineReader.isCurrentTag());
    assertFalse(lineReader.isNextTag());
    assertEquals("SQ", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertEquals("", lineReader.getNextTag());
    lineReader.readLine();
    assertTrue(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertFalse(lineReader.isCurrentTag());
    assertFalse(lineReader.isNextTag());
    assertEquals("SQ", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertEquals("", lineReader.getNextTag());
    lineReader.readLine();
    assertTrue(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertFalse(lineReader.isCurrentTag());
    assertFalse(lineReader.isNextTag());
    assertEquals("SQ", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertEquals("", lineReader.getNextTag());
    lineReader.readLine();
    assertFalse(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertTrue(lineReader.isNextLine());
    assertTrue(lineReader.isActiveTag());
    assertFalse(lineReader.isCurrentTag());
    assertTrue(lineReader.isNextTag());
    assertEquals("SQ", lineReader.getActiveTag());
    assertEquals("", lineReader.getCurrentTag());
    assertEquals("//", lineReader.getNextTag());
    lineReader.readLine();
    assertFalse(lineReader.joinLine());
    assertTrue(lineReader.isCurrentLine());
    assertFalse(lineReader.isNextLine());
  }
}
