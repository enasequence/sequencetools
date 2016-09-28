package uk.ac.ebi.embl.fasta.reader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.FlatFileEntryReader;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

public class FastaFileReaderTest extends FastaReaderTest{

	public void testRead_Entry() throws IOException {
		String entryString =
			">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"+ 
            "GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG\n"+
            "CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG\n"+
            "CTGGCTTGCGAGCTATGTCCGCTACTCTCAGTACTACACTCTCATTTGAGCCCCCGCTCA\n"+
            "GTTTGCTAGCAGAACCCGGCACATGGTTCGCCGATACCATGGAATTTCGAAAGAAACACT\n"+
            "CTGTTAGGTGGTATGAGTCATGACGCACGCAGGGAGAGGCTAAGGCTTATGCTATGCTGA\n"+
            "TCTCCGTGAATGTCTATCATTCCTACACAGGACCC\n";
		String expectedEntryString="ID   XXX; SV XXX; linear; XXX; XXX; XXX; 335 BP.\n"+
				"XX\n"+
				"AC   ;\n"+
				"XX\n"+
				"AC * _ENA\n"+
				"XX\n"+
				"DE   .\n"+
				"XX\n"+
				"KW   .\n"+
				"XX\n"+
				"CC   ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"+
                "XX\n"+
				"SQ   Sequence 335 BP; 69 A; 82 C; 95 G; 89 T; 0 other;\n"+
				"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n"+
				"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n"+
				"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n"+
				"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n"+
				"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n"+
				"     tctccgtgaa tgtctatcat tcctacacag gaccc                                  335\n"+
				"//\n";
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new FastaFileReader(new FastaLineReader(bufferedReader));
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
	
	public void testRead_MultipleEntries() throws IOException {
		String entryString =
				">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"+
						"GTTTTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG\n"+
						"CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG\n"+
						"CTGGCTTGCGAGCTATGTCCGCTACTCTCAGTACTACACTCTCATTTGAGCCCCCGCTCA\n"+
						"GTTTGCTAGCAGAACCCGGCACATGGTTCGCCGATACCATGGAATTTCGAAAGAAACACT\n"+
						"CTGTTAGGTGGTATGAGTCATGACGCACGCAGGGAGAGGCTAAGGCTTATGCTATGCTGA\n"+
						"TCTCCGTGAATGTCTATCATTCCTACACAGGACCC\n"+
						">ENA|A00002|A00002.1 B.taurus DNA sequence 1 from patent application EP0238993\n"+
						"AATTCATGCGTCCGGACTTCTGCCTCGAGCCGCCGTACACTGGGCCCTGCAAAGCTCGTA\n"+
						"TCATCCGTTACTTCTACAATGCAAAGGCAGGCCTGTGTCAGACCTTCGTATACGGCGGTT\n"+
						"GCCGTGCTAAGCGTAACAACTTCAAATCCGCGGAAGACTGCGAACGTACTTGCGGTGGTC\n"+
						"CTTAGTAAAGCTTG\n";		
		String expectedEntryString =
				"ID   XXX; SV XXX; linear; XXX; XXX; XXX; 335 BP.\n"+
						"XX\n"+
						"AC   ;\n"+
						"XX\n"+
						"AC * _ENA\n"+
						"XX\n"+
						"DE   .\n"+
						"XX\n"+
						"KW   .\n"+
						"XX\n"+
						"CC   ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"+
						"XX\n"+
						"SQ   Sequence 335 BP; 69 A; 82 C; 95 G; 89 T; 0 other;\n"+
						"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n"+
						"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n"+
						"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n"+
						"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n"+
						"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n"+
						"     tctccgtgaa tgtctatcat tcctacacag gaccc                                  335\n"+
						"//\n"+
						"ID   XXX; SV XXX; linear; XXX; XXX; XXX; 194 BP.\n"+
						"XX\n"+
						"AC   ;\n"+
						"XX\n"+
						"AC * _ENA\n"+
						"XX\n"+
						"DE   .\n"+
						"XX\n"+
						"KW   .\n"+
						"XX\n"+
						"CC   ENA|A00002|A00002.1 B.taurus DNA sequence 1 from patent application EP0238993\n"+
						"XX\n"+
						"SQ   Sequence 194 BP; 43 A; 55 C; 49 G; 47 T; 0 other;\n"+
						"     aattcatgcg tccggacttc tgcctcgagc cgccgtacac tgggccctgc aaagctcgta        60\n"+
						"     tcatccgtta cttctacaat gcaaaggcag gcctgtgtca gaccttcgta tacggcggtt       120\n"+
						"     gccgtgctaa gcgtaacaac ttcaaatccg cggaagactg cgaacgtact tgcggtggtc       180\n"+
						"     cttagtaaag cttg                                                         194\n"+
						"//\n";

		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new FastaFileReader(new FastaLineReader(bufferedReader));
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
	
	public void testRead_wrongSequenceBase() throws IOException {
		String entryString =
				">ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"+
						"111TTGTTTGATGGAGAATTGCGCAGAGGGGTTATATCTGCGTGAGGATCTGTCACTCGG\n"+
						"CGGTGTGGGATACCTCCCTGCTAAGGCGGGTTGAGTGATGTTCCCTCGGACTGGGGACCG\n"+
						"CTGGCTTGCGAGCTATGTCCGCTACTCTCAGTACTACACTCTCATTTGAGCCCCCGCTCA\n"+
						"GTTTGCTAGCAGAACCCGGCACATGGTTCGCCGATACCATGGAATTTCGAAAGAAACACT\n"+
						"CTGTTAGGTGGTATGAGTCATGACGCACGCAGGGAGAGGCTAAGGCTTATGCTATGCTGA\n"+
						"TCTCCGTGAATGTCTATCATTCCTACACAGGACCC\n"+
						">ENA|A00002|A00002.1 B.taurus DNA sequence 1 from patent application EP0238993\n"+
						"AATTCATGCGTCCGGACTTCTGCCTCGAGCCGCCGTACACTGGGCCCTGCAAAGCTCGTA\n"+
						"TCATCCGTTACTTCTACAATGCAAAGGCAGGCCTGTGTCAGACCTTCGTATACGGCGGTT\n"+
						"GCCGTGCTAAGCGTAACAACTTCAAATCCGCGGAAGACTGCGAACGTACTTGCGGTGGTC\n"+
						"CTTAGTAAAGCTTG\n";		
		String expectedEntryString =
				"ID   XXX; SV XXX; linear; XXX; XXX; XXX; 332 BP.\n"+
						"XX\n"+
						"AC   ;\n"+
						"XX\n"+
						"AC * _ENA\n"+
						"XX\n"+
						"DE   .\n"+
						"XX\n"+
						"KW   .\n"+
						"XX\n"+
						"CC   ENA|A00001|A00001.1 Cauliflower mosaic virus satellite cDNA\n"+
						"XX\n"+
						"SQ   Sequence 332 BP; 69 A; 82 C; 94 G; 87 T; 0 other;\n"+
						"     ttgtttgatg gagaattgcg cagaggggtt atatctgcgt gaggatctgt cactcggcgg        60\n"+
						"     tgtgggatac ctccctgcta aggcgggttg agtgatgttc cctcggactg gggaccgctg       120\n"+
						"     gcttgcgagc tatgtccgct actctcagta ctacactctc atttgagccc ccgctcagtt       180\n"+
						"     tgctagcaga acccggcaca tggttcgccg ataccatgga atttcgaaag aaacactctg       240\n"+
						"     ttaggtggta tgagtcatga cgcacgcagg gagaggctaa ggcttatgct atgctgatct       300\n"+
						"     ccgtgaatgt ctatcattcc tacacaggac cc                                     332\n"+
						"//\n"+
						"ID   XXX; SV XXX; linear; XXX; XXX; XXX; 194 BP.\n"+
						"XX\n"+
						"AC   ;\n"+
						"XX\n"+
						"AC * _ENA\n"+
						"XX\n"+
						"DE   .\n"+
						"XX\n"+
						"KW   .\n"+
						"XX\n"+
						"CC   ENA|A00002|A00002.1 B.taurus DNA sequence 1 from patent application EP0238993\n"+
						"XX\n"+
						"SQ   Sequence 194 BP; 43 A; 55 C; 49 G; 47 T; 0 other;\n"+
						"     aattcatgcg tccggacttc tgcctcgagc cgccgtacac tgggccctgc aaagctcgta        60\n"+
						"     tcatccgtta cttctacaat gcaaaggcag gcctgtgtca gaccttcgta tacggcggtt       120\n"+
						"     gccgtgctaa gcgtaacaac ttcaaatccg cggaagactg cgaacgtact tgcggtggtc       180\n"+
						"     cttagtaaag cttg                                                         194\n"+
						"//\n";


		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		FlatFileEntryReader reader = new FastaFileReader(new FastaLineReader(bufferedReader));
        while (true) {
        	int errorCount=0;
    		ValidationResult result = reader.read();
    		Entry entry = reader.getEntry();
    		Collection<ValidationMessage<Origin>> messages = result.getMessages();
    		for ( ValidationMessage<Origin> message : messages) {
    			System.out.println(message.getMessage());		
    			errorCount++;
    		}
    		assertEquals(errorCount, result.count(Severity.ERROR));
            if (!reader.isEntry()) {
                break;
            }
    		assertTrue(new EmblEntryWriter(entry).write(writer));
        }
		assertEquals(expectedEntryString, writer.toString());
	}	
}
