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
 * Checks that there is a one-to-one association between gene features and locus tag qualifier values 
 */
@Description("Different gene features can not share the same \"locus_tag\" qualifier \\\"{0}\\\".")
public class GeneFeatureLocusTagCheck extends EntryValidationCheck {

    protected final static String MESSAGE_ID = "GeneFeatureLocusTagCheck";

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

        //collect all gene features
        Collection<Feature> geneFeatures =
                SequenceEntryUtils.getFeatures(Feature.GENE_FEATURE_NAME, entry);

        if (geneFeatures.isEmpty()) {
            return result;
        }

        /**
         * which locus tag qualifiers are mapped to which gene features - should be an exclusive association
         */
        HashMap<String, Feature> locusTag2GeneFeature = new HashMap<String, Feature>();

        for (Feature geneFeature : geneFeatures) {

            List<Qualifier> locusQualifiers = geneFeature.getQualifiers(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
            if (locusQualifiers.size() > 0) {

                String locus_tag_name = locusQualifiers.get(0).getValue();//got other checks for multiple locus_tags

                if (locusTag2GeneFeature.containsKey(locus_tag_name)) {
                    ValidationMessage<Origin> message =
                            reportError(result, geneFeature.getOrigin(), MESSAGE_ID, locus_tag_name);
                    message.append(locusTag2GeneFeature.get(locus_tag_name).getOrigin());
                } else {
                    locusTag2GeneFeature.put(locus_tag_name, geneFeature);
                }

            }
        }

        return result;
    }

    /**
     * Adds error to the result.
     *
     * @param result a reference to validation result
     * @param origin the origin
     */
    private ValidationMessage<Origin> reportError(ValidationResult result, Origin origin, String messageId,
                                                  Object... params) {
        ValidationMessage<Origin> message =
                EntryValidations.createMessage(origin, Severity.ERROR, messageId, params);
        result.append(message);
        return message;
    }

}
