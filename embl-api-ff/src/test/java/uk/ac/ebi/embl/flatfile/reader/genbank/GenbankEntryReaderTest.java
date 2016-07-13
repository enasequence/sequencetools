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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblReaderTest;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankEntryReader;
import uk.ac.ebi.embl.flatfile.writer.genbank.GenbankEntryWriter;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class GenbankEntryReaderTest extends EmblReaderTest {

	public void testRead_Entry() throws IOException {
		String entryString =
			"LOCUS       DP000153                 122 bp    mRNA    linear   MAM 10-SEP-1998\n"+
			"DEFINITION  Cloning and characterization of a cDNA encoding a novel subtype of\n"+
			"            rat thyrotropin-releasing hormone receptor.\n"+
			"ACCESSION   DP000153\n"+
			"VERSION     DP000153.1\n" +
			"KEYWORDS    .\n"+
			"SOURCE      Mus musculus (house mouse)\n" +
			"  ORGANISM  Mus musculus\n" +
			"            Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi;\n" +
			"            Mammalia; Eutheria; Euarchontoglires; Glires; Rodentia;\n" +
			"            Sciurognathi; Muroidea; Muridae; Murinae; Mus.\n" +
			"REFERENCE   1\n"+
			"  AUTHORS   Okazaki,Y., Furuno,M., Kasukawa,T., Adachi,J., Bono,H., Kondo,S.,\n" +
	        "            Nikaido,I., Osato,N., Saito,R., Suzuki,H., Yamanaka,I., Rogers,J.,\n" +
	        "            Birney,E. and Hayashizaki,Y.\n" +
			"  CONSRTM   Google consortium\n"+
	        "  TITLE     Cloning and characterization of a cDNA encoding a novel subtype of\n"+
			"            rat thyrotropin-releasing hormone receptor\n"+
			"  JOURNAL   J. Biol. Chem. 273(48),32281-32287(1998)\n"+
			"  REMARK    reference comment\n"+
			"COMMENT     Cloning\n"+
			"            and characterization\n"+
			"            of a cDNA encoding a novel subtype of rat\n"+
			"            thyrotropin-releasing hormone receptor\n"+
			"PRIMARY     TPA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n"+
			"            1-426               AC004528.1         18665-19090         c\n"+
			"            6-546               AC004529.6         45665-98790         c\n"+
			"            1-426               AC004528.1         18665-19090         c\n"+
			"            6-546               AC004529.6         45665-98790         c\n"+
			"FEATURES             Location/Qualifiers\n"+
			"     source          1..353\n" +
            "                     /organism=\"Mus musculus\"\n" +
            "                     /mol_type=\"mRNA\"\n" +
            "                     /db_xref=\"taxon:10090\"\n" +			
			"     gene            order(AF133070.1:<1..1930,AF133071.1:1..940,\n"+
			"                     AF133072.1:1..1365,AF133073.1:1..939,AF133074.1:1..1759,\n"+
			"                     AF133075.1:1..733,AF133076.1:1..681,AF133077.1:1..180,\n"+
			"                     AF133078.1:1..495,AF133079.1:1..836,AF133080.1:1..269,\n"+
			"                     AF133081.1:1..898,AF133082.1:1..505,AF133083.1:1..387,\n"+
			"                     AF133084.1:1..2219,1..>280)\n"+
			"                     /gene=\"Itgae\"\n"+
			"     mRNA            join(AF133070.1:<1616..1739,AF133071.1:578..675,\n"+
			"                     AF133072.1:374..441,AF133072.1:806..923,\n"+
			"                     AF133072.1:1117..1248,AF133073.1:98..213,\n"+
			"                     AF133073.1:727..878,AF133074.1:10..163,\n"+
			"                     AF133074.1:672..822,AF133074.1:1236..1303,\n"+
			"                     AF133075.1:120..258,AF133076.1:267..412,\n"+
			"                     AF133077.1:22..162,AF133078.1:199..423,\n"+
			"                     AF133079.1:325..455,AF133080.1:23..153,\n"+
			"                     AF133081.1:344..504,AF133082.1:106..234,\n"+
			"                     AF133083.1:79..152,AF133084.1:1961..2093,186..>280)\n"+
			"                     /gene=\"Itgae\"\n"+
			"                     /product=\"Itgae protein\"\n"+
			"     CDS             join(AF133070.1:<1616..1739,AF133071.1:578..675,\n"+
			"                     AF133072.1:374..441,AF133072.1:806..923,\n"+
			"                     AF133072.1:1117..1248,AF133073.1:98..213,\n"+
			"                     AF133073.1:727..878,AF133074.1:10..163,\n"+
			"                     AF133074.1:672..822,AF133074.1:1236..1303,\n"+
			"                     AF133075.1:120..258,AF133076.1:267..412,\n"+
			"                     AF133077.1:22..162,AF133078.1:199..423,\n"+
			"                     AF133079.1:325..455,AF133080.1:23..153,\n"+
			"                     AF133081.1:344..504,AF133082.1:106..234,\n"+
			"                     AF133083.1:79..152,AF133084.1:1961..2093,186..>280)\n"+
			"                     /codon_start=3\n"+
			"                     /gene=\"Itgae\"\n"+
			"                     /product=\"Itgae protein\"\n"+
			"                     /db_xref=\"GI:4809045\"\n"+
			"                     /protein_id=\"AAD30063.1\"\n"+
			"                     /translation=\"LKPQGAFNMDVDWAWVTALQPGAPAVLSSLLHLDPSNNQTCLLV\n"+
			"                     ARRSSNRNTAALYRCAISISPDEIACQPVEHICMPKGRYQGVTLVGNHNGVLVCIQVQ\n"+
			"                     ARKFRSLNSELTGACSLLTPNLDLQAQAYFSDLEGFLDPGAHVDSGDYCRSKGGSTGE\n"+
			"                     EKKSARRRRTVEEEDEEEDGTEIAIVLDGSGSIEPSDFQKAKNFISTMMRNFYEKCFE\n"+
			"                     CNFALVQYGAVIQTEFDLQESRDINASLAKVQSIVQVKEVTKTASAMQHVLDNIFIPS\n"+
			"                     RGSRKKALKVMVVLTDGDIFGDPLNLTTVINSPKMQGVVRFAIGVGDAFKNNNTYREL\n"+
			"                     KLIASDPKEAHTFKVTNYSALDGLLSKLQQRIVHMEGTVGDALQYQLAQTGFSAQILD\n"+
			"                     KGQVLLGTVGAFNWSGGALLYSTQNGRGCFLNQTAKEDSRTVQYSYLGYSLAVLHKAH\n"+
			"                     GISYVAGAPRHKLRGAVFELRKEDREEDAFVRRIEGEQMGSYFGSVLCPVDIDMDGTT\n"+
			"                     DFLLVAAPFYHIRGEEGRVYVYQVPEQDASFSLAHTLSGHPGLTNSRFGFAMAAVGDI\n"+
			"                     NQDKFTDVAIGAPLEGFGAGDGASYGSVYIYNGHSGGLYDSPSQQIRASSVASGLHYF\n"+
			"                     GMSVSGGLDFNGDGLADITVGSRDSAVVLRSRPVVDLTVSMTFTPDALPMVFIGKMDV\n"+
			"                     NLCFEVDSSVVASEPGLREMFLNFTVDVDVTKQRQRLQCEDSSGCQSCLRKWNGGSFL\n"+
			"                     CEHFWLISTEELCEEDCFSNITIKVTYEFQTSGGRRDYPNPTLDHYKEPSAIFQLPYE\n"+
			"                     KDCKNKVFCIAEIQLTTNISQQELVVGVTKEVTMNISLTNSGEDSYMTNMALNYPRNL\n"+
			"                     QFKKIQKPVSPDVQCDDPKPVASVLVMNCKIGHPILKRS\"\n"+	
			"     CDS             3514..4041\n"+
			"                     /product=\"hypothetical protein\"\n"+
			"                     /note=\"ORF 5\"\n"+
			"                     /db_xref=\"InterPro:IPR001964\"\n"+
			"                     /db_xref=\"UniProtKB/Swiss-Prot:P09511\"\n"+
			"                     /protein_id=\"CAA31466.1\"\n"+			
			"                     /translation=\"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEE\n" +
			"                     LFLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFW\n" +
			"                     SSSPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLP\n" +
			"                     RTNSGSSTKAMVLHR\"\n" +			
			"ORIGIN\n"+
			"       1 aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa\n"+
			"      61 aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa\n"+
			"     121 aa\n"+
			"//\n";	
		setBufferedReader(entryString);
		EntryReader reader = new GenbankEntryReader(bufferedReader,"test file");
		ValidationResult result = reader.read();
		Entry entry = reader.getEntry();
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for ( ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());			
		}		
		assertEquals(0, result.count(Severity.ERROR));
		StringWriter writer = new StringWriter();                      
		assertTrue(new GenbankEntryWriter(entry).write(writer));
		//System.out.print(writer.toString());
		assertEquals(entryString, writer.toString());
	}
}
