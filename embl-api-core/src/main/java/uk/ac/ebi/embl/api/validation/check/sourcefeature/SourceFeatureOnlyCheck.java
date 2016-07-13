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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.Arrays;

/**
 * @author dlbower
 * Only called when in template mode (for submissions) - point out to submitter that they only have source features
 * - should have more than this
 */
@Description("Entry only has source feature - submissions usually only accepted with sequence features," +
        "Entry has no \"source\" feature all entries must have a \"source\" feature.")
public class SourceFeatureOnlyCheck extends EntryValidationCheck {

    public final static String MESSAGE_ID_SOURCE_ONLY = "SourceFeatureOnlyCheck";
    public final static String MESSAGE_ID_NO_SOURCE = "SourceFeatureOnlyCheck-1";

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        int sourcesNumber = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry).size();

        if (sourcesNumber == 0) {
            ValidationMessage<Origin> message =
                    EntryValidations.createMessage(entry.getOrigin(), Severity.ERROR, MESSAGE_ID_NO_SOURCE);
            result.append(message);
        }
        
        int othersNumber = entry.getFeatures().size();

        if (getEmblEntryValidationPlanProperty().validationScope.get() == ValidationScope.EMBL_TEMPLATE&& othersNumber == sourcesNumber) {
            /**
             * dont run this if entries are wgs or est - they do not need sequence features
             */
            String dataClass = entry.getDataClass();
            if (dataClass != null && Arrays.asList(Entry.EST_DATACLASS, Entry.WGS_DATACLASS, Entry.GSS_DATACLASS,Entry.TSA_DATACLASS).contains(dataClass)) {
                return result;
            }

            SourceFeature sourceFeature=entry.getPrimarySourceFeature();
            String scientificName=sourceFeature.getScientificName();
            
            if(scientificName!=null&& scientificName.toLowerCase().contains("viroid"))
            {
            	return result;
            }
           
            ValidationMessage<Origin> message =
                        EntryValidations.createMessage(entry.getOrigin(), Severity.WARNING, MESSAGE_ID_SOURCE_ONLY);
                result.append(message);
           
        }

        return result;
    }
}
