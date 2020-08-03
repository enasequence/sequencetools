/*
 * # Copyright 2012-2012 EMBL-EBI, Hinxton outstation
 *
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 *
# http://www.apache.org/licenses/LICENSE-2.0
 *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

public class SourceQualifierFix extends EntryValidationCheck
{

	private static final String QUALIFIER_NAME_CHANGE = "QualifierNameChange";
	private static final String QUALIFIER_VALUE_CHANGE = "QualifierValueChange";


	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();

		if (entry == null || entry.getPrimarySourceFeature() == null || entry.getPrimarySourceFeature().getQualifiers().size() == 0)
		{
			return result;
		}

		SourceFeature source = entry.getPrimarySourceFeature();
		String scientificName = source.getScientificName();

		if (scientificName != null && scientificName.toLowerCase().contains("salmonella"))
		{
			Qualifier seroTypeQual = entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SEROTYPE_QUALIFIER_NAME);
			if(seroTypeQual != null) {
				QualifierFactory qualFactory = new QualifierFactory();
				qualFactory.createQualifier(Qualifier.SEROVAR_QUALIFIER_NAME,seroTypeQual.getValue());
				source.addQualifier(qualFactory.createQualifier(Qualifier.SEROVAR_QUALIFIER_NAME,seroTypeQual.getValue()));
				source.removeQualifier(Qualifier.SEROTYPE_QUALIFIER_NAME);
				reportMessage(Severity.FIX, source.getOrigin(), QUALIFIER_NAME_CHANGE, Qualifier.SEROTYPE_QUALIFIER_NAME, Qualifier.SEROVAR_QUALIFIER_NAME);
			}
			Qualifier seroVarQual = entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.SEROVAR_QUALIFIER_NAME);
			if(seroVarQual != null) {
				String oldVal = seroVarQual.getValue();
				if(oldVal.toLowerCase().contains("serotype")) {
					seroVarQual.setValue(oldVal.replaceAll("(?i)serotype", ""));
					reportMessage(Severity.FIX, source.getOrigin(), QUALIFIER_VALUE_CHANGE, Qualifier.SEROVAR_QUALIFIER_NAME, oldVal, seroVarQual.getValue());
				}
			}
			
		}

		return result;
	}



}
