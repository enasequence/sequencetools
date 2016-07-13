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
import java.io.Writer;
import java.util.Collections;
import java.util.Vector;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.FeatureWriter;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the feature table.
 */
public class FeaturesWriter extends FlatFileWriter {
	
	public FeaturesWriter(Entry entry, boolean sortFeatures, 
			boolean sortQualifiers, WrapType wrapType) {
		super(entry, wrapType);
		this.sortFeatures = sortFeatures;
		this.sortQualifiers = sortQualifiers;
	}

	private boolean sortFeatures;
	private boolean sortQualifiers;
	
	public boolean write(Writer writer) throws IOException {
		Vector<Feature> features = new Vector<Feature>(entry.getFeatures());
		if (features == null ||
			features.size() == 0) {
			return false;
		}
		if (sortFeatures) {
			Collections.sort(features);
		}
		writer.write("FEATURES             Location/Qualifiers\n");
		for (Feature feature : features) {
            writeFeature(writer, feature);
        }
		return true;		
	}

    protected void writeFeature(Writer writer, Feature feature) throws IOException {
        new FeatureWriter(entry, feature, sortQualifiers, wrapType,
        		GenbankPadding.FEATURE_PADDING,
        		GenbankPadding.QUALIFIER_PADDING).write(writer);
    }
}
