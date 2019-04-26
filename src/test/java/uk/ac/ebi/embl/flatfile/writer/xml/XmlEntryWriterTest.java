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
package uk.ac.ebi.embl.flatfile.writer.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblLineReader;
import uk.ac.ebi.embl.flatfile.reader.embl.RAReader;

public class XmlEntryWriterTest extends XmlWriterTest {
	
	public void testCDSEntry() throws IOException
	{
		String entryString = "ID   AAK83626; SV 1; linear; mRNA; STD; PLN; 801 BP.\n" + "XX\n" + "PA   AY049284.1\n" + "XX\n"
				+ "DT   10-AUG-2001 (Rel. 68, Created)\n" + "DT   16-APR-2005 (Rel. 83, Last updated, Version 2)\n" + "XX\n"
				+ "DE   Arabidopsis thaliana (thale cress) At1g31070/F17F8_1\n" + "XX\n" + "KW   FLI_CDNA.\n" + "XX\n"
				+ "OS   Arabidopsis thaliana (thale cress)\n" + "OC   Eukaryota; Viridiplantae; Streptophyta; Embryophyta; Tracheophyta;\n"
				+ "OC   Spermatophyta; Magnoliophyta; eudicotyledons; core eudicotyledons; rosids;\n"
				+ "OC   malvids; Brassicales; Brassicaceae; Camelineae; Arabidopsis.\n" + "OX   NCBI_TaxID=3702;\n" + "XX\n"
				+ "FH   Key             Location/Qualifiers\n" + "FH\n" + "FT   source          1..801\n"
				+ "FT                   /organism=\"Arabidopsis thaliana\"\n" + "FT                   /chromosome=\"1\"\n"
				+ "FT                   /mol_type=\"mRNA\"\n" + "FT                   /clone=\"RAFL07-18-C13(R13252)\"\n"
				+ "FT                   /db_xref=\"taxon:3702\"\n" + "FT   CDS             AY049284.1:993..1793\n"
				+ "FT                   /codon_start=1\n" + "FT                   /product=\"At1g31070/F17F8_1\"\n"
				+ "FT                   /note=\"UDP-N-acetylglucosamine pyrophosphorylase-like\n" + "FT                   protein\"\n"
				+ "FT                   /db_xref=\"GOA:Q94A81\"\n" + "FT                   /db_xref=\"HSSP:1JVG\"\n"
				+ "FT                   /db_xref=\"InterPro:IPR002618\"\n" + "FT                   /db_xref=\"UniProtKB/TrEMBL:Q94A81\"\n"
				+ "FT                   /protein_id=\"AAK83626.1\"\n"
				+ "FT                   /translation=\"METPFSLAKAPDGNGGVYAALKCSRLLEDMASRGIKYVDCYGVDN\n"
				+ "FT                   VLVRVADPTFLGYFIDKGAASAAKVVRKAYPQEQVGVFVRRGKGGPLTVVEYSELDQSM\n"
				+ "FT                   ASAINQRTGRLQYCWSNVCLHMFTLDFLNQVATGLEKDSVYHLAEKKIPSMNGYTMGLK\n"
				+ "FT                   LEQFIFDSFPYAPSTALFEVLREEEFAPVKNVNGSNFDTPESARLLVLRLHTRWVIAAG\n"
				+ "FT                   GFLTHSVPLYATGVEVSPLCSYAGENLEAICRGRTFHAPCEISL\"\n" + "XX\n"
				+ "SQ   Sequence 801 BP; 216 A; 144 C; 201 G; 240 T; 0 other; 1748495607 CRC32;\n"
				+ "     atggagacac ctttcagtct agctaaagct ccagatggta acggcggagt ctatgcagcg        60\n"
				+ "     ctaaagtgtt caaggctatt agaggatatg gcttctaggg ggattaaata cgtcgattgc       120\n"
				+ "     tatggcgttg acaatgtcct ggttcgagta gctgatccta cttttcttgg atacttcatc       180\n"
				+ "     gataaagggg ctgcttcggc tgcaaaagtt gtgcggaagg catatcctca agaacaggta       240\n"
				+ "     ggagtgtttg ttagaagagg taaaggagga ccgttgacag tagttgagta tagtgagctt       300\n"
				+ "     gatcagtcta tggcttctgc tattaatcaa cgaacaggac gtcttcaata ttgctggagt       360\n"
				+ "     aacgtgtgct tacacatgtt tactttagat ttccttaatc aagtagcgac cggcctagaa       420\n"
				+ "     aaagatagcg tgtaccattt ggcggagaag aagataccat ctatgaatgg atacacaatg       480\n"
				+ "     ggactaaaac tagaacaatt catttttgat tcgtttcctt atgctccttc aaccgcactt       540\n"
				+ "     tttgaggttt taagggaaga ggagtttgca ccagtgaaga atgttaacgg gtctaatttt       600\n"
				+ "     gatacaccgg agagtgcgag gcttttggtt ctaaggttac acacacgttg ggttatagca       660\n"
				+ "     gctggtggat ttctaacaca ttctgtgcct ttatatgcta ctggtgtaga ggtttcacct       720\n"
				+ "     ttgtgctcat acgccggaga aaatcttgaa gctatttgtc gaggaagaac gtttcatgca       780\n"
				+ "     ccttgtgaaa tttccctcta a                                                 801\n" + "//\n";

		BufferedReader bufferedReader = new BufferedReader(new StringReader(entryString));
		EntryReader reader = new EmblEntryReader(bufferedReader, EmblEntryReader.Format.CDS_FORMAT, null);
		ValidationResult result = reader.read();
		Entry entry = reader.getEntry();
		// Collection<ValidationMessage<Origin>> messages =
		// result.getMessages();
		// for ( ValidationMessage<Origin> message : messages) {
		// System.out.println(message.getMessage());
		// }
		StringWriter writer = new StringWriter();
		assertTrue(new XmlEntryWriter(entry).write(writer));
		//System.out.println(writer.toString());
		assertEquals(
				"<entry accession=\"AAK83626\" version=\"1\" entryVersion=\"2\" dataClass=\"STD\" taxonomicDivision=\"PLN\" moleculeType=\"mRNA\" sequenceLength=\"801\" topology=\"linear\" firstPublic=\"2001-08-10\" firstPublicRelease=\"68\" lastUpdated=\"2005-04-16\" lastUpdatedRelease=\"83\">\n"
						+ "	<description>Arabidopsis thaliana (thale cress) At1g31070/F17F8_1</description>\n"
						+ "	<keyword>FLI_CDNA</keyword>\n"
						+ "	<xref db=\"EMBL\" id=\"AY049284.1\"/>\n"
						+ "	<feature name=\"source\" location=\"1..801\">\n"
						+ "		<taxon scientificName=\"Arabidopsis thaliana\" commonName=\"thale cress\" taxId=\"3702\">\n"
						+ "			<lineage>\n"
						+ "				<taxon scientificName=\"Eukaryota\"/>\n"
						+ "				<taxon scientificName=\"Viridiplantae\"/>\n"
						+ "				<taxon scientificName=\"Streptophyta\"/>\n"
						+ "				<taxon scientificName=\"Embryophyta\"/>\n"
						+ "				<taxon scientificName=\"Tracheophyta\"/>\n"
						+ "				<taxon scientificName=\"Spermatophyta\"/>\n"
						+ "				<taxon scientificName=\"Magnoliophyta\"/>\n"
						+ "				<taxon scientificName=\"eudicotyledons\"/>\n"
						+ "				<taxon scientificName=\"core eudicotyledons\"/>\n"
						+ "				<taxon scientificName=\"rosids\"/>\n"
						+ "				<taxon scientificName=\"malvids\"/>\n"
						+ "				<taxon scientificName=\"Brassicales\"/>\n"
						+ "				<taxon scientificName=\"Brassicaceae\"/>\n"
						+ "				<taxon scientificName=\"Camelineae\"/>\n"
						+ "				<taxon scientificName=\"Arabidopsis\"/>\n"
						+ "			</lineage>\n"
						+ "		</taxon>\n"
						+ "		<qualifier name=\"organism\">\n"
						+ "			<value>\n"
						+ "Arabidopsis thaliana\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "		<qualifier name=\"chromosome\">\n"
						+ "			<value>\n"
						+ "1\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "		<qualifier name=\"clone\">\n"
						+ "			<value>\n"
						+ "RAFL07-18-C13(R13252)\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "	</feature>\n"
						+ "	<feature name=\"CDS\" location=\"AY049284.1:993..1793\">\n"
						+ "		<xref db=\"GOA\" id=\"Q94A81\"/>\n"
						+ "		<xref db=\"HSSP\" id=\"1JVG\"/>\n"
						+ "		<xref db=\"InterPro\" id=\"IPR002618\"/>\n"
						+ "		<xref db=\"UniProtKB/TrEMBL\" id=\"Q94A81\"/>\n"
						+ "		<qualifier name=\"codon_start\">\n"
						+ "			<value>\n"
						+ "1\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "		<qualifier name=\"product\">\n"
						+ "			<value>\n"
						+ "At1g31070/F17F8_1\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "		<qualifier name=\"note\">\n"
						+ "			<value>\n"
						+ "UDP-N-acetylglucosamine pyrophosphorylase-like protein\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "		<qualifier name=\"protein_id\">\n"
						+ "			<value>\n"
						+ "AAK83626.1\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "		<qualifier name=\"translation\">\n"
						+ "			<value>\n"
						+ "METPFSLAKAPDGNGGVYAALKCSRLLEDMASRGIKYVDCYGVDNVLVRVADPTFLGYFIDKGAASAAKVVRKAYPQEQV\n"
						+ "GVFVRRGKGGPLTVVEYSELDQSMASAINQRTGRLQYCWSNVCLHMFTLDFLNQVATGLEKDSVYHLAEKKIPSMNGYTM\n"
						+ "GLKLEQFIFDSFPYAPSTALFEVLREEEFAPVKNVNGSNFDTPESARLLVLRLHTRWVIAAGGFLTHSVPLYATGVEVSP\n"
						+ "LCSYAGENLEAICRGRTFHAPCEISL\n"
						+ "			</value>\n"
						+ "		</qualifier>\n"
						+ "	</feature>\n"
						+ "	<sequence>\n"
						+ "atggagacacctttcagtctagctaaagctccagatggtaacggcggagtctatgcagcg\n"
						+ "ctaaagtgttcaaggctattagaggatatggcttctagggggattaaatacgtcgattgc\n"
						+ "tatggcgttgacaatgtcctggttcgagtagctgatcctacttttcttggatacttcatc\n"
						+ "gataaaggggctgcttcggctgcaaaagttgtgcggaaggcatatcctcaagaacaggta\n"
						+ "ggagtgtttgttagaagaggtaaaggaggaccgttgacagtagttgagtatagtgagctt\n"
						+ "gatcagtctatggcttctgctattaatcaacgaacaggacgtcttcaatattgctggagt\n"
						+ "aacgtgtgcttacacatgtttactttagatttccttaatcaagtagcgaccggcctagaa\n"
						+ "aaagatagcgtgtaccatttggcggagaagaagataccatctatgaatggatacacaatg\n"
						+ "ggactaaaactagaacaattcatttttgattcgtttccttatgctccttcaaccgcactt\n"
						+ "tttgaggttttaagggaagaggagtttgcaccagtgaagaatgttaacgggtctaatttt\n"
						+ "gatacaccggagagtgcgaggcttttggttctaaggttacacacacgttgggttatagca\n"
						+ "gctggtggatttctaacacattctgtgcctttatatgctactggtgtagaggtttcacct\n"
						+ "ttgtgctcatacgccggagaaaatcttgaagctatttgtcgaggaagaacgtttcatgca\n"
						+ "ccttgtgaaatttccctctaa\n"
						+ "	</sequence>\n"
						+ "</entry>\n"

				, writer.toString());

	}

