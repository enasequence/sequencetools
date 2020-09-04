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
import uk.ac.ebi.embl.api.validation.helper.Utils;

import java.util.*;

import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.LOCUS_TAG_QUALIFIER_NAME;

/**
 * Checks that features sharing the same locus tag are associated with the same gene and a stable list of gene_synonyms 
 */
@Description("Features sharing locus_tag \\\"{0}\\\" are associated with \\\"gene\\\" qualifiers with different values (\\\"{1}\\\" and \\\"{2}\\\")" +
        "Features sharing locus_tag \\\"{0}\\\" are associated with \\\"gene_synonym\\\" qualifiers with different sets of values. They should all share the same values.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class LocusTagAssociationCheck extends EntryValidationCheck {

    protected final static String MESSAGE_ID_DIFFERENT_GENE_VALUES = "LocusTagAssociationCheck1";
    protected final static String MESSAGE_ID_DIFFERENT_GENE_SYNONYM_VALUES = "LocusTagAssociationCheck2";

    /**
     * Checks the coverage of sequence by source features' locations.
     *
     * @param entry an entry to be checked (expected type is Entry)
     * @return a validation result
     */
    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        if (entry == null) {
            return result;
        }

        //collect all locus features
        Collection<Feature> locusFeatures =
                SequenceEntryUtils.getFeaturesContainingQualifier(LOCUS_TAG_QUALIFIER_NAME, entry);

        if (locusFeatures.isEmpty()) {
            return result;
        }

        /**
         * which locus tag qualifiers are mapped to which gene_synonyms - should be a stable association
         */
        HashMap<String, List<String>> locusTag2GeneSynonyms = new HashMap<String, List<String>>();

        /**
         * which locus tag qualifier is associated with which gene qualifier - should be 1 to 1
         */
        HashMap<String, String> locusTag2Gene = new HashMap<String, String>();

        /**
         * If there is a CDS or a gene feature, prioritize the list of gene_synonyms from these (CDS first, then Gene)
         */
        Feature cdsFeature = null;
        Feature geneFeature = null;
        for(Feature currentFeature : locusFeatures){
            if(currentFeature.getName().equals(Feature.CDS_FEATURE_NAME)){
                cdsFeature = currentFeature;
            }else if(currentFeature.getName().equals(Feature.GENE_FEATURE_NAME)){
                geneFeature = currentFeature;
            }
        }

        if (cdsFeature != null) {
            setMasterLocusFeature(locusTag2GeneSynonyms, locusTag2Gene, cdsFeature);
        } else if (geneFeature != null) {
            setMasterLocusFeature(locusTag2GeneSynonyms, locusTag2Gene, geneFeature);
        }//otherwise the first one we find will be the reference feature

        for (Feature locusFeature : locusFeatures) {

            /**
             * we know this contains a locus_tag qualifier cos that's how we built the list
             */
            String locus_tag_name = locusFeature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();

            int geneCount =
                    SequenceEntryUtils.getFeatureQualifierCount(Qualifier.GENE_QUALIFIER_NAME, locusFeature);

            if (geneCount == 1) {
                String currentGeneName = locusFeature.getQualifiers(Qualifier.GENE_QUALIFIER_NAME).get(0).getValue();

                if (locusTag2Gene.containsKey(locus_tag_name)) {
                    String registeredGeneName = locusTag2Gene.get(locus_tag_name);
                    if (!registeredGeneName.equals(currentGeneName)) {//is associated with more than 1 gene
                        reportError(result, locusFeature, MESSAGE_ID_DIFFERENT_GENE_VALUES,
                                locus_tag_name, registeredGeneName, currentGeneName);
                    }
                } else {
                    //this locus tag is now reserved by this gene qualifier
                    setMasterLocusFeature(locusTag2GeneSynonyms, locusTag2Gene, locusFeature);
                }
            }

            if (locusTag2GeneSynonyms.containsKey(locus_tag_name)) {
                List<String> geneSynonymValues = new ArrayList<String>();
                for (Qualifier synonymQualifier : locusFeature.getQualifiers(Qualifier.GENE_SYNONYM_NAME)) {
                    String synonymName = synonymQualifier.getValue();
                    geneSynonymValues.add(synonymName);
                }

                //if the feature has gene synonyms - check to see if they match the master ones
                List<String> masterGenes = locusTag2GeneSynonyms.get(locus_tag_name);
                if (!geneSynonymValues.isEmpty() && !masterGenes.isEmpty() && !Arrays.deepEquals(masterGenes.toArray(), geneSynonymValues.toArray())) {
                    ValidationMessage<Origin> message = reportError(result, locusFeature, MESSAGE_ID_DIFFERENT_GENE_SYNONYM_VALUES, locus_tag_name);
                    String masterValues =
                            Utils.paramArrayToCuratorTipString(masterGenes.toArray(new String[masterGenes.size()]));
                    String theseValues = 
                            Utils.paramArrayToCuratorTipString(geneSynonymValues.toArray(new String[geneSynonymValues.size()]));

                    message.appendCuratorMessage("This feature has synonyms " + theseValues);
                    message.appendCuratorMessage("other features have synonyms " + masterValues);
                }
            } else {
                //this locus tag is now reserved by this set of gene synonyms
                setMasterLocusFeature(locusTag2GeneSynonyms, locusTag2Gene, locusFeature);
            }
        }

        return result;
    }

    private void setMasterLocusFeature(HashMap<String, List<String>> locusTag2GeneSynonyms,
                                       HashMap<String, String> locusTag2Gene, Feature feature) {
        String locus_tag_name = feature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();
        List<Qualifier> geneQualifiers = feature.getQualifiers(Qualifier.GENE_QUALIFIER_NAME);
        if(geneQualifiers.size() > 0){
            locusTag2Gene.put(locus_tag_name, geneQualifiers.get(0).getValue());
        }

        List<String> geneSynonymValues = new ArrayList<String>();
        for (Qualifier synonymQualifier : feature.getQualifiers(Qualifier.GENE_SYNONYM_NAME)) {
            String synonymName = synonymQualifier.getValue();
            geneSynonymValues.add(synonymName);
        }

        locusTag2GeneSynonyms.put(locus_tag_name, geneSynonymValues);
    }

    /**
     * Adds error to the result.
     *
     * @param result a reference to validation result
     */
    private ValidationMessage<Origin> reportError(ValidationResult result, Feature feature, String messageId, Object ... params) {
        ValidationMessage<Origin> message =
                EntryValidations.createMessage(feature.getOrigin(), Severity.ERROR, messageId, params);

        if (SequenceEntryUtils.isQualifierAvailable(Qualifier.LOCUS_TAG_QUALIFIER_NAME, feature)) {
            Qualifier locusTag = SequenceEntryUtils.getQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, feature);
            message.appendCuratorMessage("locus tag = " + locusTag.getValue());
        }

        if (SequenceEntryUtils.isQualifierAvailable(Qualifier.GENE_QUALIFIER_NAME, feature)) {
            Qualifier geneName = SequenceEntryUtils.getQualifier(Qualifier.GENE_QUALIFIER_NAME, feature);
            message.appendCuratorMessage("gene = " + geneName.getValue());
        }

        result.append(message);
        return message;
    }

}
