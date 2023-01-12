package uk.ac.ebi.embl.flatfile.writer;

import org.junit.Test;
import uk.ac.ebi.embl.flatfile.EmblPadding;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.embl.flatfile.writer.FlatFileWriter.getDefaultOptimalLineLength;

public class FlatFileWriterTest {

    @Test
    public void writeBlockDefaultOptimalLineLengthWithSpacesWithForceBreak() throws IOException {
        StringWriter strWriter = new StringWriter();
        String comment =
                "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage " +
                        "Illumina HiSeqX data from a 10X Genomics Chromium library generated at the Wellcome Sanger Institute," +
                        " as well as ~71x coverage HiSeqX data from a Hi-C library prepared by Arima Genomics. " +
                        "An initial PacBio assembly was made using Falcon-unzip, and retained haplotigs were " +
                        "identified using purge_haplotigs. The primary contigs were then scaffolded using the " +
                        "10X data with scaff10x, then scaffolded further using the Hi-C data with SALSA2. " +
                        "Polishing and gap-filling of both the primary scaffolds and haplotigs was performed " +
                        "using the PacBio reads and Arrow, followed by two rounds of Illumina polishing using " +
                        "the 10X data and freebayes. Finally, the assembly was manually improved using gEVAL to " +
                        "correct mis-joins and improve concordance with the raw data. Chromosomes are named according " +
                        "to synteny with the GCA_003309015.1 assembly of Sparus aurata." ;

        FlatFileWriter.writeBlock(strWriter, "", "", comment, WrapChar.WRAP_CHAR_SPACE,
                EmblPadding.CC_PADDING.length(), true, getDefaultOptimalLineLength(WrapType.EMBL_WRAP));

        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        String expectedComment = "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage\n" +
                "Illumina HiSeqX data from a 10X Genomics Chromium library generated at the\n" +
                "Wellcome Sanger Institute, as well as ~71x coverage HiSeqX data from a Hi-C\n" +
                "library prepared by Arima Genomics. An initial PacBio assembly was made\n" +
                "using Falcon-unzip, and retained haplotigs were identified using\n" +
                "purge_haplotigs. The primary contigs were then scaffolded using the 10X\n" +
                "data with scaff10x, then scaffolded further using the Hi-C data with\n" +
                "SALSA2. Polishing and gap-filling of both the primary scaffolds and\n" +
                "haplotigs was performed using the PacBio reads and Arrow, followed by two\n" +
                "rounds of Illumina polishing using the 10X data and freebayes. Finally, the\n" +
                "assembly was manually improved using gEVAL to correct mis-joins and improve\n" +
                "concordance with the raw data. Chromosomes are named according to synteny\n" +
                "with the GCA_003309015.1 assembly of Sparus aurata.";
        assertEquals(expectedComment, output);
    }