    public void testEntry() throws IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
		entry.setIdLineSequenceLength(entry.getSequence().getLength());
		entry.setPrimaryAccession("DP000153");
	    entry.getSequence().setVersion(2);
	    entry.setVersion(3);
	    entry.setDataClass("CON");
	    entry.setDivision("MAM");
	    entry.getSequence().setMoleculeType("genomic RNA");	    
	    entry.getSequence().setTopology(Topology.LINEAR);
		entry.setFirstPublic(FlatFileUtils.getDay("06-SEP-2006"));
		entry.setLastUpdated(FlatFileUtils.getDay("05-SEP-2006"));            
		entry.setFirstPublicRelease(1);
		entry.setLastUpdatedRelease(2);
		entry.addSecondaryAccession(new Text("A00001"));
		entry.addSecondaryAccession(new Text("A00002"));
		entry.addSecondaryAccession(new Text("A00003"));
		entry.addSecondaryAccession(new Text("A00004"));
		entry.addSecondaryAccession(new Text("A00005"));
		entry.addSecondaryAccession(new Text("A00006"));
		entry.addSecondaryAccession(new Text("A00007"));
		entry.addSecondaryAccession(new Text("A00008"));
		entry.addKeyword(new Text("keyword1"));
		entry.addKeyword(new Text("keyword2"));
		entry.addProjectAccession(new Text("3444"));
		entry.addProjectAccession(new Text("3442"));
		entry.addProjectAccession(new Text("3441"));
		entry.setDescription(new Text("This is the descripion."));
		entry.setComment(new Text("This is\na comment."));
		
