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
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

/**
 * @author dlbower
 */
@Description("Entry has no \"source\" feature all entries must have a \"source\" feature.")
public class SourceFeatureOnlyCheck extends EntryValidationCheck {

    public final static String MESSAGE_ID_NO_SOURCE = "SourceFeatureOnlyCheck-1";

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();

        int sourcesNumber = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry).size();

        if (sourcesNumber == 0) {
            ValidationMessage<Origin> message =
                    EntryValidations.createMessage(entry.getOrigin(), Severity.ERROR, MESSAGE_ID_NO_SOURCE);
            result.append(message);
        }

        return result;
    }
}
