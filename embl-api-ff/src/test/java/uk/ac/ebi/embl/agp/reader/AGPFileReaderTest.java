package uk.ac.ebi.embl.agp.reader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.FlatFileEntryReader;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

public class AGPFileReaderTest extends AGPReaderTest{

	@Test
	public void testRead_Entry() throws IOException {
		String entryString =
				"##agp-version	2.0\n"+
						"# ORGANISM: Homo sapiens\n"+
						"# TAX_ID: 9606\n"+
						"# ASSEMBLY NAME: EG1\n"+
						"# ASSEMBLY DATE: 09-November-2011\n"+
						"# GENOME CENTER: NCBI\n"+
						"# DESCRIPTION: Example AGP specifying the assembly of scaffolds from WGS contigs\n"+
						"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
						"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		String expectedEntryString="ID   XXX; SV XXX; linear; XXX; XXX; XXX; 654 BP.\n"+
				"XX\n"+
				"AC   ;\n"+
				"XX\n"+
				"AC * _IWGSC_CSS_6DL_scaff_3330716\n"+
				"XX\n"+
				"DE   .\n"+
				"XX\n"+
				"KW   .\n"+
				"XX\n"+
				"//\n";

		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
		ValidationResult result = reader.read();
		Entry entry = reader.getEntry();
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for ( ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());			
		}		
		assertEquals(0, result.count(Severity.ERROR));
		StringWriter writer = new StringWriter();                      
		assertTrue(new EmblEntryWriter(entry).write(writer));
		//System.out.print(writer.toString());
		assertEquals(expectedEntryString, writer.toString());
	}
	@Test
	public void testRead_MultipleEntries() throws IOException {
		String entryString =
				"##agp-version	2.0\n"+
						"# ORGANISM: Homo sapiens\n"+
						"# TAX_ID: 9606\n"+
						"# ASSEMBLY NAME: EG1\n"+
						"# ASSEMBLY DATE: 09-November-2011\n"+
						"# GENOME CENTER: NCBI\n"+
						"# DESCRIPTION: Example AGP specifying the assembly of scaffolds from WGS contigs\n"+
						"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
						"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
						"                     \n"+
						"IWGSC_CSS_6DL_scaff_3330716;	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n"+
						"IWGSC_CSS_6DL_scaff_3330717	1	330	1	W	IWGSC_CSS_6DL_contig_209593	1	330	-\n";	
		String expectedEntryString =
				"ID   XXX; SV XXX; linear; XXX; XXX; XXX; 654 BP.\n"+
						"XX\n"+
						"AC   ;\n"+
						"XX\n"+
						"AC * _IWGSC_CSS_6DL_scaff_3330716\n"+
						"XX\n"+
						"DE   .\n"+
						"XX\n"+
						"KW   .\n"+
						"XX\n"+
						"//\n"+
						"ID   XXX; SV XXX; linear; XXX; XXX; XXX; 330 BP.\n"+
						"XX\n"+
						"AC   ;\n"+
						"XX\n"+
						"AC * _IWGSC_CSS_6DL_scaff_3330717\n"+
						"XX\n"+
						"DE   .\n"+
						"XX\n"+
						"KW   .\n"+
						"XX\n"+
						"//\n";

		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());			
    		}
    		assertEquals(0, result.count(Severity.ERROR));
            if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
		assertEquals(expectedEntryString, writer.toString());
	}	
	
	@Test
	public void testRead_wrongnumberofColumns() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes\n";	
		String expectedEntryString ="ID   XXX; SV XXX; linear; XXX; XXX; XXX; 330 BP.\n"+
				"XX\n"+
				"AC   ;\n"+
				"XX\n"+
				"AC * _IWGSC_CSS_6DL_scaff_3330716\n"+
				"XX\n"+
				"DE   .\n"+
				"XX\n"+
				"KW   .\n"+
				"XX\n"+
				"//\n";


		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("NumberOfColumnsCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
		assertEquals(expectedEntryString, writer.toString());
	}
	
	@Test
	public void testRead_validnumberofColumns() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	no\n"+
				"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		String expectedEntryString ="ID   XXX; SV XXX; linear; XXX; XXX; XXX; 654 BP.\n"+
  			"XX\n"+
				"AC   ;\n"+
				"XX\n"+
				"AC * _IWGSC_CSS_6DL_scaff_3330716\n"+
				"XX\n"+
				"DE   .\n"+
				"XX\n"+
				"KW   .\n"+
				"XX\n"+
				"//\n";


		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
       		assertEquals(0, result.count(Severity.ERROR));
            assertTrue(result.isValid());
            assertEquals(0, result.getMessages("NumberOfColumnsCheck").size());
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
		assertEquals(expectedEntryString, writer.toString());
	}

	@Test
	public void testRead_singletonError() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591	1	330	+\n"+
						"IWGSC_CSS_6DL_scaff_3330717	1	654	1	W	IWGSC_CSS_6DL_contig_209593	1	330	+\n";
		StringWriter writer = new StringWriter();
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
		while (true) {
			ValidationResult result = reader.read();
			Entry entry = reader.getEntry();
			Collection<ValidationMessage<Origin>> messages = result.getMessages();

			for ( ValidationMessage<Origin> message : messages) {
				assertEquals("SingletonsOnlyError", message.getMessageKey());
			}
			if (!reader.isEntry()) {
				break;
			}
			assertTrue(new EmblEntryWriter(entry).write(writer));
		}
	}

	@Test
	public void testRead_invalidobjectbeg() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	invalid	330	1	W	IWGSC_CSS_6DL_contig_209591	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("InvalidObjectBegCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
	
	@Test
	public void testRead_missingobject_name() throws IOException {
		String entryString =
				"	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209593	1	300	+\n"+
						"IWGSC_CSS_6DL_scaff_3330716	655	954	3	W	IWGSC_CSS_6DL_contig_209594	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("MissingObjectCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
	
	@Test
	public void testRead_invalidobjectend() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	invalid	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("InvalidObjectEndCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
	
	@Test
	public void testRead_invalidpartNumber() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	invalid	N	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("InvalidPartNumberCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
	
	@Test
	public void testRead_invalidcomponentTypeID() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	invalid	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("InvalidComponentTypeCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
	
	@Test
	public void testRead_invalidgapLength() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	invalid	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("InvalidGapLengthCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
	
	@Test
	public void testRead_invalidcomponentBegin() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	invalid	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("InvalidComponentBegCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
	
	@Test
	public void testRead_invalidcomponentEnd() throws IOException {
		String entryString =
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	invalid	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
						"IWGSC_CSS_6DL_scaff_3330716	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new AGPFileReader(new AGPLineReader(bufferedReader));
        while (true) {
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());
    			assertEquals("InvalidComponentEndCheck", message.getMessageKey());
        		assertEquals(1, result.count(Severity.ERROR));
    		}
    		  if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
	}
}
