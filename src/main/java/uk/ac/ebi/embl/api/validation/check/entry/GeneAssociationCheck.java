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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;

import java.util.*;

/**
 * Checks that features sharing the same gene tag are associated with the same locus_tag
 */
@Description("Features sharing gene \\\"{0}\\\" are associated with \\\"{3}\\\" qualifiers with different values (\\\"{1}\\\" and \\\"{2}\\\")\".")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class GeneAssociationCheck extends EntryValidationCheck {

    protected final static String MESSAGE_ID_DIFFERENT_LOCUS_VALUES = "GeneAssociationCheck";

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
         * which locus tag qualifier is associated with which gene qualifier - should be 1 to 1
         */
        HashMap<String, String> gene2locusTag = new HashMap<String, String>();
        /**
         * which pseudogene qualifier is associated with which gene qualifier - should be 1 to 1
         */
        HashMap<String, String> gene2pseudoGene = new HashMap<String, String>();

        for (Feature geneFeature : geneFeatures) {

            /**
             * we know this contains a gene qualifier cos that's how we built the list
             */
            String gene_name = geneFeature.getQualifiers(Qualifier.GENE_QUALIFIER_NAME).get(0).getValue();

            int locusCount =
                    SequenceEntryUtils.getFeatureQualifierCount(Qualifier.LOCUS_TAG_QUALIFIER_NAME, geneFeature);
            int pseudoCount =
                SequenceEntryUtils.getFeatureQualifierCount(Qualifier.PSEUDOGENE_QUALIFIER_NAME, geneFeature);


            if (locusCount == 1) {
                String currentLocusName = geneFeature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();

                if (gene2locusTag.containsKey(gene_name)) {
                    String registeredLocusName = gene2locusTag.get(gene_name);
                    //exception for rRNA features
                    if (registeredLocusName!=null&&!registeredLocusName.equals(currentLocusName)&&!geneFeature.getName().equals(Feature.rRNA_FEATURE_NAME)) {//is associated with more than 1 locus tag value
                    	reportWarning(geneFeature.getOrigin(), MESSAGE_ID_DIFFERENT_LOCUS_VALUES, gene_name, registeredLocusName, currentLocusName,"\\locus_tag");
                    }
                } else {
                    //this locus tag is now reserved by this gene qualifier
                    gene2locusTag.put(gene_name, currentLocusName);
                }
            }
            
            if (pseudoCount == 1) {
                String currentPseudoName = geneFeature.getQualifiers(Qualifier.PSEUDOGENE_QUALIFIER_NAME).get(0).getValue();

                if (gene2pseudoGene.containsKey(gene_name)) {
                    String registeredPseudoName = gene2pseudoGene.get(gene_name);
                    if (registeredPseudoName!=null&&!registeredPseudoName.equals(currentPseudoName)) {//is associated with more than 1 pseudogene value
                        reportWarning(geneFeature.getOrigin(), MESSAGE_ID_DIFFERENT_LOCUS_VALUES, gene_name, registeredPseudoName, currentPseudoName,"\\pseudogene");
                    }
                } else {
                    //this locus tag is now reserved by this gene qualifier
                    gene2pseudoGene.put(gene_name, currentPseudoName);
                }
            }
        }

        return result;
    }


}
