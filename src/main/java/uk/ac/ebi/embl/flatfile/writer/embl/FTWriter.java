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

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ebi.embl.common.ExonIntronSorter;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.flatfile.writer.FeatureWriter;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

/** Flat file writer for the feature table.
 */
public class FTWriter extends FlatFileWriter {

	private boolean sortFeatures;
	private boolean sortQualifiers;
	private boolean isReducedFlatfile;

	public FTWriter(Entry entry, boolean sortFeatures,
					boolean sortQualifiers, WrapType wrapType) {
		super(entry, wrapType);
		this.sortFeatures = sortFeatures;
		this.sortQualifiers = sortQualifiers;
	}

	public FTWriter(Entry entry, boolean sortFeatures,
					boolean sortQualifiers, boolean isReducedFlatfile, WrapType wrapType) {
		super(entry, wrapType);
		this.sortFeatures = sortFeatures;
		this.sortQualifiers = sortQualifiers;
		this.isReducedFlatfile = isReducedFlatfile;
	}

	public boolean write(Writer writer) throws IOException {
		List<Feature> features = new LinkedList<>(entry.getFeatures());
		if (features.size() == 0) {
			return false;
		}

		if (sortFeatures) {
			sortFeatures(features);
		}
		new FHWriter(entry).write(writer);
		for (Feature feature : features) {
            writeFeature(writer, feature);
        }
		return true;		
	}

	public void sortFeatures(List<Feature> features) {
		//source features always comes first, other features we sort by smallest min position ,
		// Order features with smaller minimum positions first.
		// if min positions are same
		// and  if intron/exon then Order smaller maximum positions first(ExonIntronSorter.java has this logic).
		// else Order largest maximum positions first(compare method in Feature.java has all the sorting log except intron/exon).
		Collections.sort(features);

		List<Feature> intronExtronFeatures= new LinkedList<>();
		int start =0;
		boolean collectingIntron = false;
		boolean collectingExon = false;
		for (int i = 0; i < features.size(); i++) {
			Feature feat = features.get(i);
			if (feat.getName().equals(Feature.INTRON_FEATURE_NAME)) {
				if (collectingExon) { //if we were already collecting exons ,sort and insert them first
					//sort and insert introns at the correct positions
					insertSortedExtrons(features, intronExtronFeatures, start, i);
					collectingExon = false;
					intronExtronFeatures = new LinkedList<>();
				}

				if (!collectingIntron) {
					start = i;
					collectingIntron = true;
				}
				intronExtronFeatures.add(feat);

			} else if (feat.getName().equals(Feature.EXON_FEATURE_NAME)) {

				if (collectingIntron) { //if we were already collecting introns ,sort and insert them first
					//sort and insert exons at the correct positions
					insertSortedExtrons(features, intronExtronFeatures, start, i);
					collectingIntron = false;
					intronExtronFeatures = new LinkedList<>();
				}

				if (!collectingExon) {
					start = i;
					collectingExon = true;
				}
				intronExtronFeatures.add(feat);
			} else {
				if (start > 0) {
					//sort and insert exons/introns at the correct positions
					insertSortedExtrons(features, intronExtronFeatures, start, i);
					collectingExon = false;
					collectingIntron = false;
					start = 0;
					intronExtronFeatures = new LinkedList<>();
				}
			}

		}
	}

	private void insertSortedExtrons(List<Feature> features, List<Feature> intronExtronFeatures, int start, int currPos) {
		intronExtronFeatures.sort( new ExonIntronSorter());
		features.subList(start, currPos).clear();
		features.addAll(start,intronExtronFeatures);
	}

	private boolean isSourceFeature(Feature feature) {
		return feature.getName().equals(Feature.SOURCE_FEATURE_NAME);
	}

	private boolean allSourceFeatures(List<Feature> features) {
		for (Feature feature : features) {
			if (!isSourceFeature(feature))
				return false;
		}
		return true;
	}

    protected void writeFeature(Writer writer, Feature feature) throws IOException {
        new FeatureWriter(entry, feature, sortQualifiers, wrapType,
        		EmblPadding.FEATURE_PADDING,
        		EmblPadding.QUALIFIER_PADDING, isReducedFlatfile).write(writer);
    }
}
