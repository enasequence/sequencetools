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

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.flatfile.writer.FeatureWriter;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.flatfile.GenbankPadding;

public class GenbankFeatureWriterTest extends GenbankWriterTest {

    public void testWrite_FeatureAndQualifiers() throws IOException {
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
    	feature.addQualifier(qualifierFactory.createQualifier("PCR_primers",
    		"fwd_name: 27F, fwd_seq: agagtttgatcctggctcag, rev_name: 1492R, rev_seq: acggctaccttgttacgactt"));  	
    	StringWriter writer = new StringWriter();
    	boolean sortQualifiers = true;
        new FeatureWriter(entry, feature, sortQualifiers, wrapType,
        		GenbankPadding.FEATURE_PADDING,
        		GenbankPadding.QUALIFIER_PADDING).write(writer);
        //System.out.print(writer.toString());
        assertEquals(
        		"     CDS             3514..4041\n" +
                "                     /product=\"hypothetical protein\"\n" +
                "                     /PCR_primers=\"fwd_name: 27F, fwd_seq:\n" +
                "                     agagtttgatcctggctcag, rev_name: 1492R, rev_seq:\n" +
                "                     acggctaccttgttacgactt\"\n" +
                "                     /note=\"ORF 5\"\n" +
                "                     /db_xref=\"InterPro:IPR001964\"\n" +
                "                     /db_xref=\"UniProtKB/Swiss-Prot:P09511\"\n" +
                "                     /protein_id=\"CAA31466.1\"\n" +
                "                     /translation=\"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEE\n" +
                "                     LFLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFW\n" +
                "                     SSSPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLP\n" +
                "                     RTNSGSSTKAMVLHR\"\n",
            	writer.toString());
    }	
}
