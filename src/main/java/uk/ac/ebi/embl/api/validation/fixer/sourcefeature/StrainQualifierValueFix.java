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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("strain qualifier value \"{0}\" has been changed to \"{1}\" " + "entry description \"{0}\" has been changed to \"{1}\"")
@ExcludeScope(validationScope = { ValidationScope.NCBI })
public class StrainQualifierValueFix extends EntryValidationCheck
{

	private final static String STRAIN_MESSAGE_ID = "StrainQualifierValueandDescriptionFix_1";
	
	public ValidationResult check(Entry entry)
	{

		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

		if (entry.getPrimarySourceFeature() == null)
		{
			return result;
		}

		SourceFeature source = entry.getPrimarySourceFeature();

		Qualifier strainQualifier = source.getSingleQualifier(Qualifier.STRAIN_QUALIFIER_NAME);

		if (strainQualifier == null)
			return result;

		String strainQualifierValue = strainQualifier.getValue();
		if (strainQualifierValue.endsWith("(T)"))
		{
			strainQualifierValue = strainQualifierValue.substring(0, strainQualifierValue.length() - 3);
		} else if (strainQualifierValue.endsWith("T"))
		{
			strainQualifierValue = strainQualifierValue.substring(0, strainQualifierValue.length() - 1);
		}
		boolean isOrganismFormal = getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismFormal(source.getScientificName());

		if (!isOrganismFormal)
		{
			if (strainQualifierValue.contains("type strain: "))
			{
				strainQualifierValue = strainQualifierValue.replace("type strain: ", "");
			}
		} else if ((strainQualifier.getValue().endsWith("(T)") || strainQualifier.getValue().endsWith("T")) && !strainQualifier.getValue().startsWith("type strain:"))
		{
			strainQualifierValue = "type strain: " + strainQualifierValue;
		}

		if (!strainQualifier.getValue().equals(strainQualifierValue))
		{
			reportMessage(Severity.FIX, entry.getOrigin(), STRAIN_MESSAGE_ID, strainQualifier.getValue(), strainQualifierValue);
			entry.getPrimarySourceFeature().getSingleQualifier(Qualifier.STRAIN_QUALIFIER_NAME).setValue(strainQualifierValue);
     	}

		return result;
	}
}