		ReferenceFactory referenceFactory = new ReferenceFactory();
		Article article = referenceFactory.createArticle(
				"Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor",
		"J. Biol. Chem.");
		article.setVolume("273");
		article.setIssue("48");
		article.setFirstPage("32281");
		article.setLastPage("32287");
		article.setYear(FlatFileUtils.getDay("10-SEP-1998"));
		EntryFactory entryFactory = new EntryFactory();
		article.addXRef(entryFactory.createXRef("UniProtKB", "A00001"));
		article.addXRef(entryFactory.createXRef("UniProtKB", "A00002"));
		EmblLineReader lineReader = new EmblLineReader();
		lineReader.getCache().setPublication(article);
		lineReader.setReader(new BufferedReader(new StringReader(
				"RA   Antonellis A., Ayele K., Benjamin B., Blakesley R.W., Boakye A., Bouffard G.G., Brinkley C., Brooks S., Chu G., Coleman H., Engle J., Gestole M., Greene A., Guan X., Gupta J., Haghighi P., Han J., Hansen N., Ho S.-L., Hu P., Hunter G., Hurle B., Idol J.R., Kwong P., Laric P., Larson S., Lee-Lin S.-Q., Legaspi R., Madden M., Maduro Q.L., Maduro V.B., Margulies E.H., Masiello C., Maskeri B., McDowell J., Mojidi H.A., Mullikin J.C., Oestreicher J.S., Park M., Portnoy M.E., Prasad A., Puri O., Reddix-Dugue N., Schandler K., Schueler M.G., Sison C., Stantripop S., Stephen E., Taye A., Thomas J.W., Thomas P.J., Tsipouri V., Ung L., Vogt J.L., Wetherby K.D., Young A., Green E.D.")));
		lineReader.readLine();
		ValidationResult result = (new RAReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		article.setConsortium("Google consortium");
		Reference reference = referenceFactory.createReference(article, 1);
		reference.setComment("reference comment");
		reference.getLocations().addLocation((new LocationFactory()).createLocalRange(32L, 123L));
		reference.getLocations().addLocation((new LocationFactory()).createLocalRange(322L, 1323L));
		entry.addReference(reference);	
		
		entry.addXRef(entryFactory.createXRef("UniProtKB", "X00001"));
		entry.addXRef(entryFactory.createXRef("UniProtKB", "X00002"));		
		
	    StringWriter writer = new StringWriter();
	    assertTrue(new XmlEntryWriter(entry).write(writer));
	    //System.out.print(writer.toString());
	    assertEquals(
	    		"<entry accession=\"DP000153\" version=\"2\" entryVersion=\"3\" dataClass=\"CON\" taxonomicDivision=\"MAM\" moleculeType=\"genomic RNA\" sequenceLength=\"2\" topology=\"linear\" firstPublic=\"2006-09-06\" firstPublicRelease=\"1\" lastUpdated=\"2006-09-05\" lastUpdatedRelease=\"2\">\n" + 
	    		"	<secondaryAccession>A00001</secondaryAccession>\n" +
	    		"	<secondaryAccession>A00002</secondaryAccession>\n" +
	    		"	<secondaryAccession>A00003</secondaryAccession>\n" +
	    		"	<secondaryAccession>A00004</secondaryAccession>\n" +
	    		"	<secondaryAccession>A00005</secondaryAccession>\n" +
	    		"	<secondaryAccession>A00006</secondaryAccession>\n" +
	    		"	<secondaryAccession>A00007</secondaryAccession>\n" +
	    		"	<secondaryAccession>A00008</secondaryAccession>\n" +
	    		"	<projectAccession>3444</projectAccession>\n" +
	    		"	<projectAccession>3442</projectAccession>\n" +
	    		"	<projectAccession>3441</projectAccession>\n" +
	    		"	<description>This is the descripion.</description>\n" +
	    		"	<comment>\n" +
	    		"This is\n" +
	    		"a comment.\n" +
	    		"	</comment>\n" +
	    		"	<keyword>keyword1</keyword>\n" +
	    		"	<keyword>keyword2</keyword>\n" +
	    		"	<reference type=\"article\" number=\"1\" location=\"32-123, 322-1323\">\n" +
	    		"		<title>Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor</title>\n" +
	    		"		<author>Antonellis A.</author>\n" +
	    		"		<author>Ayele K.</author>\n" +
	    		"		<author>Benjamin B.</author>\n" +
	    		"		<author>Blakesley R.W.</author>\n" +
	    		"		<author>Boakye A.</author>\n" +
	    		"		<author>Bouffard G.G.</author>\n" +
	    		"		<author>Brinkley C.</author>\n" +
	    		"		<author>Brooks S.</author>\n" +
	    		"		<author>Chu G.</author>\n" +
	    		"		<author>Coleman H.</author>\n" +
	    		"		<author>Engle J.</author>\n" +
	    		"		<author>Gestole M.</author>\n" +
	    		"		<author>Greene A.</author>\n" +
	    		"		<author>Guan X.</author>\n" +
	    		"		<author>Gupta J.</author>\n" +
	    		"		<author>Haghighi P.</author>\n" +
	    		"		<author>Han J.</author>\n" +
	    		"		<author>Hansen N.</author>\n" +
	    		"		<author>Ho S.-L.</author>\n" +
	    		"		<author>Hu P.</author>\n" +
	    		"		<author>Hunter G.</author>\n" +
	    		"		<author>Hurle B.</author>\n" +
	    		"		<author>Idol J.R.</author>\n" +
	    		"		<author>Kwong P.</author>\n" +
	    		"		<author>Laric P.</author>\n" +
	    		"		<author>Larson S.</author>\n" +
	    		"		<author>Lee-Lin S.-Q.</author>\n" +
	    		"		<author>Legaspi R.</author>\n" +
	    		"		<author>Madden M.</author>\n" +
	    		"		<author>Maduro Q.L.</author>\n" +
	    		"		<author>Maduro V.B.</author>\n" +
	    		"		<author>Margulies E.H.</author>\n" +
	    		"		<author>Masiello C.</author>\n" +
	    		"		<author>Maskeri B.</author>\n" +
	    		"		<author>McDowell J.</author>\n" +
	    		"		<author>Mojidi H.A.</author>\n" +
	    		"		<author>Mullikin J.C.</author>\n" +
	    		"		<author>Oestreicher J.S.</author>\n" +
	    		"		<author>Park M.</author>\n" +
	    		"		<author>Portnoy M.E.</author>\n" +
	    		"		<author>Prasad A.</author>\n" +
	    		"		<author>Puri O.</author>\n" +
	    		"		<author>Reddix-Dugue N.</author>\n" +
	    		"		<author>Schandler K.</author>\n" +
	    		"		<author>Schueler M.G.</author>\n" +
	    		"		<author>Sison C.</author>\n" +
	    		"		<author>Stantripop S.</author>\n" +
	    		"		<author>Stephen E.</author>\n" +
	    		"		<author>Taye A.</author>\n" +
	    		"		<author>Thomas J.W.</author>\n" +
	    		"		<author>Thomas P.J.</author>\n" +
	    		"		<author>Tsipouri V.</author>\n" +
	    		"		<author>Ung L.</author>\n" +
	    		"		<author>Vogt J.L.</author>\n" +
	    		"		<author>Wetherby K.D.</author>\n" +
	    		"		<author>Young A.</author>\n" +
	    		"		<author>Green E.D.</author>\n" +
	    		"		<consortium>Google consortium</consortium>\n" +
	    		"		<journal>J. Biol. Chem.</journal>\n" +
	    		"		<year>1998</year>\n" +
	    		"		<volume>273</volume>\n" +
	    		"		<issue>48</issue>\n" +
	    		"		<firstPage>32281</firstPage>\n" +
	    		"		<lastPage>32287</lastPage>\n" +
	    		"		<comment>reference comment</comment>\n" +
	    		"		<referenceLocation>\n" +
	    		"J. Biol. Chem. 273(48):32281-32287(1998).\n" +
	    		"		</referenceLocation>\n" +
	    		"		<xref db=\"UniProtKB\" id=\"A00001\"/>\n" +
	    		"		<xref db=\"UniProtKB\" id=\"A00002\"/>\n" +
	    		"	</reference>\n" +
	    		"	<xref db=\"UniProtKB\" id=\"X00001\"/>\n" +
	    		"	<xref db=\"UniProtKB\" id=\"X00002\"/>\n" +
	    		"	<sequence>\n" +
	    		"aa\n" +
	    		"	</sequence>\n" +
	    		"</entry>\n",
	    		writer.toString());
	}

