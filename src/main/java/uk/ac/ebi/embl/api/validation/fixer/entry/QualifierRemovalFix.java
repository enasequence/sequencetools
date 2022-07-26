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
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.List;

@Description("Qualifier \"{0}\" removed from feature \"{1}\"")
@ExcludeScope(validationScope = {ValidationScope.NCBI , ValidationScope.NCBI_MASTER})
public class QualifierRemovalFix extends EntryValidationCheck {
	private final static String MESSAGE_ID = "RemoveCitationQualifierFix";
	public QualifierRemovalFix() {
	}

	public ValidationResult check(Entry entry) {
		for(Feature feature: entry.getFeatures()){
			if(! feature.getName().equalsIgnoreCase(Feature.OLD_SEQUENCE_FEATURE_NAME)){
				List<Qualifier> qualifiers = feature.getQualifiers(Qualifier.CITATION_QUALIFIER_NAME);
				qualifiers.addAll(feature.getQualifiers(Qualifier.COMPARE_QUALIFIER_NAME));
				if(null != qualifiers){
					for (Qualifier qualifier: qualifiers) {
						feature.removeQualifier(qualifier);
						reportMessage(Severity.FIX, feature.getOrigin(), MESSAGE_ID, qualifier.getName(), feature.getName());
					}
				}
			}
		}
		return result;
	}
}
