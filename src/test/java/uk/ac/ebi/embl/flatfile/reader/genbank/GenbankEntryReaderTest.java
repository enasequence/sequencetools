/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblReaderTest;
import uk.ac.ebi.embl.flatfile.writer.genbank.GenbankEntryWriter;

public class GenbankEntryReaderTest extends EmblReaderTest {

  public void testRead_Entry() throws IOException {
    String entryString =
        "LOCUS       DP000153                 122 bp    mRNA    linear   MAM 10-SEP-1998\n"
            + "DEFINITION  Cloning and characterization of a cDNA encoding a novel subtype of\n"
            + "            rat thyrotropin-releasing hormone receptor.\n"
            + "ACCESSION   DP000153\n"
            + "VERSION     DP000153.1\n"
            + "KEYWORDS    .\n"
            + "SOURCE      Mus musculus\n"
            + "  ORGANISM  Mus musculus\n"
            + "            Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi;\n"
            + "            Mammalia; Eutheria; Euarchontoglires; Glires; Rodentia;\n"
            + "            Sciurognathi; Muroidea; Muridae; Murinae; Mus.\n"
            + "REFERENCE   1\n"
            + "  AUTHORS   Okazaki,Y., Furuno,M., Kasukawa,T., Adachi,J., Bono,H., Kondo,S.,\n"
            + "            Nikaido,I., Osato,N., Saito,R., Suzuki,H., Yamanaka,I., Rogers,J.,\n"
            + "            Birney,E. and Hayashizaki,Y.\n"
            + "  CONSRTM   Google consortium\n"
            + "  TITLE     Cloning and characterization of a cDNA encoding a novel subtype of\n"
            + "            rat thyrotropin-releasing hormone receptor\n"
            + "  JOURNAL   J. Biol. Chem. 273(48),32281-32287(1998)\n"
            + "  REMARK    reference comment\n"
            + "COMMENT     Cloning\n"
            + "            and characterization\n"
            + "            of a cDNA encoding a novel subtype of rat\n"
            + "            thyrotropin-releasing hormone receptor\n"
            + "PRIMARY     TPA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n"
            + "            1-426               AC004528.1         18665-19090         c\n"
            + "            6-546               AC004529.6         45665-98790         c\n"
            + "            1-426               AC004528.1         18665-19090         c\n"
            + "            6-546               AC004529.6         45665-98790         c\n"
            + "FEATURES             Location/Qualifiers\n"
            + "     source          1..353\n"
            + "                     /organism=\"Mus musculus\"\n"
            + "                     /mol_type=\"mRNA\"\n"
            + "                     /db_xref=\"taxon:10090\"\n"
            + "     gene            order(AF133070.1:<1..1930,AF133071.1:1..940,\n"
            + "                     AF133072.1:1..1365,AF133073.1:1..939,AF133074.1:1..1759,\n"
            + "                     AF133075.1:1..733,AF133076.1:1..681,AF133077.1:1..180,\n"
            + "                     AF133078.1:1..495,AF133079.1:1..836,AF133080.1:1..269,\n"
            + "                     AF133081.1:1..898,AF133082.1:1..505,AF133083.1:1..387,\n"
            + "                     AF133084.1:1..2219,1..>280)\n"
            + "                     /gene=\"Itgae\"\n"
            + "     mRNA            join(AF133070.1:<1616..1739,AF133071.1:578..675,\n"
            + "                     AF133072.1:374..441,AF133072.1:806..923,\n"
            + "                     AF133072.1:1117..1248,AF133073.1:98..213,\n"
            + "                     AF133073.1:727..878,AF133074.1:10..163,\n"
            + "                     AF133074.1:672..822,AF133074.1:1236..1303,\n"
            + "                     AF133075.1:120..258,AF133076.1:267..412,\n"
            + "                     AF133077.1:22..162,AF133078.1:199..423,\n"
            + "                     AF133079.1:325..455,AF133080.1:23..153,\n"
            + "                     AF133081.1:344..504,AF133082.1:106..234,\n"
            + "                     AF133083.1:79..152,AF133084.1:1961..2093,186..>280)\n"
            + "                     /gene=\"Itgae\"\n"
            + "                     /product=\"Itgae protein\"\n"
            + "     CDS             join(AF133070.1:<1616..1739,AF133071.1:578..675,\n"
            + "                     AF133072.1:374..441,AF133072.1:806..923,\n"
            + "                     AF133072.1:1117..1248,AF133073.1:98..213,\n"
            + "                     AF133073.1:727..878,AF133074.1:10..163,\n"
            + "                     AF133074.1:672..822,AF133074.1:1236..1303,\n"
            + "                     AF133075.1:120..258,AF133076.1:267..412,\n"
            + "                     AF133077.1:22..162,AF133078.1:199..423,\n"
            + "                     AF133079.1:325..455,AF133080.1:23..153,\n"
            + "                     AF133081.1:344..504,AF133082.1:106..234,\n"
            + "                     AF133083.1:79..152,AF133084.1:1961..2093,186..>280)\n"
            + "                     /codon_start=3\n"
            + "                     /gene=\"Itgae\"\n"
            + "                     /product=\"Itgae protein\"\n"
            + "                     /db_xref=\"GI:4809045\"\n"
            + "                     /protein_id=\"AAD30063.1\"\n"
            + "                     /translation=\"LKPQGAFNMDVDWAWVTALQPGAPAVLSSLLHLDPSNNQTCLLV\n"
            + "                     ARRSSNRNTAALYRCAISISPDEIACQPVEHICMPKGRYQGVTLVGNHNGVLVCIQVQ\n"
            + "                     ARKFRSLNSELTGACSLLTPNLDLQAQAYFSDLEGFLDPGAHVDSGDYCRSKGGSTGE\n"
            + "                     EKKSARRRRTVEEEDEEEDGTEIAIVLDGSGSIEPSDFQKAKNFISTMMRNFYEKCFE\n"
            + "                     CNFALVQYGAVIQTEFDLQESRDINASLAKVQSIVQVKEVTKTASAMQHVLDNIFIPS\n"
            + "                     RGSRKKALKVMVVLTDGDIFGDPLNLTTVINSPKMQGVVRFAIGVGDAFKNNNTYREL\n"
            + "                     KLIASDPKEAHTFKVTNYSALDGLLSKLQQRIVHMEGTVGDALQYQLAQTGFSAQILD\n"
            + "                     KGQVLLGTVGAFNWSGGALLYSTQNGRGCFLNQTAKEDSRTVQYSYLGYSLAVLHKAH\n"
            + "                     GISYVAGAPRHKLRGAVFELRKEDREEDAFVRRIEGEQMGSYFGSVLCPVDIDMDGTT\n"
            + "                     DFLLVAAPFYHIRGEEGRVYVYQVPEQDASFSLAHTLSGHPGLTNSRFGFAMAAVGDI\n"
            + "                     NQDKFTDVAIGAPLEGFGAGDGASYGSVYIYNGHSGGLYDSPSQQIRASSVASGLHYF\n"
            + "                     GMSVSGGLDFNGDGLADITVGSRDSAVVLRSRPVVDLTVSMTFTPDALPMVFIGKMDV\n"
            + "                     NLCFEVDSSVVASEPGLREMFLNFTVDVDVTKQRQRLQCEDSSGCQSCLRKWNGGSFL\n"
            + "                     CEHFWLISTEELCEEDCFSNITIKVTYEFQTSGGRRDYPNPTLDHYKEPSAIFQLPYE\n"
            + "                     KDCKNKVFCIAEIQLTTNISQQELVVGVTKEVTMNISLTNSGEDSYMTNMALNYPRNL\n"
            + "                     QFKKIQKPVSPDVQCDDPKPVASVLVMNCKIGHPILKRS\"\n"
            + "     CDS             3514..4041\n"
            + "                     /product=\"hypothetical protein\"\n"
            + "                     /note=\"ORF 5\"\n"
            + "                     /db_xref=\"InterPro:IPR001964\"\n"
            + "                     /db_xref=\"UniProtKB/Swiss-Prot:P09511\"\n"
            + "                     /protein_id=\"CAA31466.1\"\n"
            + "                     /translation=\"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEE\n"
            + "                     LFLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFW\n"
            + "                     SSSPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLP\n"
            + "                     RTNSGSSTKAMVLHR\"\n"
            + "ORIGIN\n"
            + "       1 aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa\n"
            + "      61 aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa aaaaaaaaaa\n"
            + "     121 aa\n"
            + "//\n";
    setBufferedReader(entryString);
    EntryReader reader = new GenbankEntryReader(bufferedReader, "test file");
    ValidationResult result = reader.read();
    Entry entry = reader.getEntry();
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    StringWriter writer = new StringWriter();
    assertTrue(new GenbankEntryWriter(entry).write(writer));
    // System.out.print(writer.toString());
    assertEquals(entryString, writer.toString());
  }

