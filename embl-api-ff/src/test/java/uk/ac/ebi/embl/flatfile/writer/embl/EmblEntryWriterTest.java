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
package uk.ac.ebi.embl.flatfile.writer.embl;

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
import uk.ac.ebi.embl.flatfile.reader.embl.EmblLineReader;
import uk.ac.ebi.embl.flatfile.reader.embl.RAReader;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

public class EmblEntryWriterTest extends EmblWriterTest {

	public void testWrite_Entry() throws IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
		entry.setPrimaryAccession("DP000153");
		entry.getSequence().setVersion(1);
		entry.getSequence().setTopology(Topology.LINEAR);
		entry.getSequence().setMoleculeType("genomic RNA");
		entry.setDataClass("CON");
		entry.setDivision("MAM");
		entry.setIdLineSequenceLength(2);
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
		assertTrue(new EmblEntryWriter(entry).write(writer));
		//System.out.print(writer.toString());
		assertEquals(
				"ID   DP000153; SV 1; linear; genomic RNA; CON; MAM; 2 BP.\n" +
				"XX\n" +
				"AC   DP000153;\n" +
				"XX\n" +
				"DE   Cloning and characterization of a cDNA encoding a novel subtype of rat\n" + 
				"DE   thyrotropin-releasing hormone receptor.\n" +				
				"XX\n" +
				"KW   .\n" +
				"XX\n" +
				"RN   [1]\n" +
				"RC   reference comment\n" +
				"RG   Google consortium\n" +
				"RA   Antonellis A., Ayele K., Benjamin B., Blakesley R.W., Boakye A.,\n" +
				"RA   Bouffard G.G., Brinkley C., Brooks S., Chu G., Coleman H., Engle J.,\n" +
				"RA   Gestole M., Greene A., Guan X., Gupta J., Haghighi P., Han J., Hansen N.,\n" +
				"RA   Ho S.-L., Hu P., Hunter G., Hurle B., Idol J.R., Kwong P., Laric P.,\n" +
				"RA   Larson S., Lee-Lin S.-Q., Legaspi R., Madden M., Maduro Q.L., Maduro V.B.,\n" +
				"RA   Margulies E.H., Masiello C., Maskeri B., McDowell J., Mojidi H.A.,\n" +
				"RA   Mullikin J.C., Oestreicher J.S., Park M., Portnoy M.E., Prasad A., Puri O.,\n" +
				"RA   Reddix-Dugue N., Schandler K., Schueler M.G., Sison C., Stantripop S.,\n" +
				"RA   Stephen E., Taye A., Thomas J.W., Thomas P.J., Tsipouri V., Ung L.,\n" +
				"RA   Vogt J.L., Wetherby K.D., Young A., Green E.D.;\n" +
				"RT   \"Cloning and characterization of a cDNA encoding a novel subtype of rat\n" +
				"RT   thyrotropin-releasing hormone receptor\";\n" +
				"RL   J. Biol. Chem. 273(48):32281-32287(1998).\n" +
				"XX\n" +
				"CC   Cloning\n" +
				"CC    and characterization\n" +
				"CC    of a cDNA encoding a novel subtype of rat\n" +
				"CC    thyrotropin-releasing hormone receptor\n" +
				"XX\n" +				
				"AH   LOCAL_SPAN          PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n" +
				"AS   1-426               AC004528.1         18665-19090         c\n" +
				"AS   6-546               AC004529.6         45665-98790         c\n" +
				"AS   1-426               AC004528.1         18665-19090         c\n" +
				"AS   6-546               AC004529.6         45665-98790         c\n" +				"XX\n" +
				"FH   Key             Location/Qualifiers\n" +
				"FH\n" +
				"FT   CDS             3514..4041\n" +
				"FT                   /product=\"hypothetical protein\"\n" +
				"FT                   /note=\"ORF 5\"\n" +
				"FT                   /db_xref=\"InterPro:IPR001964\"\n" +
				"FT                   /db_xref=\"UniProtKB/Swiss-Prot:P09511\"\n" +
				"FT                   /protein_id=\"CAA31466.1\"\n" +
				"FT                   /translation=\"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEEL\n" +
				"FT                   FLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFWSS\n" +
				"FT                   SPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLPRTN\n" +
				"FT                   SGSSTKAMVLHR\"\n" +				
				"XX\n" +				
				"SQ   Sequence 2 BP; 2 A; 0 C; 0 G; 0 T; 0 other;\n" +
				"     aa                                                                        2\n" +
				"//\n",
				writer.toString());
	}
}
