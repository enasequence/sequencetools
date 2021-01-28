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
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

import java.util.Collection;

@Description("Entries of type \"WGS\" must not contain \"gap\" features.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class WGSGapCheck extends EntryValidationCheck {

    private final static String GAPS_PRESENT_ID = "WGSGapCheck";

    public WGSGapCheck() {
    }

    public void setPopulated() {
        super.setPopulated();
    }

    public ValidationResult check(Entry entry) {

        result = new ValidationResult();

        if (entry == null) {
            return result;
        }

        String dataclass = entry.getDataClass();
        
        if(dataclass != null && dataclass.equals(Entry.WGS_DATACLASS)){

        	if(SequenceEntryUtils.isFeatureAvailable(Feature.GAP_FEATURE_NAME, entry)||SequenceEntryUtils.isFeatureAvailable(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry)){
        		
        		Collection<Feature> features = SequenceEntryUtils.getFeatures(Feature.GAP_FEATURE_NAME, entry);
        		if(features.isEmpty())
        		{
        			features=SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
        		}
        		
                ValidationMessage<Origin> message =
                    reportWarning(null, GAPS_PRESENT_ID);
                
                for(Feature feature : features){
                	message.append(feature.getOrigin());
                }
        	}
        	
        }

        return result;
    }

}
