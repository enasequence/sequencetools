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

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;
import java.io.StringWriter;

public class XmlFeatureLocationWriter {

    private Feature feature;
    		
    public XmlFeatureLocationWriter(Feature feature) {
        this.feature = feature;
    }
	
    public boolean write(SimpleXmlWriter writer) throws IOException {
    	if (feature == null ||
    		feature.getLocations() == null ||
    		feature.getLocations().getLocations() == null ||
    		feature.getLocations().getLocations().size() == 0) {
    		return false;
    	}
    	// feature.getLocations().removeGlobalComplement();
    	StringWriter stringWriter = new StringWriter();
		(new FeatureLocationWriter(null /*entry */, feature, WrapType.NO_WRAP,
				"", "")).write(stringWriter);
    	String location = stringWriter.toString();
    	// Remove feature name and space between feature and location.
    	location = location.substring(location.indexOf(' ')).trim();
    	writer.writeAttribute("location", location);
		return true;
    }
}
