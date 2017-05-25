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

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;
import java.io.StringWriter;

public class XmlFeatureWriterTest extends XmlWriterTest {

    public void testWrite() throws IOException {
    	FeatureFactory featureFactory = new FeatureFactory();
    	SourceFeature feature = featureFactory.createSourceFeature();
    	LocationFactory locationFactory = new LocationFactory();
    	feature.getLocations().addLocation(
    		locationFactory.createLocalRange(3514l, 4041l, false));
    	QualifierFactory qualifierFactory = new QualifierFactory();
    	feature.addQualifier(qualifierFactory.createQualifier("product",
    		"hypothetical protein"));
    	feature.addQualifier(qualifierFactory.createQualifier("note", "ORF 5"));
    	feature.addXRef((new EntryFactory()).createXRef(
    			"InterPro", "IPR001964"));
    	feature.addXRef((new EntryFactory()).createXRef(
    			"UniProtKB", "Swiss-Prot:P09511"));
    	feature.addQualifier(qualifierFactory.createQualifier("protein_id",
    		"CAA31466.1"));
    	feature.addQualifier(qualifierFactory.createQualifier("translation",
			"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEELFLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFWSSSPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLPRTNSGSSTKAMVLHR"));    	
    	feature.addQualifier(qualifierFactory.createQualifier("PCR_primers",
    		"fwd_name: 27F, fwd_seq: agagtttgatcctggctcag, rev_name: 1492R, rev_seq: acggctaccttgttacgactt"));
		feature.setScientificName("homo sapiens");
		feature.setCommonName("human");
		feature.setTaxId(34433L);
		feature.getTaxon().setLineage("parent1;parent2;");
    	StringWriter writer = new StringWriter();
    	entry.addFeature(feature);
        new XmlFeatureWriter(entry).write(new SimpleXmlWriter(writer));
        // System.err.print(writer.toString());
        assertEquals(
        		"<feature name=\"source\" location=\"3514..4041\">\n" +
        		"	<taxon scientificName=\"homo sapiens\" commonName=\"human\" taxId=\"34433\">\n" +
        		"		<lineage>\n" +
        		"			<taxon scientificName=\"parent1\"/>\n" +
        		"			<taxon scientificName=\"parent2\"/>\n" +
        		"		</lineage>\n" +
        		"	</taxon>\n" +
        		"	<xref db=\"InterPro\" id=\"IPR001964\"/>\n" +
        		"	<xref db=\"UniProtKB\" id=\"Swiss-Prot:P09511\"/>\n" +
        		"	<qualifier name=\"product\">\n" +
        		"		<value>\n" +
        		"hypothetical protein\n" +
        		"		</value>\n" +
        		"	</qualifier>\n" +
        		"	<qualifier name=\"note\">\n" +
        		"		<value>\n" +
        		"ORF 5\n" +
        		"		</value>\n" +
        		"	</qualifier>\n" +
        		"	<qualifier name=\"protein_id\">\n" +
        		"		<value>\n" +
        		"CAA31466.1\n" +
        		"		</value>\n" +
        		"	</qualifier>\n" +
        		"	<qualifier name=\"translation\">\n" +
        		"		<value>\n" +
        		"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEELFLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQT\n" +
        		"ARHSLMECSRPTMSIKSQWSFWSSSPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLP\n" +
        		"RTNSGSSTKAMVLHR\n" +
        		"		</value>\n" +
        		"	</qualifier>\n" +
        		"	<qualifier name=\"PCR_primers\">\n" +
        		"		<value>\n" +
        		"fwd_name: 27F, fwd_seq: agagtttgatcctggctcag, rev_name: 1492R, rev_seq:\n" +
        		"acggctaccttgttacgactt\n" +
        		"		</value>\n" +
        		"	</qualifier>\n" +
	            "	<qualifier name=\"organism\">\n" +
			    "		<value>\n" +
			    "homo sapiens\n" +
			    "		</value>\n" +
			    "	</qualifier>\n" +
        		"</feature>\n",
            	writer.toString());
    }	
}
