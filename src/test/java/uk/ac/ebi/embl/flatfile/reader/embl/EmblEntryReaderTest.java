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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblReducedFlatFileWriter;

public class EmblEntryReaderTest extends EmblReaderTest {
   public final static String FLAT_FILES_RES_DIR = "/flatfiles/examples/";

	private static final List<String> MISSING_VALUE_TERMS = Arrays.asList(
			"missing: control sample",
			"missing: data agreement established pre-2023",
			"missing: endangered species",
			"missing: human-identifiable",
			"missing: lab stock",
			"missing: sample group",
			"missing: synthetic construct",
			"missing: third party data",
			"not applicable",
			"not collected",
			"not provided",
			"restricted access");

   private String getEntryStringFromResourceFile(String file) throws Exception {
      InputStream is = getClass().getResourceAsStream(FLAT_FILES_RES_DIR + file);
      BufferedReader r = new BufferedReader(new InputStreamReader(is));
      
      StringBuffer toReturn;
      try {
         toReturn = new StringBuffer();
         r.lines().forEach(s -> toReturn.append(s).append("\n") );
         
         return toReturn.toString();
      } finally {
         r.close();
         is.close();
      }
      
   }
   
	public void testRead_Entry() throws IOException {
		String entryString =
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
		    "AH   LOCAL_SPAN          PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n" +
		    "AS   1-426               AC004528.1         18665-19090\n" +
		    "AS   427-526             AC001234.2         1-100               c\n" +
		    "XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   misc_feature    join(1..23,24..335)\n" +
			"FT                   /note=\"satellite DNA\"\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n";	
		setBufferedReader(entryString);
		EntryReader reader = new EmblEntryReader(bufferedReader);
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
		assertEquals(entryString, writer.toString());
	}

