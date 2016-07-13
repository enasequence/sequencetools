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
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Description("type_material value contains invalid organism name \"{0}\"")
public class Type_materialQualifierCheck extends FeatureValidationCheck
{

	private final static String MESSAGE_ID = "type_materialQualifierCheck1";
	Pattern type_materialPattern = Pattern.compile("^\\s*(.*)\\s* of\\s*(.*)\\s*$");

	public Type_materialQualifierCheck()
	{
	}

	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();

		if (feature == null||!(feature instanceof SourceFeature))
		{
			return result;
		}
		
		SourceFeature source=(SourceFeature) feature;
		List<Qualifier> type_materialQualifiers=feature.getQualifiers(Qualifier.TYPE_MATERIAL_QUALIFIER_NAME);
		
		if (type_materialQualifiers!=null&&type_materialQualifiers.size()!=0)
		{
			String organism =source.getScientificName();
			if (organism == null)
			{
				return result;
			}
			for (Qualifier qualifier : type_materialQualifiers)
			{
				String qualifierValue = qualifier.getValue();
				Matcher matcher = type_materialPattern.matcher(qualifierValue);
				if (matcher.matches())
				{
					String organismGroup = matcher.group(2);
					if (organismGroup != null && !organismGroup.trim().equals(organism))
					{
						reportError(qualifier.getOrigin(), MESSAGE_ID, organism);
					}
				}
			}
		}

		return result;
	}

}