    @Test
    public void writeBlockDefaultCustomLineLengthWithSpacesWithForceBreak() throws IOException {
        StringWriter strWriter = new StringWriter();
        String comment =
                "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage " +
                        "Illumina HiSeqX data from a 10X Genomics Chromium library generated at the Wellcome Sanger Institute," +
                        " as well as ~71x coverage HiSeqX data from a Hi-C library prepared by Arima Genomics. " +
                        "An initial PacBio assembly was made using Falcon-unzip, and retained haplotigs were " +
                        "identified using purge_haplotigs. The primary contigs were then scaffolded using the " +
                        "10X data with scaff10x, then scaffolded further using the Hi-C data with SALSA2. " +
                        "Polishing and gap-filling of both the primary scaffolds and haplotigs was performed " +
                        "using the PacBio reads and Arrow, followed by two rounds of Illumina polishing using " +
                        "the 10X data and freebayes. Finally, the assembly was manually improved using gEVAL to " +
                        "correct mis-joins and improve concordance with the raw data. Chromosomes are named according " +
                        "to synteny with the GCA_003309015.1 assembly of Sparus aurata." ;

        FlatFileWriter.writeBlock(strWriter, "", "", comment, WrapChar.WRAP_CHAR_SPACE,
                EmblPadding.CC_PADDING.length(), true, 200); // optimalLineLength is 200

        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        String expectedComment = "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage Illumina HiSeqX data from a 10X Genomics Chromium library generated at the Wellcome Sanger Institute, as well as ~71x\n" +
                "coverage HiSeqX data from a Hi-C library prepared by Arima Genomics. An initial PacBio assembly was made using Falcon-unzip, and retained haplotigs were identified using purge_haplotigs. The\n" +
                "primary contigs were then scaffolded using the 10X data with scaff10x, then scaffolded further using the Hi-C data with SALSA2. Polishing and gap-filling of both the primary scaffolds and\n" +
                "haplotigs was performed using the PacBio reads and Arrow, followed by two rounds of Illumina polishing using the 10X data and freebayes. Finally, the assembly was manually improved using gEVAL to\n" +
                "correct mis-joins and improve concordance with the raw data. Chromosomes are named according to synteny with the GCA_003309015.1 assembly of Sparus aurata.";
        assertEquals(expectedComment, output);
    }

    @Test
    public void writeBlockSingleLine() throws IOException {
        StringWriter strWriter = new StringWriter();
        String comment =
                "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage ";

        FlatFileWriter.writeBlock(strWriter, "", "", comment, WrapChar.WRAP_CHAR_SPACE,
                EmblPadding.CC_PADDING.length(), false, getDefaultOptimalLineLength(WrapType.EMBL_WRAP));

        String output = strWriter.toString();
        if (output.endsWith("\n")) {
            assertEquals(output.length() - 1, output.lastIndexOf("\n"));
            output = output.substring(0, output.length() - 1);
        }
        String expectedComment = "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage ";
        assertEquals(expectedComment, output);

    }

    @Test
    public void writeBlockDefaultOptimalLineLengthWithoutSpacesWithForceBreak() throws IOException {
        StringWriter strWriter = new StringWriter();
        //no space or break
        String comment = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        strWriter = new StringWriter();
        FlatFileWriter.writeBlock(strWriter, "", "", comment, WrapChar.WRAP_CHAR_SPACE,
                EmblPadding.CC_PADDING.length(), true, getDefaultOptimalLineLength(WrapType.EMBL_WRAP));
        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "a", output);

    }

    @Test
    public void writeBlockCustomOptimalLineLengthWithoutSpacesWithForceBreak() throws IOException {
        StringWriter strWriter = new StringWriter();
        //no space or break
        String comment = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        strWriter = new StringWriter();
        FlatFileWriter.writeBlock(strWriter, "", "", comment, WrapChar.WRAP_CHAR_SPACE,
                EmblPadding.CC_PADDING.length(), true, 200); // optimalLineLength is 200
        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", output);

    }

    @Test
    public void writeBlockDefaultOptimalLineLengthWithoutSpacesWithoutForceBreak() throws IOException {
        //no space or break
        String comment = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        StringWriter strWriter = new StringWriter();
        FlatFileWriter.writeBlock(strWriter, "", "", comment, WrapChar.WRAP_CHAR_SPACE,
                EmblPadding.CC_PADDING.length(), false, getDefaultOptimalLineLength(WrapType.EMBL_WRAP));
        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }

        //NOTE: no \n at teh end
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", output);

    }
    @Test
    public void writeBlockEmptyString() throws IOException {
        // null not allowed, empty string
        String comment = "";
        StringWriter strWriter = new StringWriter();
        FlatFileWriter.writeBlock(strWriter, "", "", comment, WrapChar.WRAP_CHAR_SPACE,
                EmblPadding.CC_PADDING.length(), true, getDefaultOptimalLineLength(WrapType.EMBL_WRAP));
        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        assertEquals("", output);
    }
}