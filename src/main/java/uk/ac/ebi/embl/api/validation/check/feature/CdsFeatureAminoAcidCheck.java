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
import uk.ac.ebi.embl.api.entry.qualifier.AminoAcidFactory;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Invalid amino acid {0} in translation.")
public class CdsFeatureAminoAcidCheck extends FeatureValidationCheck {

	private final static String MESSAGE_ID = "CdsFeatureAminoAcidCheck";
	
	public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature instanceof CdsFeature) {
			CdsFeature cdsFeature = (CdsFeature) feature;
			String translation = cdsFeature.getTranslation();
			if (translation == null) {
				return result;
			}
			AminoAcidFactory factory = new AminoAcidFactory();
			for (int i = 0; i < translation.length(); ++i) {
				Character aminoAcid = translation.charAt(i);
				if (factory.createAminoAcid(aminoAcid) == null
						|| factory.createAminoAcid(aminoAcid).getLetter().equals('*')) {
					reportError(feature.getOrigin(), MESSAGE_ID, aminoAcid);
					return result;
				}
			}
		}
		return result;
	}
}
