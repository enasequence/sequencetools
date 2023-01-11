package uk.ac.ebi.embl.flatfile.writer;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.writer.embl.CCWriter;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class FlatFileWriterTest {

    @Test
    public void writeBlock() throws IOException {
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

        Entry entry = new Entry();
        entry.setComment(new Text(comment));
        CCWriter ccWriter = new CCWriter(entry, WrapType.EMBL_WRAP);
        ccWriter.write(strWriter);

        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        String expectedComment = "CC   The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage\n" +
                "CC   Illumina HiSeqX data from a 10X Genomics Chromium library generated at the\n" +
                "CC   Wellcome Sanger Institute, as well as ~71x coverage HiSeqX data from a Hi-C\n" +
                "CC   library prepared by Arima Genomics. An initial PacBio assembly was made\n" +
                "CC   using Falcon-unzip, and retained haplotigs were identified using\n" +
                "CC   purge_haplotigs. The primary contigs were then scaffolded using the 10X\n" +
                "CC   data with scaff10x, then scaffolded further using the Hi-C data with\n" +
                "CC   SALSA2. Polishing and gap-filling of both the primary scaffolds and\n" +
                "CC   haplotigs was performed using the PacBio reads and Arrow, followed by two\n" +
                "CC   rounds of Illumina polishing using the 10X data and freebayes. Finally, the\n" +
                "CC   assembly was manually improved using gEVAL to correct mis-joins and improve\n" +
                "CC   concordance with the raw data. Chromosomes are named according to synteny with the GCA_003309015.1 assembly of Sparus aurata.";
        assertEquals(expectedComment, output);
    }

    @Test
    public void writeBlockNoSpaceOrBreak() throws IOException {
        StringWriter strWriter = new StringWriter();
        String   comment = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                +"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";


        Entry entry = new Entry();
        entry.setComment(new Text(comment));
        CCWriter ccWriter = new CCWriter(entry, WrapType.EMBL_WRAP);
        ccWriter.write(strWriter);
        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        assertEquals("CC   aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "CC   aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", output);
    }

    @Test
    public void writeCCSingleLine() throws IOException {
        StringWriter strWriter = new StringWriter();
        String comment =
                "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage " ;

        Entry entry = new Entry();
        entry.setComment(new Text(comment));
        CCWriter ccWriter = new CCWriter(entry, WrapType.EMBL_WRAP);
        ccWriter.write(strWriter);

        String output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        String expectedComment = "CC   The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage ";
        assertEquals(expectedComment, output);

        //null not allowed, empty string
        comment = "";
        strWriter = new StringWriter();
        entry = new Entry();
        entry.setComment(new Text(comment));
        ccWriter = new CCWriter(entry, WrapType.EMBL_WRAP);
        ccWriter.write(strWriter);
        output = strWriter.toString();
        if(output.endsWith("\n")) {
            assertEquals(output.length()-1, output.lastIndexOf("\n"));
            output = output.substring(0,output.length()-1);
        }
        assertEquals("", output);
    }


}