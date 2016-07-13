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
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope.Group;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.util.List;

@Description("Gap features replaced with assembly_gap features as entry has assembly_gap and gap features and gap/assembly_gap features are mutually exclusive")

public class GaptoAssemblyGapFeatureFix extends EntryValidationCheck
{

	private final static String GAP_TO_ASSEMBLYGAP_MESSAGE_ID_1 = "GaptoAssemblyGapFeatureFix_1";
	
	public ValidationResult check(Entry entry)
	{
		if(!getEmblEntryValidationPlanProperty().validationScope.get().isInGroup(Group.ASSEMBLY)&&!getEmblEntryValidationPlanProperty().isAssembly.get())
    	{
    	  return result;
    	}
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

		List<Feature> gapFeautures = SequenceEntryUtils.getFeatures(
				Feature.GAP_FEATURE_NAME, entry);

		if (gapFeautures.size() == 0)
		{
			return result;
		}
		
		for(Feature feature:entry.getFeatures())
		{
			if(feature.getName().equals(Feature.GAP_FEATURE_NAME))
			{
				feature.setName(Feature.ASSEMBLY_GAP_FEATURE_NAME);
				feature.addQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME,"unknown");
				reportMessage(Severity.FIX, feature.getOrigin(),GAP_TO_ASSEMBLYGAP_MESSAGE_ID_1);
			}
			
		}

		return result;
	}

}
