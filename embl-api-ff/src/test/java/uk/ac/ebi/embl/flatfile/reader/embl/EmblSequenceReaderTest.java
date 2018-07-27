/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.IOException;

import uk.ac.ebi.embl.flatfile.reader.ReaderOptions;
import uk.ac.ebi.embl.flatfile.reader.SequenceReader;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblReaderTest;

public class EmblSequenceReaderTest extends EmblReaderTest {

	public void testRead_Sequence() throws IOException {
		initLineReader(
			    "     gutttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			    "     cggtgtggga TACctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			    "     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			    "     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			    "     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
                "     tctccgtgaa tgtctatcat tcctacacag gaccc                                  335\n"
		);
		ValidationResult result = (new SequenceReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals( 
			    "gttttgtttgatggagaattgcgcagaggggttatatctgcgtgaggatctgtcactcgg" +
			    "cggtgtgggatacctccctgctaaggcgggttgagtgatgttccctcggactggggaccg" +
			    "ctggcttgcgagctatgtccgctactctcagtactacactctcatttgagcccccgctca" +
			    "gtttgctagcagaacccggcacatggttcgccgataccatggaatttcgaaagaaacact" +
			    "ctgttaggtggtatgagtcatgacgcacgcagggagaggctaaggcttatgctatgctga" +
                "tctccgtgaatgtctatcattcctacacaggaccc",				
				new String(entry.getSequence().getSequenceByte()));
		assertEquals(335, 
				entry.getSequence().getLength());
	}

    public void testSkipSequence() throws IOException {
        initLineReader(
                "     gutttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
                        "     cggtgtggga TACctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
                        "     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
                        "     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
                        "     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
                        "     tctccgtgaa tgtctatcat tcctacacag gaccc                                  335\n"
        );
        ReaderOptions rO = new ReaderOptions();
        rO.setIgnoreSequence(true);
        ValidationResult result = (new SequenceReader(lineReader.setReaderOptions(rO))).read(entry);
        assertEquals(0, result.count(Severity.ERROR));
        assertNull(entry.getSequence().getSequenceByte());

		result = (new SequenceReader(lineReader.setReaderOptions(new ReaderOptions()))).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getSequence().getSequenceByte());
    }

	public void testRead_Origin() throws IOException {
		initLineReader(
		    "     gutttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
		    "     cggtgtggga TACctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
		    "     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
		    "     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
		    "     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
	        "     tctccgtgaa tgtctatcat tcctacacag gaccc                                  335\n"
	    );
	    ValidationResult result = (new SequenceReader(lineReader)).read(entry);
	    assertEquals(0, result.count(Severity.ERROR));
		FlatFileOrigin origin = (FlatFileOrigin)entry.getSequence().getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(6, origin.getLastLineNumber());
	}
}