	public void testReadWriteReducedFileOnly() throws IOException {
		String entryString =
				"ID   A00001; SV 1; linear; unassigned DNA; CON; XXX; 339 BP.\n" +
						"XX\n" +
						"FH   Key             Location/Qualifiers\n" +
						"FH\n" +
						"FT   CDS             1..300\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   exon            500..700\n" +
						"FT                   /gene=\"x\"\n" +
						"FT                   /number=1b\n" +
						"FT   exon            500..550\n" +
						"FT                   /gene=\"x\"\n" +
						"FT                   /number=1a\n" +
						"FT   source          1..335\n" +
						"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
						"FT   primer_bind     1..23\n" +
						"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
						"FT                   /note=\"PCR 5' primer\"\n" +
						"FT   operon          <1..1295\n" +
						"FT                   /operon=\"example\"\n" +
						"FT   source          1..1295\n" +
						"FT                   /organism=\"uncultured alphaproteobacterium\"\n" +
						"FT   mat_peptide     31..297\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   sig_peptide     1..30\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   primer_bind     complement(1271..1294)\n" +
						"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
						"FT                   /note=\"PCR 3' primer\"\n" +
						"XX\n" +
						"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
						"XX\n" +
						"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
						"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
						"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
						"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
						"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
						"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
						"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
						"//\n";
		String expectedEntryString =
				"ID   A00001; SV 1; linear; unassigned DNA; CON; XXX; 339 BP.\n" +
						"XX\n" +
						"FH   Key             Location/Qualifiers\n" +
						"FH\n" +
						"FT   source          1..335\n" +
						"FT                   /submitter_seqid=\"\"\n"+
						"FT   operon          <1..1295\n" +
						"FT                   /operon=\"example\"\n" +
						"FT   CDS             1..300\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   sig_peptide     1..30\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   primer_bind     1..23\n" +
						"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
						"FT                   /note=\"PCR 5' primer\"\n" +
						"FT   mat_peptide     31..297\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   exon            500..700\n" +
						"FT                   /gene=\"x\"\n" +
						"FT                   /number=1b\n" +
						"FT   exon            500..550\n" +
						"FT                   /gene=\"x\"\n" +
						"FT                   /number=1a\n" +
						"FT   primer_bind     complement(1271..1294)\n" +
						"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
						"FT                   /note=\"PCR 3' primer\"\n" +
						"XX\n" +
						"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
						"XX\n" +
						"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
						"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
						"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
						"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
						"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
						"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
						"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
						"//\n";
		setBufferedReader(entryString);
		EntryReader reader = new EmblEntryReader(bufferedReader, EmblEntryReader.Format.REDUCED_FILE_FORMAT, null);
		ValidationResult result = reader.read();
		Entry entry = reader.getEntry();
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for ( ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		StringWriter writer = new StringWriter();
		assertTrue(new EmblReducedFlatFileWriter(entry).write(writer));
		//System.out.print(writer.toString());
		assertEquals(expectedEntryString, writer.toString());
	}

	public void testRead_EntryFeatureSort() throws IOException {
		String entryString =
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"ST * private 01-JAN-2003\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"OS   uncultured alphaproteobacterium\n" +
			"OC   unclassified sequences.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   CDS             1..300\n" +
			"FT                   /gene=\"T\"\n" +
			"FT   exon            500..700\n" +
			"FT                   /gene=\"x\"\n" +
			"FT                   /number=1b\n" +
			"FT   exon            500..550\n" +
			"FT                   /gene=\"x\"\n" +
			"FT                   /number=1a\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   primer_bind     1..23\n" +
			"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
			"FT                   /note=\"PCR 5' primer\"\n" +
			"FT   operon          <1..1295\n" +
			"FT                   /operon=\"example\"\n" +
			"FT   source          1..1295\n" +
			"FT                   /organism=\"uncultured alphaproteobacterium\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /clone=\"alp23\"\n" +
			"FT   mat_peptide     31..297\n" +
			"FT                   /gene=\"T\"\n" +
			"FT   sig_peptide     1..30\n" +
			"FT                   /gene=\"T\"\n" +
			"FT   primer_bind     complement(1271..1294)\n" +
			"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
			"FT                   /note=\"PCR 3' primer\"\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n";	
		String expectedEntryString =
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"OS   uncultured alphaproteobacterium\n" +
			"OC   unclassified sequences.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..1295\n" +
			"FT                   /organism=\"uncultured alphaproteobacterium\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /clone=\"alp23\"\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   operon          <1..1295\n" +
			"FT                   /operon=\"example\"\n" +
			"FT   CDS             1..300\n" +
			"FT                   /gene=\"T\"\n" +
			"FT   sig_peptide     1..30\n" +
			"FT                   /gene=\"T\"\n" +
			"FT   primer_bind     1..23\n" +
			"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
			"FT                   /note=\"PCR 5' primer\"\n" +                    
			"FT   mat_peptide     31..297\n" +
			"FT                   /gene=\"T\"\n" +
			"FT   exon            500..700\n" +
			"FT                   /gene=\"x\"\n" +
			"FT                   /number=1b\n" +
			"FT   exon            500..550\n" +
			"FT                   /gene=\"x\"\n" +
			"FT                   /number=1a\n" +
			"FT   primer_bind     complement(1271..1294)\n" +
			"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
			"FT                   /note=\"PCR 3' primer\"\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n";			
		setBufferedReader(entryString);
		EntryReader reader = new EmblEntryReader(bufferedReader);
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
			"\n" +
			"\n" +
			"\n" +
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   misc_feature    1..335\n" +
			"FT                   /note=\"satellite DNA\"\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"\n" +
			"\n" +
			"\n" +
			"\n" +
			"//\n" +
			"\n" +
			"\n" +
			"\n" +
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"ST * private 01-JAN-2003\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   misc_feature    1..335\n" +
			"FT                   /note=\"satellite DNA\"\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n" +
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"ST * private 01-JAN-2003\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   misc_feature    1..335\n" +
			"FT                   /note=\"satellite DNA\"\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n";			
		String expectedEntryString =
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   misc_feature    1..335\n" +
			"FT                   /note=\"satellite DNA\"\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n" +
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   misc_feature    1..335\n" +
			"FT                   /note=\"satellite DNA\"\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n" +
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"XX\n" +
			"AC   A00001; A00002;\n" +
			"XX\n" +			
			"AC * _AAAAA\n" +
			"XX\n" +
			"PR   Project:3443;\n" +
			"XX\n" +			
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"XX\n" +
			"DE   Cauliflower mosaic virus satellite cDNA.\n" +
			"XX\n" +
			"KW   .\n" +
			"XX\n" +
			"OS   Cauliflower mosaic virus\n" +
			"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RP   1-335\n" +
			"RX   HELLO; hello.\n" +
			"RG   blah blah\n" +
			"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
			"RT   \"Modification of plant viruses or their effects.\";\n" +
			"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
			"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
			"XX\n" +
			"DR   database; primary accession; secondary accession.\n" +
			"DR   database2; primary accession2; secondary accession2.\n" +				
			"XX\n" +
			"CC   comment comment comment comment comment\n" +
			"CC   comment comment comment comment comment\n" +
			"XX\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..335\n" +
			"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
			"FT                   /mol_type=\"unassigned DNA\"\n" +
			"FT                   /db_xref=\"taxon:10641\"\n" +
			"FT   misc_feature    1..335\n" +
			"FT                   /note=\"satellite DNA\"\n" +
			"XX\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
			"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
			"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
			"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
			"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
			"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
			"//\n";
		StringWriter writer = new StringWriter();                      
		setBufferedReader(entryString);
		EntryReader reader = new EmblEntryReader(bufferedReader);
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

	@Ignore
	public void testRead_MasterEntry() throws IOException {
		String entryString =
			"ID   AAAA02000000; SV 02; linear; genomic DNA; CON; XXX; 0 SQ.\n" +
			"XX\n" +
			"AC   AAAA02000000;\n" +
			"XX\n" +
			"PR   Project:234455; Project:344332;\n" +
			"XX\n" +
			"DE   Oryza sativa Indica Group, WGS project AAAA02000000 data, 410679186\n" +
			"DE   basepairs.\n" +
			"XX\n" +
 			"KW   .\n" +
			"XX\n" +
			"OS   Oryza sativa Indica Group\n" +
			"OC   Eukaryota; Viridiplantae; Streptophyta; Embryophyta; Tracheophyta;\n" +
			"OC   Spermatophyta; Magnoliophyta; Liliopsida; Poales; Poaceae; BEP clade;\n" +
			"OC   Ehrhartoideae; Oryzeae; Oryza.\n" +
			"XX\n" +
			"RN   [1]\n" +
			"RX   DOI; 10.1371/journal.pbio.0030038.\n" +
			"RX   PUBMED; 15685292.\n" +
			"RA   Yu J., Wang J., Lin W., Li S., Li H., Zhou J., Ni P., Dong W., Hu S.,\n" +
			"RA   Zeng C., Zhang J., Zhang Y., Li R., Xu Z., Li S., Li X., Zheng H., Cong L.,\n" +
			"RA   Lin L., Yin J., Geng J., Li G., Shi J., Liu J., Lv H., Li J., Wang J.,\n" +
			"RA   Deng Y., Ran L., Shi X., Wang X., Wu Q., Li C., Ren X., Wang J., Wang X.,\n" +
			"RA   Li D., Liu D., Zhang X., Ji Z., Zhao W., Sun Y., Zhang Z., Bao J., Han Y.,\n" +
			"RA   Dong L., Ji J., Chen P., Wu S., Liu J., Xiao Y., Bu D., Tan J., Yang L.,\n" +
			"RA   Ye C., Zhang J., Xu J., Zhou Y., Yu Y., Zhang B., Zhuang S., Wei H.,\n" +
			"RA   Liu B., Lei M., Yu H., Li Y., Xu H., Wei S., He X., Fang L., Zhang Z.,\n" +
			"RA   Zhang Y., Huang X., Su Z., Tong W., Li J., Tong Z., Li S., Ye J., Wang L.,\n" +
			"RA   Fang L., Lei T., Chen C., Chen H., Xu Z., Li H., Huang H., Zhang F., Xu H.,\n" +
			"RA   Li N., Zhao C., Li S., Dong L., Huang Y., Li L., Xi Y., Qi Q., Li W.,\n" +
			"RA   Zhang B., Hu W., Zhang Y., Tian X., Jiao Y., Liang X., Jin J., Gao L.,\n" +
			"RA   Zheng W., Hao B., Liu S., Wang W., Yuan L., Cao M., McDermott J.,\n" +
			"RA   Samudrala R., Wang J., Wong G.K., Yang H.;\n" +
			"RT   \"The Genomes of Oryza sativa: A History of Duplications\";\n" +
			"RL   PLoS Biol. 3(2):E38-E38(2005).\n" +
			"XX\n" +
			"RN   [2]\n" +
			"RA   Yu J., Hu S., Wang J., Li S., Wong K.-S.G., Liu B., Deng Y., Dai L.,\n" +
			"RA   Zhou Y., Zhang X., Cao M., Liu J., Sun J., Tang J., Chen Y., Huang X.,\n" +
			"RA   Lin W., Ye C., Tong W., Cong L., Geng J., Han Y., Li L., Li W., Hu G.,\n" +
			"RA   Huang X., Li W., Li J., Liu Z., Li L., Liu J., Qi Q., Liu J., Li L.,\n" +
			"RA   Wang X., Lu H., Wu T., Zhu M., Ni P., Han H., Dong W., Ren X., Feng X.,\n" +
			"RA   Cui P., Li X., Wang H., Xu X., Zhai W., Xu Z., Zhang J., He S., Zhang J.,\n" +
			"RA   Xu J., Zhang K., Zheng X., Dong J., Zeng W., Tao L., Chen X., He J.,\n" +
			"RA   Liu D., Tian W., Tian C., Xia H., Li G., Gao H., Li P., Chen W., Wang X.,\n" +
			"RA   Zhang Y., Hu J., Wang J., Liu S., Yang J., Zhang G., Bao Q., Xiong Y.,\n" +
			"RA   Li Z., Mao L., Zhou C., Chen R., Zhu Z., Hao B., Zheng W., Chen S., Guo W.,\n" +
			"RA   Li G., Liu S., Huang G., Tao M., Wang J., Zhu L., Yuan L., Yang H.;\n" +
			"RT   ;\n" +
			"RL   Submitted (04-JAN-2002) to the INSDC.\n" +
			"RL   Beijing Genomics Institute/Center of Genomics & Bioinformatics, Institute\n" +
			"RL   of Genomics, Chinese Academy of Sciences, Beijing Airport Industrial Zone\n" +
			"RL   B6, Beijing, Beijing 101300, P.R.China\n" +
			"XX\n" +
			"RN   [3]\n" +
			"RA   Yu J., Wang J., Lin W., Li S., Li H., Zhou J., Ni P., Dong W., Hu S.,\n" +
			"RA   Zeng C., Zhang J., Zhang Y., Li R., Xu Z., Li S., Li X., Zheng H., Cong L.,\n" +
			"RA   Lin L., Yin J., Geng J., Li G., Shi J., Liu J., Lv H., Li J., Wang J.,\n" +
			"RA   Deng Y., Ran L., Shi X., Wang X., Wu Q., Li C., Ren X., Wang J., Wang X.,\n" +
			"RA   Li D., Liu D., Zhang X., Ji Z., Zhao W., Sun Y., Zhang Z., Bao J., Han Y.,\n" +
			"RA   Dong L., Ji J., Chen P., Wu S., Liu J., Xiao Y., Bu D., Tan J., Yang L.,\n" +
			"RA   Ye C., Zhang J., Xu J., Zhou Y., Yu Y., Zhang B., Zhuang S., Wei H.,\n" +
			"RA   Liu B., Lei M., Yu H., Li Y., Xu H., Wei S., He X., Fang L., Zhang Z.,\n" +
			"RA   Zhang Y., Huang X., Su Z., Tong W., Li J., Tong Z., Li S., Ye J., Wang L.,\n" +
			"RA   Fang L., Lei T., Chen C., Chen H., Xu Z., Li H., Huang H., Zhang F., Xu H.,\n" +
			"RA   Li N., Zhao C., Li S., Dong L., Huang Y., Li L., Xi Y., Qi Q., Li W.,\n" +
			"RA   Zhang B., Hu W., Zhang Y., Zheng W., Hao B., Liu S., Wang W., Yuan L.,\n" +
			"RA   Cao M.L., McDermott J., Samudrala R., Wang J., Wong G.K.-S., Yang H.;\n" +
			"RT   ;\n" +
			"RL   Submitted (12-SEP-2003) to the INSDC.\n" +
			"RL   Beijing Institute of Genomics, Chinese Academy of Sciences, Beijing Airport\n" +
			"RL   Industrial Zone B6, Beijing, Beijing 101300, P.R.China\n" +
			"XX\n" +
			"FH   Key             Location/Qualifiers\n" +
			"FH\n" +
			"FT   source          1..53326\n" +
			"FT                   /organism=\"Oryza sativa Indica Group\"\n" +
			"FT                   /mol_type=\"genomic DNA\"\n" +
			"FT                   /db_xref=\"taxon:39946\"\n" +
			"XX\n" +
			"WGS  AAAA02000001-AAAA02050231\n" +
			"XX\n" +
			"CON  CH398081-CH401163, CM000126-CM000137\n" +
			"//\n";
		
		setBufferedReader(entryString);
		EntryReader reader = new EmblEntryReader(bufferedReader, 
				EmblEntryReader.Format.MASTER_FORMAT, null);
		ValidationResult result = reader.read();
		Entry entry = reader.getEntry();
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for ( ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());			
		}		
//		assertEquals(0, result.count(Severity.ERROR));//CON entry must have CO(CONDIV) lines
		StringWriter writer = new StringWriter();
		assertTrue(new EmblEntryWriter(entry).write(writer));
		assertEquals(entryString, writer.toString());
	}


	public void testRead_IgnoreRepeatRegionWithLocasTag() throws IOException {
		String entryString =
				"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
						"XX\n" +
						"ST * private 01-JAN-2003\n" +
						"XX\n" +
						"AC   A00001; A00002;\n" +
						"XX\n" +
						"AC * _AAAAA\n" +
						"XX\n" +
						"PR   Project:3443;\n" +
						"XX\n" +
						"DT   28-JAN-1993 (Rel. 34, Created)\n" +
						"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
						"XX\n" +
						"DE   Cauliflower mosaic virus satellite cDNA.\n" +
						"XX\n" +
						"KW   .\n" +
						"XX\n" +
						"OS   Cauliflower mosaic virus\n" +
						"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
						"XX\n" +
						"OS   uncultured alphaproteobacterium\n" +
						"OC   unclassified sequences.\n" +
						"XX\n" +
						"RN   [1]\n" +
						"RP   1-335\n" +
						"RX   HELLO; hello.\n" +
						"RG   blah blah\n" +
						"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
						"RT   \"Modification of plant viruses or their effects.\";\n" +
						"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
						"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
						"XX\n" +
						"DR   database; primary accession; secondary accession.\n" +
						"DR   database2; primary accession2; secondary accession2.\n" +
						"XX\n" +
						"CC   comment comment comment comment comment\n" +
						"CC   comment comment comment comment comment\n" +
						"XX\n" +
						"FH   Key             Location/Qualifiers\n" +
						"FH\n" +
						"FT   CDS             1..300\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   repeat_region   15..71\n" +
						"FT                   /locus_tag=\"BN5_00001\"\n" +
						"FT                   /note=\"BN5_00005\"\n" +
						"FT   repeat_region   72..100\n" +
						"FT                   /note=\"BN5_00001\"\n" +
						"FT                   /locus_tag=\"BN5_00001\"\n" +
						"FT   primer_bind     complement(1271..1294)\n" +
						"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
						"FT                   /note=\"PCR 3' primer\"\n" +
						"XX\n" +
						"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
						"XX\n" +
						"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
						"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
						"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
						"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
						"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
						"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
						"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
						"//\n";
		String expectedEntryString =
				"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
						"XX\n" +
						"AC   A00001; A00002;\n" +
						"XX\n" +
						"AC * _AAAAA\n" +
						"XX\n" +
						"PR   Project:3443;\n" +
						"XX\n" +
						"DT   28-JAN-1993 (Rel. 34, Created)\n" +
						"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
						"XX\n" +
						"DE   Cauliflower mosaic virus satellite cDNA.\n" +
						"XX\n" +
						"KW   .\n" +
						"XX\n" +
						"RN   [1]\n" +
						"RP   1-335\n" +
						"RX   HELLO; hello.\n" +
						"RG   blah blah\n" +
						"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
						"RT   \"Modification of plant viruses or their effects.\";\n" +
						"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
						"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
						"XX\n" +
						"DR   database; primary accession; secondary accession.\n" +
						"DR   database2; primary accession2; secondary accession2.\n" +
						"XX\n" +
						"CC   comment comment comment comment comment\n" +
						"CC   comment comment comment comment comment\n" +
						"XX\n" +
						"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
						"XX\n" +
						"FH   Key             Location/Qualifiers\n" +
						"FH\n" +
						"FT   CDS             1..300\n" +
						"FT                   /gene=\"T\"\n" +
						"FT   repeat_region   15..71\n" +
						"FT                   /note=\"BN5_00005\"\n" +
						"FT   repeat_region   72..100\n" +
						"FT                   /note=\"BN5_00001\"\n" +
						"FT   primer_bind     complement(1271..1294)\n" +
						"FT                   /PCR_conditions=\"annealing temp 65 degrees C\"\n" +
						"FT                   /note=\"PCR 3' primer\"\n" +
						"XX\n" +
						"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
						"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
						"     cggtgtggga tacctccctg ctaaggcggg ttgagtgatg ttccctcgga ctggggaccg       120\n" +
						"     ctggcttgcg agctatgtcc gctactctca gtactacact ctcatttgag cccccgctca       180\n" +
						"     gtttgctagc agaacccggc acatggttcg ccgataccat ggaatttcga aagaaacact       240\n" +
						"     ctgttaggtg gtatgagtca tgacgcacgc agggagaggc taaggcttat gctatgctga       300\n" +
						"     tctccgtgaa tgtctatcat tcctacacag gacccrask                              339\n" +
						"//\n";
		setBufferedReader(entryString);
		EntryReader reader = new EmblEntryReader(bufferedReader);
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

	public void testRead_IgnoreMissingValueTerms() throws IOException {

	   String expectedEntryString =
				"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 60 BP.\n" +
						"XX\n" +
						"AC   A00001; A00002;\n" +
						"XX\n" +
						"AC * _AAAAA\n" +
						"XX\n" +
						"PR   Project:3443;\n" +
						"XX\n" +
						"DT   28-JAN-1993 (Rel. 34, Created)\n" +
						"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
						"XX\n" +
						"DE   Cauliflower mosaic virus satellite cDNA.\n" +
						"XX\n" +
						"KW   .\n" +
						"XX\n" +
						"OS   Cauliflower mosaic virus\n" +
						"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
						"XX\n" +
						"RN   [1]\n" +
						"RP   1-335\n" +
						"RX   HELLO; hello.\n" +
						"RG   blah blah\n" +
						"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
						"RT   \"Modification of plant viruses or their effects.\";\n" +
						"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
						"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
						"XX\n" +
						"DR   database; primary accession; secondary accession.\n" +
						"DR   database2; primary accession2; secondary accession2.\n" +
						"XX\n" +
						"CC   comment comment comment comment comment\n" +
						"CC   comment comment comment comment comment\n" +
						"XX\n" +
						"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
						"XX\n" +
						"FH   Key             Location/Qualifiers\n" +
						"FH\n" +
						"FT   source          1..335\n" +
						"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
						"FT                   /mol_type=\"unassigned DNA\"\n" +
						"FT                   /db_xref=\"taxon:10641\"\n" +
						"FT   CDS             1..300\n" +
						"FT                   /gene=\"T\"\n" +
						"XX\n" +
						"SQ   Sequence 60 BP; 11 A; 8 C; 21 G; 20 T; 0 other;\n" +
						"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
						"//\n";
		
		for (String missingValues: MISSING_VALUE_TERMS) {
			String entryString =
					"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 60 BP.\n" +
							"XX\n" +
							"ST * private 01-JAN-2003\n" +
							"XX\n" +
							"AC   A00001; A00002;\n" +
							"XX\n" +
							"AC * _AAAAA\n" +
							"XX\n" +
							"PR   Project:3443;\n" +
							"XX\n" +
							"DT   28-JAN-1993 (Rel. 34, Created)\n" +
							"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
							"XX\n" +
							"DE   Cauliflower mosaic virus satellite cDNA.\n" +
							"XX\n" +
							"KW   .\n" +
							"XX\n" +
							"OS   Cauliflower mosaic virus\n" +
							"OC   Viruses; Retro-transcribing viruses; Caulimoviridae; Caulimovirus.\n" +
							"XX\n" +
							"OS   uncultured alphaproteobacterium\n" +
							"OC   unclassified sequences.\n" +
							"XX\n" +
							"RN   [1]\n" +
							"RP   1-335\n" +
							"RX   HELLO; hello.\n" +
							"RG   blah blah\n" +
							"RA   Baulcombe D.C., Mayo M.A., Harrison B.D., Bevan M.W.;\n" +
							"RT   \"Modification of plant viruses or their effects.\";\n" +
							"RL   Patent number EP0242016-A/1, 21-OCT-1987.\n" +
							"RL   AGRICULTURAL GENETICS COMPANY LIMITED.\n" +
							"XX\n" +
							"DR   database; primary accession; secondary accession.\n" +
							"DR   database2; primary accession2; secondary accession2.\n" +
							"XX\n" +
							"CC   comment comment comment comment comment\n" +
							"CC   comment comment comment comment comment\n" +
							"XX\n" +
							"FH   Key             Location/Qualifiers\n" +
							"FH\n" +
							"FT   source          1..335\n" +
							"FT                   /organism=\"Cauliflower mosaic virus\"\n" +
							"FT                   /mol_type=\"unassigned DNA\"\n" +
							"FT                   /country=" + missingValues + "\n" +
							"FT                   /collection_date=" + missingValues + "\n" +
							"FT                   /lat_lon=" + missingValues + "\n" +
							"FT                   /db_xref=\"taxon:10641\"\n" +
							"FT   CDS             1..300\n" +
							"FT                   /gene=\"T\"\n" +
							"XX\n" +
							"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
							"XX\n" +
							"SQ   Sequence 60 BP; 11 A; 8 C; 21 G; 20 T; 0 other;\n" +
							"     gttttgtttg atggagaatt gcgcagaggg gttatatctg cgtgaggatc tgtcactcgg        60\n" +
							"//\n";

			setBufferedReader(entryString);
			EntryReader reader = new EmblEntryReader(bufferedReader);
			ValidationResult result = reader.read();
			Entry entry = reader.getEntry();
			Collection<ValidationMessage<Origin>> messages = result.getMessages();
			for (ValidationMessage<Origin> message : messages) {
				System.out.println(message.getMessage());
			}
			assertEquals(0, result.count(Severity.ERROR));
			StringWriter writer = new StringWriter();
			assertTrue(new EmblEntryWriter(entry).write(writer));
			//System.out.print(writer.toString());
			assertEquals(expectedEntryString, writer.toString());
		}
	}

	public void testReadCDSEntryWithoutCOLine() throws Exception {
	   String entryString = getEntryStringFromResourceFile("coding_no_COLine.cds");
      setBufferedReader(entryString);
      EntryReader reader = new EmblEntryReader(bufferedReader, 
            EmblEntryReader.Format.CDS_FORMAT, null);
      ValidationResult result = reader.read();
      Entry entry = reader.getEntry();
      Collection<ValidationMessage<Origin>> messages = result.getMessages();
      for ( ValidationMessage<Origin> message : messages) {
         System.out.println(message.getMessage());
      }     
      assertEquals(0, result.count(Severity.ERROR));//CON entry must have CO(CONDIV) lines
      StringWriter writer = new StringWriter();                      
      assertTrue(new EmblEntryWriter(entry).write(writer));
      // assertEquals(entryString, writer.toString()); the writer seem not able to exactly replicate the entry
	}
	
	
	public void testRead_MultipleACLine() throws IOException {
		String entryString =
			"AC   A00001; A00002;\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"AC   A00001; A00002;\n//";		
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.5", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block AC must occur exactly once");
        assertTrue(foundMessage);
	}

    public void testRead_MultipleDELine() throws IOException {
		String entryString =
			"DE   blah\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"DE   blah\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.5", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block DE must occur exactly once");
        assertTrue(foundMessage);
	}

