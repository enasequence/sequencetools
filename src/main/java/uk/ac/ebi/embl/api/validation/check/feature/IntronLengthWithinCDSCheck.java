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
package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Intron usually expected to be at least 10 nt long. Please check the accuracy.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class IntronLengthWithinCDSCheck extends FeatureValidationCheck {

    private final static String MESSAGE_ID = "IntronLengthWithinCDSCheck_1";

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature instanceof CdsFeature) {
            CdsFeature cdsFeature = (CdsFeature) feature;
            Location prevLocation = null;
            boolean hasArtificialLocation = SequenceEntryUtils.isQualifierAvailable(Qualifier.ARTIFICIAL_LOCATION, cdsFeature);
            boolean hasribosomal_slippage = SequenceEntryUtils.isQualifierAvailable(Qualifier.RIBOSOMAL_SLIPPAGE_QUALIFIER_NAME, cdsFeature);
            boolean hastrans_splicing = SequenceEntryUtils.isQualifierAvailable(Qualifier.TRANS_SPLICING, cdsFeature);

            if ((hasribosomal_slippage || hastrans_splicing || cdsFeature.isPseudo())) {
                return result;
            }
            CompoundLocation<Location> cdsLocations = cdsFeature.getLocations().getSortedLocations();
            for (Location location : cdsLocations.getLocations()) {
                if (prevLocation == null) {
                    prevLocation = location;
                    continue;
                }

                Long intron = location.getBeginPosition() - prevLocation.getEndPosition();
                if (intron >= 0 && intron < 10) {
                    if (hasArtificialLocation)
                        return result;
                    else {
                        ValidationMessage<Origin> message = reportError(cdsFeature.getOrigin(), MESSAGE_ID);
                        message.setCuratorMessage("Intron usually expected to be at least 10 nt long. Please check accuracy and Use one of the following options for annotation: \n /artificial_location=\"heterogeneous population sequenced\" \n OR \n /artificial_location=\"low-quality sequence region\". \n Alternatively, use where appropriate: \n /pseudo, /pseudogene, /trans_splicing, /ribosomal_slippage");

                    }
                }
                prevLocation = location;

            }
        }
        return result;
    }
}
