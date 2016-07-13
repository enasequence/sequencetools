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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Description("CDS/tRNA/rRNA feature is within a gene feature. Copied \\\"locus_tag\\\", \\\"gene\\\" and \\\"gene_synonym\\\" qualifiers (where present) from the gene feature.")
public class CDS_RNA_LocusFix extends EntryValidationCheck {

    private final static String FIX_ID = "CDS_RNA_LocusFix";
    private final static String SKIPPING_CIRCULAR_ID = "CDS_RNA_LocusFix2";

    public CDS_RNA_LocusFix() {
    }

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null) {
            return result;
        }

        Sequence sequence = entry.getSequence();
        if (sequence == null) {
            return result;
        }

        if (sequence.getTopology() == null) {
            return result;
        }

        /**
         * build a list of CDS tRNA and rRNA features
         */
        List<Feature> relevantFeatures = new ArrayList<Feature>();
        relevantFeatures.addAll(SequenceEntryUtils.getFeatures("CDS", entry));
        relevantFeatures.addAll(SequenceEntryUtils.getFeatures("tRNA", entry));
        relevantFeatures.addAll(SequenceEntryUtils.getFeatures("rRNA", entry));

        /**
         * make a list of all features with a gene feature
         */
        Collection<Feature> geneFeatures = SequenceEntryUtils.getFeatures("gene", entry);

        /**
         * if there is at least one of the three feature types CDS, tRNA and rRNA and there is at least 1 gene...
         */
        if (((!relevantFeatures.isEmpty())) && !geneFeatures.isEmpty()) {
            List<Feature> nonLocusFeatures = new ArrayList<Feature>();
            /**
             * keep a list of all CDS/rRNA/tRNA features with no locus/gene or gene synonym features
             */
            for (Feature feature : relevantFeatures) {
                if (!SequenceEntryUtils.isQualifierAvailable("locus_tag", feature) &&
                        !SequenceEntryUtils.isQualifierAvailable("gene", feature) &&
                        !SequenceEntryUtils.isQualifierAvailable("gene_synonym", feature)) {
                    nonLocusFeatures.add(feature);
                }
            }

            /**
             * then assign the locus/gene/gene_synonym features to the CDS features from the other features (when not circular)
             */
            for (Feature feature : nonLocusFeatures) {

                for (Feature geneFeature : geneFeatures) {
                    if (SequenceEntryUtils.isLocationWithin(feature.getLocations(), geneFeature.getLocations())) {

                        /**
                         * if the CDS/rRNA/tRNA feature is circular - warn and skip
                         */
                        if(SequenceEntryUtils.isCircularBoundary(feature.getLocations(), sequence.getLength())){
                            reportMessage(Severity.WARNING, feature.getOrigin(), SKIPPING_CIRCULAR_ID);
                            continue;
                        }

                        /**
                         * if the gene feature is circular skip but don't warn...
                         */
                        if ((sequence.getTopology() == Sequence.Topology.CIRCULAR) &&
                                (SequenceEntryUtils.isCircularBoundary(geneFeature.getLocations(), sequence.getLength()))) {
                            continue;
                        }

                        Qualifier locus_tag = SequenceEntryUtils.getQualifier("locus_tag", geneFeature);
                        Qualifier gene = SequenceEntryUtils.getQualifier("gene", geneFeature);
                        Qualifier gene_synonym = SequenceEntryUtils.getQualifier("gene_synonym", geneFeature);
                        boolean fixMade = false;
                        if (locus_tag != null) {
                            feature.addQualifier(locus_tag);
                            fixMade = true;
                        }

                        if (gene != null) {
                            feature.addQualifier(gene);
                            fixMade = true;
                        }

                        if (gene_synonym != null) {
                            feature.addQualifier(gene_synonym);
                            fixMade = true;
                        }

                        if (fixMade) {
                            reportMessage(Severity.FIX, feature.getOrigin(), FIX_ID);
                        }
                    }
                }
            }
        }
        return result;
    }

}
