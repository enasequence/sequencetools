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

import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("")
public class CdsFeatureCheck extends EntryValidationCheck {

    private static final String EXCEPTION_TRANSLATION_ID = "CdsFeatureCheck-1";
    private static final String MRNA_LOCATION_COMPLEMENT_ID = "CdsFeatureCheck-2";
    private static final String MRNA_LOCATION_JOIN_ID = "CdsFeatureCheck-3";

     public ValidationResult check(Entry entry) 
     {
        result = new ValidationResult();

        if (entry == null||entry.getFeatures()==null||entry.getFeatures().size()==0) {
			return result;
		}
        
        List<Feature> cdsFeatures=SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry);
        if(cdsFeatures.size()==0)
        	return result;
        
        String molType= null;
        if(entry.getSequence()!=null)
        {
        	molType=entry.getSequence().getMoleculeType();
        }
        
		for (Feature cdsFeature : cdsFeatures) 
		{
			CdsFeature feature = (CdsFeature) cdsFeature;
			List<Qualifier> exceptionQualifiers = feature.getQualifiers(Qualifier.EXCEPTION_QUALIFIER_NAME);
			List<Qualifier> translationQualifiers = feature.getQualifiers(Qualifier.TRANSLATION_QUALIFIER_NAME);
			List<Qualifier> ribosomal_slippageQualifiers = feature.getQualifiers(Qualifier.RIBOSOMAL_SLIPPAGE);

			if (exceptionQualifiers.size() > 0&& translationQualifiers.size() == 0) 
			{
				reportError(feature.getOrigin(), EXCEPTION_TRANSLATION_ID);
			}
			if (molType != null && Sequence.MRNA_MOLTYPE.equals(molType)) 
			{
				if (feature.getLocations().isComplement()) {
					reportError(feature.getOrigin(),MRNA_LOCATION_COMPLEMENT_ID);
				}
				if (exceptionQualifiers.size() == 0&& ribosomal_slippageQualifiers.size() == 0&&cdsFeature.getLocations()!=null) {
					if (feature.getLocations() instanceof Join<?>) 
					{
					 reportError(feature.getOrigin(), MRNA_LOCATION_JOIN_ID);
					}
				}
			}

		}
               
 	    return result;
	}

}