    /**
     * tests read and write with the old "to the EMBL/Genbank/DDBJ" submission type
     * @throws IOException
     */
	public void testMasterEntryOldSubmissionType() throws IOException {
		String entryString =
			"ID   AAAA00000000; SV 2; linear; genomic DNA; SET; PLN; 53326 SQ.\n" +
			"XX\n" +
			"AC   AAAA00000000; AAAA02000000;\n" +
			"XX\n" +
			"PR   Project:234455; Project:344332;\n" +
			"XX\n" +
			"DE   Oryza sativa Indica Group, WGS project AAAA00000000 data, 410679186\n" +
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
			"RL   Submitted (04-JAN-2002) to the EMBL/GenBank/DDBJ databases.\n" +
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
			"RL   Submitted (12-SEP-2003) to the EMBL/GenBank/DDBJ databases.\n" +
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
			"XX\n" +
			"TPA  A398081-A401163\n" +
			"//\n";
		
		BufferedReader bufferedReader = new BufferedReader(new StringReader(entryString));
		EntryReader reader = new EmblEntryReader(bufferedReader, 
				EmblEntryReader.Format.MASTER_FORMAT, null);
		ValidationResult result = reader.read();
		Entry entry = reader.getEntry();
		//Collection<ValidationMessage<Origin>> messages = result.getMessages();
		//for ( ValidationMessage<Origin> message : messages) {
		//	System.out.println(message.getMessage());			
		//}
      assertEquals(1, result.count(Severity.ERROR));
      assertEquals("Master entry must have a sequence", "FF.12", result.getMessages().iterator().next().getMessageKey());
	    StringWriter writer = new StringWriter();
	    assertTrue(new XmlEntryWriter(entry).write(writer));
	    //System.out.print(writer.toString());
	    assertEquals(
                masterOutputString,
	    		writer.toString());
	}

