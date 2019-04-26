package uk.ac.ebi.embl.agp.writer;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Test;

import uk.ac.ebi.embl.agp.reader.AGPFileReader;
import uk.ac.ebi.embl.agp.reader.AGPLineReader;
import uk.ac.ebi.embl.api.entry.Entry;

public class AGPFileWriterTest
{
	@Test
   public void testwrite_entry() throws IOException
   {
        InputStream stream = this.getClass().getResourceAsStream("/test_files/agpfile.txt");
        String entryString = readFileAsString(stream);

        AGPFileReader fileReader = new AGPFileReader(new AGPLineReader(new BufferedReader(new StringReader(entryString))));
        fileReader.read();
        StringWriter writer = new StringWriter();
        while(fileReader.isEntry())
        {
        Entry entry = fileReader.getEntry();
        AGPFileWriter fileWriter = new AGPFileWriter(entry,writer);
        fileWriter.write();
        writer.write("\n");
        fileReader.read();
        }
       assertEquals(writer.toString().trim().replace("\t", "").replace("\n",""), entryString.trim().replace("\t", "").replace("\n", "").replace("\r", ""));
   }
	private static String readFileAsString(InputStream stream) throws java.io.IOException {
        byte[] buffer = new byte[(int) stream.available()];
        stream.read(buffer);
        return new String(buffer);
    }
}
