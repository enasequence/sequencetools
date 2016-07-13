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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.genbank.AuthorsReader;
import uk.ac.ebi.embl.flatfile.reader.genbank.GenbankLineReader;
import uk.ac.ebi.embl.flatfile.writer.genbank.GenbankEntryWriter;

public class GenbankEntryWriterTest extends GenbankWriterTest {

	public void testWrite_Entry() throws IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes()));
		entry.setPrimaryAccession("DP000153");
		entry.setLastUpdated(FlatFileUtils.getDay("10-SEP-1998"));
		entry.getSequence().setVersion(1);
		entry.getSequence().setTopology(Topology.LINEAR);
		entry.getSequence().setMoleculeType("genomic RNA");
		entry.getSequence().setAccession("DP000153");
		entry.setDataClass("CON");
		entry.setDivision("MAM");
		entry.setDescription(new Text("Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor."));
		entry.setComment(new Text("Cloning\n and characterization\n of a cDNA encoding a novel subtype of rat\n thyrotropin-releasing hormone receptor"));

		ReferenceFactory referenceFactory = new ReferenceFactory();
		Article article = referenceFactory.createArticle(
				"Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor",
		"J. Biol. Chem.");
		article.setVolume("273");
		article.setIssue("48");
		article.setFirstPage("32281");
		article.setLastPage("32287");
		article.setYear(FlatFileUtils.getDay("10-SEP-1998"));
		GenbankLineReader lineReader = new GenbankLineReader();
		lineReader.getCache().setPublication(article);
		lineReader.setReader(new BufferedReader(new StringReader(
				"RA   Antonellis,A., Ayele,K., Benjamin,B., Blakesley,R.W., Young,A., Green,E.D.")));
		lineReader.readLine();
		ValidationResult result = (new AuthorsReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		article.setConsortium("Google consortium");
		Reference reference = referenceFactory.createReference(article, 1);
		reference.setComment("reference comment");
		entry.addReference(reference);

    	EntryFactory entryFactory = new EntryFactory();
    	entry.addAssembly(entryFactory.createAssembly("AC004528", 1, 18665l,
    		19090l, true, 1l, 426l));
    	entry.addAssembly(entryFactory.createAssembly("AC004529", 6, 45665l,
    		98790l, true, 6l, 546l));    	
    	entry.addAssembly(entryFactory.createAssembly("AC004528", 1, 18665l,
    		19090l, true, 1l, 426l));
    	entry.addAssembly(entryFactory.createAssembly("AC004529", 6, 45665l,
    		98790l, true, 6l, 546l));    	
    	
    	FeatureFactory featureFactory = new FeatureFactory();
    	Feature feature = featureFactory.createCdsFeature();
    	LocationFactory locationFactory = new LocationFactory();
    	feature.getLocations().addLocation(
    		locationFactory.createLocalRange(3514l, 4041l, false));
    	QualifierFactory qualifierFactory = new QualifierFactory();
    	feature.addQualifier(qualifierFactory.createQualifier("product",
    		"hypothetical protein"));
    	feature.addQualifier(qualifierFactory.createQualifier("note", "ORF 5"));
    	feature.addQualifier(qualifierFactory.createQualifier("db_xref",
    		"InterPro:IPR001964"));
    	feature.addQualifier(qualifierFactory.createQualifier("db_xref",
    		"UniProtKB/Swiss-Prot:P09511"));
    	feature.addQualifier(qualifierFactory.createQualifier("protein_id",
    		"CAA31466.1"));
    	feature.addQualifier(qualifierFactory.createQualifier("translation",
			"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEELFLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFWSSSPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLPRTNSGSSTKAMVLHR"));
    	entry.addFeature(feature);
    			
		StringWriter writer = new StringWriter();                      
		assertTrue(new GenbankEntryWriter(entry).write(writer));
		//System.out.print(writer.toString());
		assertEquals(
				"LOCUS       DP000153                 122 bp    RNA     linear   CON 10-SEP-1998\n"+
				"DEFINITION  Cloning and characterization of a cDNA encoding a novel subtype of\n"+
				"            rat thyrotropin-releasing hormone receptor.\n"+
				"ACCESSION   DP000153\n"+
				"VERSION     DP000153.1\n" +
				"KEYWORDS    .\n"+
				"REFERENCE   1\n"+
				"  AUTHORS   RA Antonellis,A., Ayele,K., Benjamin,B., Blakesley,R.W., Young,A.\n" +
		        "            and Green,E.D.\n" +				
				"  CONSRTM   Google consortium\n"+
				"  TITLE     Cloning and characterization of a cDNA encoding a novel subtype of\n"+
				"            rat thyrotropin-releasing hormone receptor\n"+
				"  JOURNAL   J. Biol. Chem. 273(48),32281-32287(1998)\n"+
				"  REMARK    reference comment\n"+
				"COMMENT     Cloning\n"+
				"             and characterization\n"+
				"             of a cDNA encoding a novel subtype of rat\n"+
				"             thyrotropin-releasing hormone receptor\n"+
				"PRIMARY     TPA_SPAN            PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n"+
				"            1-426               AC004528.1         18665-19090         c\n"+
				"            6-546               AC004529.6         45665-98790         c\n"+
				"            1-426               AC004528.1         18665-19090         c\n"+
				"            6-546               AC004529.6         45665-98790         c\n"+
				"FEATURES             Location/Qualifiers\n"+
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
				"//\n",
				writer.toString());
	}
}