    /**
     * checks read and write of embl entry with the new "to the INSDC" in submission ref
     * @throws IOException
     */
	public void testMasterEntryNewSubmissionType() throws IOException {
		String entryString =
			"ID   AAAA00000000; SV 2; linear; genomic DNA; SET; PLN; 53326 SQ.\n" +
			"XX\n" +
			"AC   AAAA00000000; AAAA02000000;\n" +
			"XX\n" +
			"PR   Project:234455; Project:344332;\n" +
			"XX\n" +
			"DE   Oryza sativa Indica Group, WGS project AAAA00000000 data, 410679186\n" +
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
			"XX\n" +
			"TPA  A398081-A401163\n" +
			"//\n";

		BufferedReader bufferedReader = new BufferedReader(new StringReader(entryString));
		EntryReader reader = new EmblEntryReader(bufferedReader,
				EmblEntryReader.Format.MASTER_FORMAT, null);
		ValidationResult result = reader.read();
		Entry entry = reader.getEntry();
//		Collection<ValidationMessage<Origin>> messages = result.getMessages();
//		for ( ValidationMessage<Origin> message : messages) {
//			System.out.println(message.getMessage());
//		}
      assertEquals(1, result.count(Severity.ERROR));
      assertEquals("Master entry must have a sequence", "FF.12", result.getMessages().iterator().next().getMessageKey());
	    StringWriter writer = new StringWriter();
	    assertTrue(new XmlEntryWriter(entry).write(writer));
	    //System.out.print(writer.toString());
	    assertEquals(
                masterOutputString,
	    		writer.toString());
	}

