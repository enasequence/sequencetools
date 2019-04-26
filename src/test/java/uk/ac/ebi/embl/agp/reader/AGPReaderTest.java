package uk.ac.ebi.embl.agp.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

public abstract class AGPReaderTest extends TestCase {

	protected Entry entry;
	protected LineReader lineReader;
	protected BufferedReader bufferedReader;

	protected void setUp() throws Exception {
		super.setUp();
        ValidationMessageManager.addBundle(FlatFileValidations.AGP_FLAT_FILE_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		Sequence sequence = (new SequenceFactory()).createSequence();
		entry.setSequence(sequence);
	}

	protected void setLineReader(String string) throws IOException {
		lineReader = new AGPLineReader(new BufferedReader(new StringReader(string)));
	}

	protected void setBufferedReader(String string) throws IOException {
		bufferedReader = new BufferedReader(new StringReader(string));
	}
		
	protected void initLineReader(String string) throws IOException {
		lineReader = new AGPLineReader(new BufferedReader(new StringReader(string)));
		lineReader.readLine();
	}
}