  public void testReadWGSEntry() throws IOException {
    String entryString =
        "LOCUS       WAAP01000000             153 rc    DNA     linear   BCT 02-OCT-2019\n"
            + "DEFINITION  Escherichia coli strain PTA_A1527-5, whole genome shotgun\n"
            + "            sequencing project.\n"
            + "ACCESSION   WAAP00000000\n"
            + "VERSION     WAAP00000000.1  GI:1755607708\n"
            + "DBLINK      BioProject: PRJNA573742\n"
            + "            BioSample: SAMN12823904\n"
            + "KEYWORDS    WGS.\n"
            + "SOURCE      Escherichia coli\n"
            + "  ORGANISM  Escherichia coli\n"
            + "            Bacteria; Proteobacteria; Gammaproteobacteria; Enterobacterales;\n"
            + "            Enterobacteriaceae; Escherichia.\n"
            + "REFERENCE   1  (bases 1 to 153)\n"
            + "  AUTHORS   Barrantes,K., Ramirez,L., Acuna,G., Morales,E. and Chacon,L.\n"
            + "  TITLE     MDR Escherichia coli 16 isolated from WWTP effluents\n"
            + "  JOURNAL   Unpublished\n"
            + "REFERENCE   2  (bases 1 to 153)\n"
            + "  AUTHORS   Barrantes,K., Ramirez,L., Acuna,G., Morales,E. and Chacon,L.\n"
            + "  TITLE     Direct Submission\n"
            + "  JOURNAL   Submitted (24-SEP-2019) Instituto de Investigaciones en Salud,\n"
            + "            Universidad de Costa Rica, Mercedes Montes de Oca, San Jose 2060,\n"
            + "            Costa Rica\n"
            + "COMMENT     The Escherichia coli whole genome shotgun (WGS) project has the\n"
            + "            project accession WAAN00000000.  This version of the project (01)\n"
            + "            has the accession number WAAN01000000, and consists of sequences\n"
            + "            WAAN01000001-WAAN01000153.\n"
            + "            Bacteria and source DNA available from INISA_UCR.\n"
            + "            The annotation was added by the NCBI Prokaryotic Genome Annotation\n"
            + "            Pipeline (PGAP). Information about PGAP can be found here:\n"
            + "            https://www.ncbi.nlm.nih.gov/genome/annotation_prok/\n"
            + "            ##Genome-Assembly-Data-START##\n"
            + "            Assembly Date          :: NOV-2018\n"
            + "            Assembly Method        :: SPAdes v. 3.13.0\n"
            + "            Genome Representation  :: Full\n"
            + "            Expected Final Version :: Yes\n"
            + "            Genome Coverage        :: 30.0x\n"
            + "            Sequencing Technology  :: Illumina MiSeq\n"
            + "            ##Genome-Assembly-Data-END##\n"
            + "            ##Genome-Annotation-Data-START##\n"
            + "            Annotation Provider               :: NCBI\n"
            + "            Annotation Date                   :: 09/27/2019 14:07:38\n"
            + "            Annotation Pipeline               :: NCBI Prokaryotic Genome\n"
            + "                                                 Annotation Pipeline (PGAP)\n"
            + "            Annotation Method                 :: Best-placed reference protein\n"
            + "                                                 set; GeneMarkS-2+\n"
            + "            Annotation Software revision      :: 4.9\n"
            + "            Features Annotated                :: Gene; CDS; rRNA; tRNA; ncRNA;\n"
            + "                                                 repeat_region\n"
            + "            Genes (total)                     :: 4,993\n"
            + "            CDSs (total)                      :: 4,871\n"
            + "            Genes (coding)                    :: 4,694\n"
            + "            CDSs (with protein)               :: 4,694\n"
            + "            Genes (RNA)                       :: 122\n"
            + "            rRNAs                             :: 7, 7, 16 (5S, 16S, 23S)\n"
            + "            complete rRNAs                    :: 5, 1 (5S, 16S)\n"
            + "            partial rRNAs                     :: 2, 6, 16 (5S, 16S, 23S)\n"
            + "            tRNAs                             :: 83\n"
            + "            ncRNAs                            :: 9\n"
            + "            Pseudo Genes (total)              :: 177\n"
            + "            CDSs (without protein)            :: 177\n"
            + "            Pseudo Genes (ambiguous residues) :: 0 of 177\n"
            + "            Pseudo Genes (frameshifted)       :: 85 of 177\n"
            + "            Pseudo Genes (incomplete)         :: 67 of 177\n"
            + "            Pseudo Genes (internal stop)      :: 58 of 177\n"
            + "            Pseudo Genes (multiple problems)  :: 28 of 177\n"
            + "            CRISPR Arrays                     :: 2\n"
            + "            ##Genome-Annotation-Data-END##\n"
            + "FEATURES             Location/Qualifiers\n"
            + "     source          1..153\n"
            + "                     /organism=\"Escherichia coli\"\n"
            + "                     /mol_type=\"genomic DNA\"\n"
            + "                     /strain=\"PTA_A1527-5\"\n"
            + "                     /serotype=\"O110:H28\"\n"
            + "                     /isolation_source=\"wastewater\"\n"
            + "                     /db_xref=\"taxon:562\"\n"
            + "                     /country=\"Costa Rica\"\n"
            + "                     /lat_lon=\"9.56 N 84.16 W\"\n"
            + "                     /collection_date=\"11-Dec-2013\"\n"
            + "                     /collected_by=\"INISA-UCR\"\n"
            + "WGS             WAAN01000001-WAAN01000153\n"
            + "//\n";
    setBufferedReader(entryString);
    EntryReader reader = new GenbankEntryReader(bufferedReader, "test file");
    ValidationResult result = reader.read();
    Entry entry = reader.getEntry();
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals("WAAP01000000", entry.getPrimaryAccession());
    StringWriter writer = new StringWriter();
    assertTrue(new GenbankEntryWriter(entry).write(writer));
    // System.out.print(writer.toString());
    String expectedEntryString =
        "LOCUS       WAAP01000000               0 bp    DNA     linear   XXX 02-OCT-2019\n"
            + "DEFINITION  Escherichia coli strain PTA_A1527-5, whole genome shotgun\n"
            + "            sequencing project.\n"
            + "ACCESSION   WAAP01000000\n"
            + "VERSION     WAAP00000000.1  GI:1755607708\n"
            + "DBLINK      BioProject: PRJNA573742\n"
            + "            BioSample: SAMN12823904\n"
            + "KEYWORDS    WGS.\n"
            + "SOURCE      Escherichia coli\n"
            + "  ORGANISM  Escherichia coli\n"
            + "            Bacteria; Proteobacteria; Gammaproteobacteria; Enterobacterales;\n"
            + "            Enterobacteriaceae; Escherichia.\n"
            + "REFERENCE   1  (bases 1 to 153)\n"
            + "  AUTHORS   Barrantes,K., Ramirez,L., Acuna,G., Morales,E. and Chacon,L.\n"
            + "  TITLE     MDR Escherichia coli 16 isolated from WWTP effluents\n"
            + "  JOURNAL   Unpublished\n"
            + "REFERENCE   2  (bases 1 to 153)\n"
            + "  AUTHORS   Barrantes,K., Ramirez,L., Acuna,G., Morales,E. and Chacon,L.\n"
            + "  JOURNAL   Submitted (24-SEP-2019) Instituto de Investigaciones en Salud,\n"
            + "            Universidad de Costa Rica, Mercedes Montes de Oca, San Jose 2060,\n"
            + "            Costa Rica\n"
            + "COMMENT     The Escherichia coli whole genome shotgun (WGS) project has the\n"
            + "            project accession WAAN00000000.  This version of the project (01)\n"
            + "            has the accession number WAAN01000000, and consists of sequences\n"
            + "            WAAN01000001-WAAN01000153.\n"
            + "            Bacteria and source DNA available from INISA_UCR.\n"
            + "            The annotation was added by the NCBI Prokaryotic Genome Annotation\n"
            + "            Pipeline (PGAP). Information about PGAP can be found here:\n"
            + "            https://www.ncbi.nlm.nih.gov/genome/annotation_prok/\n"
            + "            ##Genome-Assembly-Data-START##\n"
            + "            Assembly Date          :: NOV-2018\n"
            + "            Assembly Method        :: SPAdes v. 3.13.0\n"
            + "            Genome Representation  :: Full\n"
            + "            Expected Final Version :: Yes\n"
            + "            Genome Coverage        :: 30.0x\n"
            + "            Sequencing Technology  :: Illumina MiSeq\n"
            + "            ##Genome-Assembly-Data-END##\n"
            + "            ##Genome-Annotation-Data-START##\n"
            + "            Annotation Provider               :: NCBI\n"
            + "            Annotation Date                   :: 09/27/2019 14:07:38\n"
            + "            Annotation Pipeline               :: NCBI Prokaryotic Genome\n"
            + "            Annotation Pipeline (PGAP)\n"
            + "            Annotation Method                 :: Best-placed reference protein\n"
            + "            set; GeneMarkS-2+\n"
            + "            Annotation Software revision      :: 4.9\n"
            + "            Features Annotated                :: Gene; CDS; rRNA; tRNA; ncRNA;\n"
            + "            repeat_region\n"
            + "            Genes (total)                     :: 4,993\n"
            + "            CDSs (total)                      :: 4,871\n"
            + "            Genes (coding)                    :: 4,694\n"
            + "            CDSs (with protein)               :: 4,694\n"
            + "            Genes (RNA)                       :: 122\n"
            + "            rRNAs                             :: 7, 7, 16 (5S, 16S, 23S)\n"
            + "            complete rRNAs                    :: 5, 1 (5S, 16S)\n"
            + "            partial rRNAs                     :: 2, 6, 16 (5S, 16S, 23S)\n"
            + "            tRNAs                             :: 83\n"
            + "            ncRNAs                            :: 9\n"
            + "            Pseudo Genes (total)              :: 177\n"
            + "            CDSs (without protein)            :: 177\n"
            + "            Pseudo Genes (ambiguous residues) :: 0 of 177\n"
            + "            Pseudo Genes (frameshifted)       :: 85 of 177\n"
            + "            Pseudo Genes (incomplete)         :: 67 of 177\n"
            + "            Pseudo Genes (internal stop)      :: 58 of 177\n"
            + "            Pseudo Genes (multiple problems)  :: 28 of 177\n"
            + "            CRISPR Arrays                     :: 2\n"
            + "            ##Genome-Annotation-Data-END##\n"
            + "FEATURES             Location/Qualifiers\n"
            + "     source          1..153\n"
            + "                     /organism=\"Escherichia coli\"\n"
            + "                     /strain=\"PTA_A1527-5\"\n"
            + "                     /serotype=\"O110:H28\"\n"
            + "                     /mol_type=\"genomic DNA\"\n"
            + "                     /country=\"Costa Rica\"\n"
            + "                     /lat_lon=\"9.56 N 84.16 W\"\n"
            + "                     /isolation_source=\"wastewater\"\n"
            + "                     /collected_by=\"INISA-UCR\"\n"
            + "                     /collection_date=\"11-Dec-2013\"\n"
            + "                     /db_xref=\"taxon:562\"\n"
            + "//\n";
    assertEquals(expectedEntryString, writer.toString());
  }
}