    public String masterOutputString =
            "<entry accession=\"AAAA00000000\" version=\"2\" dataClass=\"SET\" taxonomicDivision=\"PLN\" moleculeType=\"genomic DNA\" sequenceLength=\"53326\" topology=\"linear\">\n" +
            "	<secondaryAccession>AAAA02000000</secondaryAccession>\n" +
            "	<projectAccession>234455</projectAccession>\n" +
            "	<projectAccession>344332</projectAccession>\n" +
            "	<description>Oryza sativa Indica Group, WGS project AAAA00000000 data, 410679186 basepairs.</description>\n" +
            "	<reference type=\"article\" number=\"1\">\n" +
            "		<title>The Genomes of Oryza sativa: A History of Duplications</title>\n" +
            "		<author>Yu J.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Lin W.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Li H.</author>\n" +
            "		<author>Zhou J.</author>\n" +
            "		<author>Ni P.</author>\n" +
            "		<author>Dong W.</author>\n" +
            "		<author>Hu S.</author>\n" +
            "		<author>Zeng C.</author>\n" +
            "		<author>Zhang J.</author>\n" +
            "		<author>Zhang Y.</author>\n" +
            "		<author>Li R.</author>\n" +
            "		<author>Xu Z.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Li X.</author>\n" +
            "		<author>Zheng H.</author>\n" +
            "		<author>Cong L.</author>\n" +
            "		<author>Lin L.</author>\n" +
            "		<author>Yin J.</author>\n" +
            "		<author>Geng J.</author>\n" +
            "		<author>Li G.</author>\n" +
            "		<author>Shi J.</author>\n" +
            "		<author>Liu J.</author>\n" +
            "		<author>Lv H.</author>\n" +
            "		<author>Li J.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Deng Y.</author>\n" +
            "		<author>Ran L.</author>\n" +
            "		<author>Shi X.</author>\n" +
            "		<author>Wang X.</author>\n" +
            "		<author>Wu Q.</author>\n" +
            "		<author>Li C.</author>\n" +
            "		<author>Ren X.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Wang X.</author>\n" +
            "		<author>Li D.</author>\n" +
            "		<author>Liu D.</author>\n" +
            "		<author>Zhang X.</author>\n" +
            "		<author>Ji Z.</author>\n" +
            "		<author>Zhao W.</author>\n" +
            "		<author>Sun Y.</author>\n" +
            "		<author>Zhang Z.</author>\n" +
            "		<author>Bao J.</author>\n" +
            "		<author>Han Y.</author>\n" +
            "		<author>Dong L.</author>\n" +
            "		<author>Ji J.</author>\n" +
            "		<author>Chen P.</author>\n" +
            "		<author>Wu S.</author>\n" +
            "		<author>Liu J.</author>\n" +
            "		<author>Xiao Y.</author>\n" +
            "		<author>Bu D.</author>\n" +
            "		<author>Tan J.</author>\n" +
            "		<author>Yang L.</author>\n" +
            "		<author>Ye C.</author>\n" +
            "		<author>Zhang J.</author>\n" +
            "		<author>Xu J.</author>\n" +
            "		<author>Zhou Y.</author>\n" +
            "		<author>Yu Y.</author>\n" +
            "		<author>Zhang B.</author>\n" +
            "		<author>Zhuang S.</author>\n" +
            "		<author>Wei H.</author>\n" +
            "		<author>Liu B.</author>\n" +
            "		<author>Lei M.</author>\n" +
            "		<author>Yu H.</author>\n" +
            "		<author>Li Y.</author>\n" +
            "		<author>Xu H.</author>\n" +
            "		<author>Wei S.</author>\n" +
            "		<author>He X.</author>\n" +
            "		<author>Fang L.</author>\n" +
            "		<author>Zhang Z.</author>\n" +
            "		<author>Zhang Y.</author>\n" +
            "		<author>Huang X.</author>\n" +
            "		<author>Su Z.</author>\n" +
            "		<author>Tong W.</author>\n" +
            "		<author>Li J.</author>\n" +
            "		<author>Tong Z.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Ye J.</author>\n" +
            "		<author>Wang L.</author>\n" +
            "		<author>Fang L.</author>\n" +
            "		<author>Lei T.</author>\n" +
            "		<author>Chen C.</author>\n" +
            "		<author>Chen H.</author>\n" +
            "		<author>Xu Z.</author>\n" +
            "		<author>Li H.</author>\n" +
            "		<author>Huang H.</author>\n" +
            "		<author>Zhang F.</author>\n" +
            "		<author>Xu H.</author>\n" +
            "		<author>Li N.</author>\n" +
            "		<author>Zhao C.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Dong L.</author>\n" +
            "		<author>Huang Y.</author>\n" +
            "		<author>Li L.</author>\n" +
            "		<author>Xi Y.</author>\n" +
            "		<author>Qi Q.</author>\n" +
            "		<author>Li W.</author>\n" +
            "		<author>Zhang B.</author>\n" +
            "		<author>Hu W.</author>\n" +
            "		<author>Zhang Y.</author>\n" +
            "		<author>Tian X.</author>\n" +
            "		<author>Jiao Y.</author>\n" +
            "		<author>Liang X.</author>\n" +
            "		<author>Jin J.</author>\n" +
            "		<author>Gao L.</author>\n" +
            "		<author>Zheng W.</author>\n" +
            "		<author>Hao B.</author>\n" +
            "		<author>Liu S.</author>\n" +
            "		<author>Wang W.</author>\n" +
            "		<author>Yuan L.</author>\n" +
            "		<author>Cao M.</author>\n" +
            "		<author>McDermott J.</author>\n" +
            "		<author>Samudrala R.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Wong G.K.</author>\n" +
            "		<author>Yang H.</author>\n" +
            "		<journal>PLoS Biol.</journal>\n" +
            "		<year>2005</year>\n" +
            "		<volume>3</volume>\n" +
            "		<issue>2</issue>\n" +
            "		<firstPage>E38</firstPage>\n" +
            "		<lastPage>E38</lastPage>\n" +     
            "		<referenceLocation>\n" +
            "PLoS Biol. 3(2):E38-E38(2005).\n" +
            "		</referenceLocation>\n" +
            "		<xref db=\"DOI\" id=\"10.1371/journal.pbio.0030038\"/>\n" +
            "		<xref db=\"PUBMED\" id=\"15685292\"/>\n" +
            "	</reference>\n" +
            "	<reference type=\"submission\" number=\"2\">\n" +
            "		<author>Yu J.</author>\n" +
            "		<author>Hu S.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Wong K.-S.G.</author>\n" +
            "		<author>Liu B.</author>\n" +
            "		<author>Deng Y.</author>\n" +
            "		<author>Dai L.</author>\n" +
            "		<author>Zhou Y.</author>\n" +
            "		<author>Zhang X.</author>\n" +
            "		<author>Cao M.</author>\n" +
            "		<author>Liu J.</author>\n" +
            "		<author>Sun J.</author>\n" +
            "		<author>Tang J.</author>\n" +
            "		<author>Chen Y.</author>\n" +
            "		<author>Huang X.</author>\n" +
            "		<author>Lin W.</author>\n" +
            "		<author>Ye C.</author>\n" +
            "		<author>Tong W.</author>\n" +
            "		<author>Cong L.</author>\n" +
            "		<author>Geng J.</author>\n" +
            "		<author>Han Y.</author>\n" +
            "		<author>Li L.</author>\n" +
            "		<author>Li W.</author>\n" +
            "		<author>Hu G.</author>\n" +
            "		<author>Huang X.</author>\n" +
            "		<author>Li W.</author>\n" +
            "		<author>Li J.</author>\n" +
            "		<author>Liu Z.</author>\n" +
            "		<author>Li L.</author>\n" +
            "		<author>Liu J.</author>\n" +
            "		<author>Qi Q.</author>\n" +
            "		<author>Liu J.</author>\n" +
            "		<author>Li L.</author>\n" +
            "		<author>Wang X.</author>\n" +
            "		<author>Lu H.</author>\n" +
            "		<author>Wu T.</author>\n" +
            "		<author>Zhu M.</author>\n" +
            "		<author>Ni P.</author>\n" +
            "		<author>Han H.</author>\n" +
            "		<author>Dong W.</author>\n" +
            "		<author>Ren X.</author>\n" +
            "		<author>Feng X.</author>\n" +
            "		<author>Cui P.</author>\n" +
            "		<author>Li X.</author>\n" +
            "		<author>Wang H.</author>\n" +
            "		<author>Xu X.</author>\n" +
            "		<author>Zhai W.</author>\n" +
            "		<author>Xu Z.</author>\n" +
            "		<author>Zhang J.</author>\n" +
            "		<author>He S.</author>\n" +
            "		<author>Zhang J.</author>\n" +
            "		<author>Xu J.</author>\n" +
            "		<author>Zhang K.</author>\n" +
            "		<author>Zheng X.</author>\n" +
            "		<author>Dong J.</author>\n" +
            "		<author>Zeng W.</author>\n" +
            "		<author>Tao L.</author>\n" +
            "		<author>Chen X.</author>\n" +
            "		<author>He J.</author>\n" +
            "		<author>Liu D.</author>\n" +
            "		<author>Tian W.</author>\n" +
            "		<author>Tian C.</author>\n" +
            "		<author>Xia H.</author>\n" +
            "		<author>Li G.</author>\n" +
            "		<author>Gao H.</author>\n" +
            "		<author>Li P.</author>\n" +
            "		<author>Chen W.</author>\n" +
            "		<author>Wang X.</author>\n" +
            "		<author>Zhang Y.</author>\n" +
            "		<author>Hu J.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Liu S.</author>\n" +
            "		<author>Yang J.</author>\n" +
            "		<author>Zhang G.</author>\n" +
            "		<author>Bao Q.</author>\n" +
            "		<author>Xiong Y.</author>\n" +
            "		<author>Li Z.</author>\n" +
            "		<author>Mao L.</author>\n" +
            "		<author>Zhou C.</author>\n" +
            "		<author>Chen R.</author>\n" +
            "		<author>Zhu Z.</author>\n" +
            "		<author>Hao B.</author>\n" +
            "		<author>Zheng W.</author>\n" +
            "		<author>Chen S.</author>\n" +
            "		<author>Guo W.</author>\n" +
            "		<author>Li G.</author>\n" +
            "		<author>Liu S.</author>\n" +
            "		<author>Huang G.</author>\n" +
            "		<author>Tao M.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Zhu L.</author>\n" +
            "		<author>Yuan L.</author>\n" +
            "		<author>Yang H.</author>\n" +
    		"		<submissionDate>2002-01-04</submissionDate>\n" +
            "		<referenceLocation>\n" +
            "Submitted (04-JAN-2002) to the INSDC.\n" +
            "Beijing Genomics Institute/Center of Genomics &amp; Bioinformatics, Institute of\n" +
            "Genomics, Chinese Academy of Sciences, Beijing Airport Industrial Zone B6,\n" +
            "Beijing, Beijing 101300, P.R.China\n" +
            "		</referenceLocation>\n" +
            "	</reference>\n" +
            "	<reference type=\"submission\" number=\"3\">\n" +
            "		<author>Yu J.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Lin W.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Li H.</author>\n" +
            "		<author>Zhou J.</author>\n" +
            "		<author>Ni P.</author>\n" +
            "		<author>Dong W.</author>\n" +
            "		<author>Hu S.</author>\n" +
            "		<author>Zeng C.</author>\n" +
            "		<author>Zhang J.</author>\n" +
            "		<author>Zhang Y.</author>\n" +
            "		<author>Li R.</author>\n" +
            "		<author>Xu Z.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Li X.</author>\n" +
            "		<author>Zheng H.</author>\n" +
            "		<author>Cong L.</author>\n" +
            "		<author>Lin L.</author>\n" +
            "		<author>Yin J.</author>\n" +
            "		<author>Geng J.</author>\n" +
            "		<author>Li G.</author>\n" +
            "		<author>Shi J.</author>\n" +
            "		<author>Liu J.</author>\n" +
            "		<author>Lv H.</author>\n" +
            "		<author>Li J.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Deng Y.</author>\n" +
            "		<author>Ran L.</author>\n" +
            "		<author>Shi X.</author>\n" +
            "		<author>Wang X.</author>\n" +
            "		<author>Wu Q.</author>\n" +
            "		<author>Li C.</author>\n" +
            "		<author>Ren X.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Wang X.</author>\n" +
            "		<author>Li D.</author>\n" +
            "		<author>Liu D.</author>\n" +
            "		<author>Zhang X.</author>\n" +
            "		<author>Ji Z.</author>\n" +
            "		<author>Zhao W.</author>\n" +
            "		<author>Sun Y.</author>\n" +
            "		<author>Zhang Z.</author>\n" +
            "		<author>Bao J.</author>\n" +
            "		<author>Han Y.</author>\n" +
            "		<author>Dong L.</author>\n" +
            "		<author>Ji J.</author>\n" +
            "		<author>Chen P.</author>\n" +
            "		<author>Wu S.</author>\n" +
            "		<author>Liu J.</author>\n" +
            "		<author>Xiao Y.</author>\n" +
            "		<author>Bu D.</author>\n" +
            "		<author>Tan J.</author>\n" +
            "		<author>Yang L.</author>\n" +
            "		<author>Ye C.</author>\n" +
            "		<author>Zhang J.</author>\n" +
            "		<author>Xu J.</author>\n" +
            "		<author>Zhou Y.</author>\n" +
            "		<author>Yu Y.</author>\n" +
            "		<author>Zhang B.</author>\n" +
            "		<author>Zhuang S.</author>\n" +
            "		<author>Wei H.</author>\n" +
            "		<author>Liu B.</author>\n" +
            "		<author>Lei M.</author>\n" +
            "		<author>Yu H.</author>\n" +
            "		<author>Li Y.</author>\n" +
            "		<author>Xu H.</author>\n" +
            "		<author>Wei S.</author>\n" +
            "		<author>He X.</author>\n" +
            "		<author>Fang L.</author>\n" +
            "		<author>Zhang Z.</author>\n" +
            "		<author>Zhang Y.</author>\n" +
            "		<author>Huang X.</author>\n" +
            "		<author>Su Z.</author>\n" +
            "		<author>Tong W.</author>\n" +
            "		<author>Li J.</author>\n" +
            "		<author>Tong Z.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Ye J.</author>\n" +
            "		<author>Wang L.</author>\n" +
            "		<author>Fang L.</author>\n" +
            "		<author>Lei T.</author>\n" +
            "		<author>Chen C.</author>\n" +
            "		<author>Chen H.</author>\n" +
            "		<author>Xu Z.</author>\n" +
            "		<author>Li H.</author>\n" +
            "		<author>Huang H.</author>\n" +
            "		<author>Zhang F.</author>\n" +
            "		<author>Xu H.</author>\n" +
            "		<author>Li N.</author>\n" +
            "		<author>Zhao C.</author>\n" +
            "		<author>Li S.</author>\n" +
            "		<author>Dong L.</author>\n" +
            "		<author>Huang Y.</author>\n" +
            "		<author>Li L.</author>\n" +
            "		<author>Xi Y.</author>\n" +
            "		<author>Qi Q.</author>\n" +
            "		<author>Li W.</author>\n" +
            "		<author>Zhang B.</author>\n" +
            "		<author>Hu W.</author>\n" +
            "		<author>Zhang Y.</author>\n" +
            "		<author>Zheng W.</author>\n" +
            "		<author>Hao B.</author>\n" +
            "		<author>Liu S.</author>\n" +
            "		<author>Wang W.</author>\n" +
            "		<author>Yuan L.</author>\n" +
            "		<author>Cao M.L.</author>\n" +
            "		<author>McDermott J.</author>\n" +
            "		<author>Samudrala R.</author>\n" +
            "		<author>Wang J.</author>\n" +
            "		<author>Wong G.K.-S.</author>\n" +
            "		<author>Yang H.</author>\n" +
    		"		<submissionDate>2003-09-12</submissionDate>\n" +
            "		<referenceLocation>\n" +
            "Submitted (12-SEP-2003) to the INSDC.\n" +
            "Beijing Institute of Genomics, Chinese Academy of Sciences, Beijing Airport\n" +
            "Industrial Zone B6, Beijing, Beijing 101300, P.R.China\n" +
            "		</referenceLocation>\n" +
            "	</reference>\n" +
            "	<xref db=\"ENA-WGS\" id=\"AAAA02000001-AAAA02050231\"/>\n" +
            "	<xref db=\"ENA-CON\" id=\"CH398081-CH401163\"/>\n" +
            "	<xref db=\"ENA-CON\" id=\"CM000126-CM000137\"/>\n" +
            "	<xref db=\"ENA-TPA\" id=\"A398081-A401163\"/>\n" +
            "	<feature name=\"source\" location=\"1..53326\">\n" +
            "		<taxon scientificName=\"Oryza sativa Indica Group\" taxId=\"39946\">\n" +
            "			<lineage>\n" +
            "				<taxon scientificName=\"Eukaryota\"/>\n" +
            "				<taxon scientificName=\"Viridiplantae\"/>\n" +
            "				<taxon scientificName=\"Streptophyta\"/>\n" +
            "				<taxon scientificName=\"Embryophyta\"/>\n" +
            "				<taxon scientificName=\"Tracheophyta\"/>\n" +
            "				<taxon scientificName=\"Spermatophyta\"/>\n" +
            "				<taxon scientificName=\"Magnoliophyta\"/>\n" +
            "				<taxon scientificName=\"Liliopsida\"/>\n" +
            "				<taxon scientificName=\"Poales\"/>\n" +
            "				<taxon scientificName=\"Poaceae\"/>\n" +
            "				<taxon scientificName=\"BEP clade\"/>\n" +
            "				<taxon scientificName=\"Ehrhartoideae\"/>\n" +
            "				<taxon scientificName=\"Oryzeae\"/>\n" +
            "				<taxon scientificName=\"Oryza\"/>\n" +
            "			</lineage>\n" +
            "		</taxon>\n" +
            "		<qualifier name=\"organism\">\n" +
            "			<value>\n" +
            "Oryza sativa Indica Group\n" +
            "			</value>\n" +
            "		</qualifier>\n" +
            "	</feature>\n" +
            "</entry>\n";
}
