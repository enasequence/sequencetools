package uk.ac.ebi.embl.agp.reader;

import java.io.IOException;

public class AGPLineReaderTest extends AGPReaderTest{
	
	public void testGetCurrentTag_StandardTag() throws IOException {
		setLineReader(
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+"
			);
			assertTrue(lineReader.readLine());
			assertTrue(lineReader.isCurrentLine());
			assertFalse(lineReader.isNextLine());
			assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getCurrentTag());
			setLineReader("##agp-version	2.0");
			assertTrue(lineReader.readLine());
			assertTrue(lineReader.isCurrentLine());
			assertFalse(lineReader.isNextLine());
			assertEquals("##agp-version", lineReader.getCurrentTag());
	}
	
	public void testGetLines() throws IOException {
		setLineReader("##agp-version	2.0\n"+
				"# ORGANISM: Homo sapiens\n"+
				"IWGSC_CSS_6DL_scaff_3330716	1	330	1	W	IWGSC_CSS_6DL_contig_209591;	1	330	+\n"+
				"IWGSC_CSS_6DL_scaff_3330716	331	354	2	N	24	scaffold	yes	paired-ends\n"+
				"                     \n"+
				"IWGSC_CSS_6DL_scaff_3330716;	355	654	3	W	IWGSC_CSS_6DL_contig_209592	1	300	+\n"+
				"IWGSC_CSS_6DL_scaff_3330717	1	330	1	W	IWGSC_CSS_6DL_contig_209593	1	330	-");
		
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals(" 2.0", lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals(" ORGANISM: Homo sapiens", lineReader.getNextLine());
		assertEquals("#", lineReader.getNextTag());
		assertEquals("##agp-version", lineReader.getCurrentTag());
		assertTrue(lineReader.isNextTag());
		//next line
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals(" ORGANISM: Homo sapiens", lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals(" 1 330 1 W IWGSC_CSS_6DL_contig_209591; 1 330 +", lineReader.getNextLine());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getNextTag());
		assertEquals("#", lineReader.getActiveTag());
		assertEquals("#", lineReader.getCurrentTag());
		assertTrue(lineReader.isNextTag());
		//next line
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals(" 1 330 1 W IWGSC_CSS_6DL_contig_209591; 1 330 +", lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals(" 331 354 2 N 24 scaffold yes paired-ends", lineReader.getNextLine());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getNextTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getActiveTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getCurrentTag());
		assertTrue(lineReader.isNextTag());
		// next line
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals(" 331 354 2 N 24 scaffold yes paired-ends",lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals("; 355 654 3 W IWGSC_CSS_6DL_contig_209592 1 300 +",lineReader.getNextLine());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getNextTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getActiveTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getCurrentTag());
		assertTrue(lineReader.isNextTag());
		// next line
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals("; 355 654 3 W IWGSC_CSS_6DL_contig_209592 1 300 +",lineReader.getCurrentLine());
		assertTrue(lineReader.isNextLine());
		assertEquals(" 1 330 1 W IWGSC_CSS_6DL_contig_209593 1 330 -",lineReader.getNextLine());
		assertEquals("IWGSC_CSS_6DL_scaff_3330717", lineReader.getNextTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getActiveTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330716", lineReader.getCurrentTag());
		assertTrue(lineReader.isNextTag());
		
		// next line
		assertTrue(lineReader.readLine());
		assertTrue(lineReader.isCurrentLine());
		assertEquals(" 1 330 1 W IWGSC_CSS_6DL_contig_209593 1 330 -",lineReader.getCurrentLine());
		assertFalse(lineReader.isNextLine());
		assertEquals(null,lineReader.getNextLine());
		assertEquals(null, lineReader.getNextTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330717", lineReader.getActiveTag());
		assertEquals("IWGSC_CSS_6DL_scaff_3330717", lineReader.getCurrentTag());
		assertFalse(lineReader.isNextTag());
}
	
}
