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

import java.util.*;

import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.*;

/**
 * Checks that features sharing the same locus tag are associated with the same gene and a stable list of gene_synonyms
 */
@Description("Added gene_synonym \\\"{0}\\\" to feature sharing locus_tag/gene \\\"{1}\\\" - to create a stable list of gene_synonyms" +
        "Removed gene_synonym \\\"{0}\\\" from feature sharing locus_tag/gene \\\"{1}\\\" - to create a stable list of gene_synonyms")
public class GeneSynonymFix extends EntryValidationCheck {

    protected final static String MESSAGE_ID_ADDED_SYNONYM = "GeneSynonymFix";
    protected final static String MESSAGE_ID_REMOVED_SYNONYM = "GeneSynonymFix2";

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

        //collect all locus features...
        Collection<Feature> locusAndGeneFeatures =
                SequenceEntryUtils.getFeaturesContainingQualifier(LOCUS_TAG_QUALIFIER_NAME, entry);

        //..then add all features containing gene qualifiers - likely to be a lot of overlap
        locusAndGeneFeatures.addAll(SequenceEntryUtils.getFeaturesContainingQualifier(GENE_QUALIFIER_NAME, entry));

        if (locusAndGeneFeatures.isEmpty()) {
            return result;
        }

        /**
         * which locus tag qualifiers are mapped to which gene_synonyms - should be a stable association
         */
        HashMap<String, List<String>> identifier2GeneSynonyms = new HashMap<String, List<String>>();

        /**
         * keep a list of locus tags that we have decided should not be considered due to unstable associations in CDS
         * features - don't even try with these ones
         */
        List<String> unreliableIdentifiers = new ArrayList<String>();

        /**
         * If there are CDS features, prioritize the list of gene_synonyms from these.
         */
        for (Feature currentFeature : locusAndGeneFeatures) {
            if (currentFeature.getName().equals(Feature.CDS_FEATURE_NAME)) {
                String identifier;
                if(!currentFeature.getQualifiers(LOCUS_TAG_QUALIFIER_NAME).isEmpty()){
                    identifier = currentFeature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();
                }else if (!currentFeature.getQualifiers(GENE_QUALIFIER_NAME).isEmpty()){
                    identifier = currentFeature.getQualifiers(GENE_QUALIFIER_NAME).get(0).getValue();
                }else{
                    continue;//this should not be possible as the list was built with features containing at least 1 of the above
                }

                /**
                 * look at all occurrences of any given LOCUS_TAG or GENE_QUALIFIER name - if there are mismatches
                 * between the gene_synonym qualifiers, exclude from the fix - they are unreliable and we cant decide
                 * which of the lists of syninyms we should use to overwrite the other features.
                 */
                if (!unreliableIdentifiers.contains(identifier)) {
                    if (identifier2GeneSynonyms.containsKey(identifier)) {
                        List<String> registeredSynonyms = identifier2GeneSynonyms.get(identifier);

                        List<String> newSynonymValues = new ArrayList<String>();
                        for (Qualifier synonymQualifier : currentFeature.getQualifiers(GENE_SYNONYM_NAME)) {
                            String synonymName = synonymQualifier.getValue();
                            newSynonymValues.add(synonymName);
                        }

                        /**
                         * if there are mismatches - add to the list of unreliable locus_tag/gene qualifiers...
                         */
                        if (!Arrays.deepEquals(registeredSynonyms.toArray(), newSynonymValues.toArray())) {
                            unreliableIdentifiers.add(identifier);
                            identifier2GeneSynonyms.remove(identifier);
                        }
                    } else {
                        //...otherwise set the master list
                        setMasterSynonymFeature(identifier2GeneSynonyms, identifier, currentFeature);
                    }
                }
            }
        }

        QualifierFactory qualifierFactory = new QualifierFactory();

        for (Feature currentFeature : locusAndGeneFeatures) {

            /**
             * we know this contains a locus_tag qualifier cos that's how we built the list
             */
            String identifier;
            if (!currentFeature.getQualifiers(LOCUS_TAG_QUALIFIER_NAME).isEmpty()) {
                identifier = currentFeature.getQualifiers(LOCUS_TAG_QUALIFIER_NAME).get(0).getValue();
            } else if (!currentFeature.getQualifiers(GENE_QUALIFIER_NAME).isEmpty()) {
                identifier = currentFeature.getQualifiers(GENE_QUALIFIER_NAME).get(0).getValue();
            } else {
                continue;//this should not be possible as the list was built with features containing at least 1 of the above
            }

            if (!unreliableIdentifiers.contains(identifier)) {
                if (identifier2GeneSynonyms.containsKey(identifier)) {

                    /**
                     * make a list of the gene synonyms associated with this feature...
                     */
                    List<String> currentSynonymValues = new ArrayList<String>();
                    for (Qualifier synonymQualifier : currentFeature.getQualifiers(GENE_SYNONYM_NAME)) {
                        String synonymName = synonymQualifier.getValue();
                        currentSynonymValues.add(synonymName);
                    }

                    /**
                     * ...add the synonyms where not present from the master list...
                     */
                    List<String> masterSynonyms = identifier2GeneSynonyms.get(identifier);
                    for(String registeredSynonym : masterSynonyms){
                        if(!SequenceEntryUtils.isQualifierWithValueAvailable(GENE_SYNONYM_NAME, registeredSynonym, currentFeature)){
                            currentFeature.addQualifier(qualifierFactory.createQualifier(GENE_SYNONYM_NAME, registeredSynonym));
                            reportMessage(Severity.FIX, currentFeature.getOrigin(), MESSAGE_ID_ADDED_SYNONYM, registeredSynonym, identifier);
                        }
                    }

                    /**
                     * then look at all the synonyms associated with this feature and remove those not present in the
                     * master list
                     */
                    for(String synonymValue : currentSynonymValues){
                        if(!masterSynonyms.contains(synonymValue)){
                            currentFeature.removeQualifiersWithValue(GENE_SYNONYM_NAME, synonymValue);
                            reportMessage(Severity.FIX, currentFeature.getOrigin(), MESSAGE_ID_REMOVED_SYNONYM, synonymValue, identifier);
                        }
                    }
                } else {
                    //this locus tag is now reserved by this set of gene synonyms
                    setMasterSynonymFeature(identifier2GeneSynonyms, identifier, currentFeature);
                }
            }
        }

        return result;
    }

    private void setMasterSynonymFeature(HashMap<String, List<String>> locusTag2GeneSynonyms,
                                         String identifier, Feature feature) {

        List<String> geneSynonymValues = new ArrayList<String>();
        for (Qualifier synonymQualifier : feature.getQualifiers(GENE_SYNONYM_NAME)) {
            String synonymName = synonymQualifier.getValue();
            geneSynonymValues.add(synonymName);
        }

        locusTag2GeneSynonyms.put(identifier, geneSynonymValues);
    }
}