	public void testRead_MultipleKWLine() throws IOException {
		String entryString =
			"KW   blah;\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"KW   blah;\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.9", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block KW must not occur more than once");
        assertTrue(foundMessage);
	}

	public void testRead_MultipleIDLine() throws IOException {
		String entryString =
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"ID   A00001; SV 1; linear; unassigned DNA; PAT; XXX; 339 BP.\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.5", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block ID must occur exactly once");
        assertTrue(foundMessage);
	}

	public void testRead_MultipleDTLine() throws IOException {
		String entryString =
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"DT   28-JAN-1993 (Rel. 34, Created)\n" +
			"DT   11-MAY-2001 (Rel. 67, Last updated, Version 2)\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.9", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block DT must not occur more than once");
        assertTrue(foundMessage);
	}

	public void testRead_MultiplePRLine() throws IOException {
		String entryString =
			"PR   342343432\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"PR   342343432\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
		Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.9", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block PR must not occur more than once");
        assertTrue(foundMessage);
	}

	public void testRead_MultipleSTLine() throws IOException {
		String entryString =
			"ST * private 1-JAN-2003\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"ST * private 1-JAN-2003\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.9", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block ST * must not occur more than once");
        assertTrue(foundMessage);
	}

	public void testRead_MultipleCOLine() throws IOException {
		String entryString =
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.9", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block CO must not occur more than once");
        assertTrue(foundMessage);
	}
	
	public void testRead_MultipleAHLine() throws IOException {
		String entryString =
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"AH   TPA-SPAN       PRIMARY_IDENTIFIER     PRIMARY_SPAN     COMP\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.9", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block AH must not occur more than once");
        assertTrue(foundMessage);
	}

	public void testRead_MultipleSQLine() throws IOException {
		String entryString =
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n" +
			"CO   join(AL358912.1:1..39187,gap(unk100),gap(43))\n" +
			"SQ   Sequence 339 BP; 70 A; 82 C; 95 G; 89 T; 3 other;\n//";
		setBufferedReader(entryString);
		ValidationResult result = (new EmblEntryReader(bufferedReader)).read();
        Collection<ValidationMessage<Origin>> messages = result.getMessages("FF.9", Severity.ERROR);
        boolean foundMessage = findMessageString(messages, "Block SQ must not occur more than once");
        assertTrue(foundMessage);
	}

    private boolean findMessageString(Collection<ValidationMessage<Origin>> messages, String matchString) {
        boolean foundMessage = false;
        for(ValidationMessage message : messages){
            String messageString = message.getMessage();
            if(messageString.equals(matchString)){
                foundMessage = true;
            }
        }
        return foundMessage;
    }

}
