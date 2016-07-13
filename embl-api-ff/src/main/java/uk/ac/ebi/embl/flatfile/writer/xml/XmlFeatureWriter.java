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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.taxonomy.Taxon;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.writer.QualifierWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

/** Flat file writer for the features and qualifiers on the FT lines.
 */
public class XmlFeatureWriter {

	private Entry entry;
    
    public XmlFeatureWriter(Entry entry) {
        this.entry = entry;
    }
    
    public boolean write(SimpleXmlWriter writer) throws IOException {
		Vector<Feature> features = new Vector<Feature>(entry.getFeatures());
		if (features == null ||
			features.size() == 0) {
			return false;
		}
		for (Feature feature : features) {
            writeFeature(writer, feature);
        }
		return true;		
    }

    private boolean writeFeature(SimpleXmlWriter writer, Feature feature) throws IOException {
		writer.beginElement("feature");
		writer.writeAttribute("name", feature.getName());
        (new XmlFeatureLocationWriter(feature)).write(writer);
		writer.openElement("feature");
        writeTaxon(writer, feature);
        writeXrefs(writer, feature);
        writeQualifiers(writer, feature);
        writer.closeElement("feature");
        return true;
    }
    
    private void writeTaxon(SimpleXmlWriter writer, Feature feature) throws IOException {
        if (feature instanceof SourceFeature) {
        	SourceFeature sourceFeature = (SourceFeature)feature;
        	String scientificName = sourceFeature.getScientificName();
        	if (FlatFileUtils.isBlankString(scientificName)) {
        		return;
        	}
        	writer.beginElement("taxon");
        	writer.writeAttribute("scientificName", scientificName);
        	writer.writeAttribute("commonName", sourceFeature.getCommonName());
        	writer.writeAttribute( "taxId", sourceFeature.getTaxId());
        	writer.openElement("taxon");
        	Taxon taxon = sourceFeature.getTaxon();
        	if (taxon != null && taxon.getLineage() != null &&
    			taxon.getFamilyNames().size() > 0) {
        		writer.beginElement("lineage");
        		writer.openElement("lineage");
        		for (String familyName : taxon.getFamilyNames()) {
            		writer.beginElement("taxon");
        			writer.writeAttribute("scientificName", familyName);
        			writer.openCloseElement("taxon");
        		}
        		writer.closeElement("lineage");
        	}        	
        	writer.closeElement("taxon");
        }
    }

    private void writeXrefs(SimpleXmlWriter writer, Feature feature) throws IOException {
    	// Write cross-references.
		(new XmlXrefWriter(feature.getXRefs())).write(writer);    	
    }
    
    private void writeQualifiers(SimpleXmlWriter writer, Feature feature) throws IOException {
    	for (Qualifier qualifier : feature.getQualifiers() ) {
    		writer.beginElement("qualifier");
    		writer.writeAttribute("name", qualifier.getName());
    		writer.openElement("qualifier");
    		if (qualifier.getValue() != null && !qualifier.getValue().trim().isEmpty()) {
    			writer.beginElement("value");
    			writer.openElement("value");
    			StringWriter stringWriter = new StringWriter();
    			(new QualifierWriter(entry, qualifier, WrapType.EMBL_WRAP, "", true)).write(stringWriter);     					
    			writer.writeElementText(stringWriter.toString());
    			writer.closeElement("value");
    		}
    		writer.closeElement("qualifier");
    	}
    }
}
