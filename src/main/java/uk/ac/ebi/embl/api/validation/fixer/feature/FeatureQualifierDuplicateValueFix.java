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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import java.util.ArrayList;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("Removed the qualifier {1}in feature {2},since {0},{1} qualifiers values are duplicated in the same feature"
		+ "removed the qualifier {0} in feature {2},since it has duplicated value {1} in the same feature ")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class FeatureQualifierDuplicateValueFix extends FeatureValidationCheck {

	private static final String DIFF_QUALIFIER_VALUES_DUPLICATED = "FeatureQualifierDuplicateValueFix_1";
	private static final String QUALIFIER_VALUES_DUPLICATED = "FeatureQualifierDuplicateValueFix_2";

	public ValidationResult check(Feature feature) {
		result = new ValidationResult();

		if (feature == null) {
			return result;
		}

		String locusTag_Value = null;
		ArrayList<String> oldLocusTag_Values = new ArrayList<>();

		for (Qualifier qualifier : feature.getQualifiers()) {
			if (qualifier.getName().equals(Qualifier.LOCUS_TAG_QUALIFIER_NAME)) {
				locusTag_Value = qualifier.getValue();

			}
		}
		for (Qualifier qualifier : feature
				.getQualifiers(Qualifier.OLD_LOCUS_TAG)) {
			if (!oldLocusTag_Values.contains(qualifier.getValue())) {
				oldLocusTag_Values.add(qualifier.getValue());
			} else {
				feature.removeQualifier(qualifier);
				reportMessage(Severity.FIX, qualifier.getOrigin(),
						QUALIFIER_VALUES_DUPLICATED, qualifier.getName(),
						qualifier.getValue(), feature.getName());
			}

		}
		if (oldLocusTag_Values.size() != 0 && locusTag_Value != null) {
			if (oldLocusTag_Values.contains(locusTag_Value)) {
				feature.removeQualifiersWithValue(Qualifier.OLD_LOCUS_TAG,
						locusTag_Value);
				reportMessage(Severity.FIX, feature.getOrigin(),
						DIFF_QUALIFIER_VALUES_DUPLICATED,
						Qualifier.LOCUS_TAG_QUALIFIER_NAME,
						Qualifier.OLD_LOCUS_TAG, feature.getName());
			}
		}
		return result;
	}

}
