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
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Adds locus_tag qualifiers to features sharing the same gene qualifier where there is a 1 to 1 mapping with
 * locus_tag qualifiers - ignores where multiple locus_tags are associated (to be picked up by other checks)
 */
@Description("Added locus_tag \\\"{0}\\\" to feature sharing \\\"gene\\\" qualifier \\\"{0}\\")
public class LocusTagAssociationFix extends EntryValidationCheck {

    protected final static String MESSAGE_ID = "LocusTagAssociationFix";

    /**
     * Adds locus_tag qualifiers to features sharing the same gene qualifier where there is a 1 to 1 mapping with
     * locus_tag qualifiers - ignores where multiple locus_tags are associated (to be picked up by other checks)
     *
     * @param entry an entry to be checked (expected type is Entry)
     * @return a validation result
     */
    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null) {
            return result;
        }

        //collect all gene features
        Collection<Feature> geneFeatures =
                SequenceEntryUtils.getFeaturesContainingQualifier(Qualifier.GENE_QUALIFIER_NAME, entry);

        if (geneFeatures.isEmpty()) {
            return result;
        }

        /**
         * which gene qualifier is associated with which locus tag qualifier - should be 1 to 1
         */
        HashMap<String, String> gene2locusTag = new HashMap<String, String>();

        /**
         * firstly, build up a list of all genes associated with only 1 locus_tag - ignore those associated with
         * more than 1 locus_tag as we are just looking for clean, 1 to 1 relationships.
         */
        for (Feature geneFeature : geneFeatures) {

            /**
             * we know this contains a gene qualifier cos that's how we built the list
             */
            List<Qualifier> geneQualifiers = geneFeature.getQualifiers(Qualifier.GENE_QUALIFIER_NAME);
            String geneName = geneQualifiers.get(0).getValue();
            if(geneQualifiers.size() > 1){
                continue;//just leave it - other checks for this, should only be 1
            }

            int locusTagCount =
                    SequenceEntryUtils.getFeatureQualifierCount(Qualifier.LOCUS_TAG_QUALIFIER_NAME, geneFeature);

            if (locusTagCount > 1) {
                continue;//just leave it - other checks for this
            } else if (locusTagCount == 1) {
                String currentLocusTag = geneFeature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();

                if (gene2locusTag.containsKey(geneName)) {
                    /**
                     * if the locus tag already associated with this gene has a different value, bail out for this gene.
                     * other checks will point out that there needs to be a 1 to 1 correspondence.
                     */
                    if(!gene2locusTag.get(geneName).equals(currentLocusTag)){
                        gene2locusTag.remove(geneName);//remove existing mapping as this is now not clearly the intended mapping
                        continue;
                    }
                } else {
                    //this locus tag is now reserved by this gene qualifier
                    gene2locusTag.put(geneName, currentLocusTag);
                }
            }
        }

        QualifierFactory qualifierFactory = new QualifierFactory();
        /**
         * then add the locus_tag to all features that do not have any and share the same gene qualifier as another
         * feature that does have a locus_tag associated
         */
        for (Feature geneFeature : geneFeatures) {
            String geneName = geneFeature.getQualifiers(Qualifier.GENE_QUALIFIER_NAME).get(0).getValue();

            int locusTagCount =
                    SequenceEntryUtils.getFeatureQualifierCount(Qualifier.LOCUS_TAG_QUALIFIER_NAME, geneFeature);

            if (locusTagCount == 0) {
                if(gene2locusTag.containsKey(geneName)){
                    String locus_tag = gene2locusTag.get(geneName);
                    Qualifier locusQualifier =
                            qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, locus_tag);
                    geneFeature.addQualifier(locusQualifier);
                    reportMessage(Severity.FIX, geneFeature.getOrigin(), MESSAGE_ID, locus_tag, geneName);
                }
            }
        }

        return result;
    }
}
