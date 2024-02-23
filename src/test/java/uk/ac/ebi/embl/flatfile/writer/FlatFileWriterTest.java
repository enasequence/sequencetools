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
package uk.ac.ebi.embl.flatfile.writer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import uk.ac.ebi.embl.flatfile.EmblPadding;

public class FlatFileWriterTest {

  @Test
  public void writeBlock() throws IOException {
    StringWriter strWriter = new StringWriter();
    String comment =
        "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage "
            + "Illumina HiSeqX data from a 10X Genomics Chromium library generated at the Wellcome Sanger Institute,"
            + " as well as ~71x coverage HiSeqX data from a Hi-C library prepared by Arima Genomics. "
            + "An initial PacBio assembly was made using Falcon-unzip, and retained haplotigs were "
            + "identified using purge_haplotigs. The primary contigs were then scaffolded using the "
            + "10X data with scaff10x, then scaffolded further using the Hi-C data with SALSA2. "
            + "Polishing and gap-filling of both the primary scaffolds and haplotigs was performed "
            + "using the PacBio reads and Arrow, followed by two rounds of Illumina polishing using "
            + "the 10X data and freebayes. Finally, the assembly was manually improved using gEVAL to "
            + "correct mis-joins and improve concordance with the raw data. Chromosomes are named according "
            + "to synteny with the GCA_003309015.1 assembly of Sparus aurata.";

    FlatFileWriter.writeBlock(
        strWriter,
        "",
        "",
        comment,
        WrapChar.WRAP_CHAR_SPACE,
        WrapType.EMBL_WRAP,
        EmblPadding.CC_PADDING.length(),
        true,
        null,
        null);

    String output = strWriter.toString();
    if (output.endsWith("\n")) {
      assertEquals(output.length() - 1, output.lastIndexOf("\n"));
      output = output.substring(0, output.length() - 1);
    }
    String expectedComment =
        "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage\n"
            + "Illumina HiSeqX data from a 10X Genomics Chromium library generated at the\n"
            + "Wellcome Sanger Institute, as well as ~71x coverage HiSeqX data from a Hi-C\n"
            + "library prepared by Arima Genomics. An initial PacBio assembly was made\n"
            + "using Falcon-unzip, and retained haplotigs were identified using\n"
            + "purge_haplotigs. The primary contigs were then scaffolded using the 10X\n"
            + "data with scaff10x, then scaffolded further using the Hi-C data with\n"
            + "SALSA2. Polishing and gap-filling of both the primary scaffolds and\n"
            + "haplotigs was performed using the PacBio reads and Arrow, followed by two\n"
            + "rounds of Illumina polishing using the 10X data and freebayes. Finally, the\n"
            + "assembly was manually improved using gEVAL to correct mis-joins and improve\n"
            + "concordance with the raw data. Chromosomes are named according to synteny\n"
            + "with the GCA_003309015.1 assembly of Sparus aurata.";
    assertEquals(expectedComment, output);
  }

  @Test
  public void writeSingleLineBreakAtDefaultOptimalLength() throws IOException {
    StringWriter strWriter = new StringWriter();
    String comment = "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage ";

    FlatFileWriter.writeBlock(
        strWriter,
        "",
        "",
        comment,
        WrapChar.WRAP_CHAR_SPACE,
        WrapType.EMBL_WRAP,
        EmblPadding.CC_PADDING.length(),
        true,
        null,
        null);

    String output = strWriter.toString();
    if (output.endsWith("\n")) {
      assertEquals(output.length() - 1, output.lastIndexOf("\n"));
      output = output.substring(0, output.length() - 1);
    }
    String expectedComment =
        "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage ";
    assertEquals(expectedComment, output);

    // no space or break
    comment =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    strWriter = new StringWriter();
    FlatFileWriter.writeBlock(
        strWriter,
        "",
        "",
        comment,
        WrapChar.WRAP_CHAR_SPACE,
        WrapType.EMBL_WRAP,
        EmblPadding.CC_PADDING.length(),
        true,
        null,
        null);
    output = strWriter.toString();
    if (output.endsWith("\n")) {
      assertEquals(output.length() - 1, output.lastIndexOf("\n"));
      output = output.substring(0, output.length() - 1);
    }
    assertEquals(
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
            + "a",
        output);
  }

  @Test
  public void writeSingleLine() throws IOException {
    StringWriter strWriter = new StringWriter();
    String comment = "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage ";

    FlatFileWriter.writeBlock(
        strWriter,
        "",
        "",
        comment,
        WrapChar.WRAP_CHAR_SPACE,
        WrapType.EMBL_WRAP,
        EmblPadding.CC_PADDING.length(),
        true,
        200,
        null);

    String output = strWriter.toString();
    if (output.endsWith("\n")) {
      assertEquals(output.length() - 1, output.lastIndexOf("\n"));
      output = output.substring(0, output.length() - 1);
    }
  }

  @Test
  public void writeSingleLineBreakAtCustomOptimalLength() throws IOException {
    StringWriter strWriter = new StringWriter();
    // no space or break
    String comment =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    strWriter = new StringWriter();
    FlatFileWriter.writeBlock(
        strWriter,
        "",
        "",
        comment,
        WrapChar.WRAP_CHAR_SPACE,
        WrapType.EMBL_WRAP,
        EmblPadding.CC_PADDING.length(),
        true,
        200,
        null);
    String output = strWriter.toString();
    if (output.endsWith("\n")) {
      assertEquals(output.length() - 1, output.lastIndexOf("\n"));
      output = output.substring(0, output.length() - 1);
    }
    assertEquals(
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        output);
  }

  @Test
  public void writeEmptyLine() throws IOException {
    StringWriter strWriter = new StringWriter();
    // null not allowed, empty string
    String comment = "";
    strWriter = new StringWriter();
    FlatFileWriter.writeBlock(
        strWriter,
        "",
        "",
        comment,
        WrapChar.WRAP_CHAR_SPACE,
        WrapType.EMBL_WRAP,
        EmblPadding.CC_PADDING.length(),
        true,
        null,
        null);
    String output = strWriter.toString();
    if (output.endsWith("\n")) {
      assertEquals(output.length() - 1, output.lastIndexOf("\n"));
      output = output.substring(0, output.length() - 1);
    }
    assertEquals("", output);
  }
}
