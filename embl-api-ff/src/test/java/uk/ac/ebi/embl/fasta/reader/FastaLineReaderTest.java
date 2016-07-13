package uk.ac.ebi.embl.fasta.reader;

import java.io.IOException;

public class FastaLineReaderTest extends FastaReaderTest {

	public void testGetCurrentTag_StandardTag() throws IOException {
		setLineReader(
			">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA"
		);
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertFalse(lineReader.isNextLine());
		assertEquals(">", lineReader.getCurrentTag());
	}

	public void testGetLines() throws IOException {
		setLineReader(">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"
				+ "GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG\n"
				+ "CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG\n"
				+">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n");
		
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals("ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA", lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals("GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG", lineReader.getNextLine());
		assertEquals("", lineReader.getNextTag());
		assertEquals(">", lineReader.getCurrentTag());
		assertFalse(lineReader.isNextTag());
		//next line
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals("GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG", lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals("CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG", lineReader.getNextLine());
		assertEquals("", lineReader.getNextTag());
		assertEquals(">", lineReader.getActiveTag());
		assertEquals("", lineReader.getCurrentTag());
		assertFalse(lineReader.isNextTag());
		//next line
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals("CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG", lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals("ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA", lineReader.getNextLine());
		assertEquals(">", lineReader.getNextTag());
		assertEquals(">", lineReader.getActiveTag());
		assertEquals("", lineReader.getCurrentTag());
		assertTrue(lineReader.isNextTag());

	}
}
