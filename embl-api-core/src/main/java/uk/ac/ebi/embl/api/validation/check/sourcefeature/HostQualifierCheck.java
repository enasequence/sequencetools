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

import java.util.List;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.taxonomy.Taxon;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
@RemoteExclude
public class HostQualifierCheck extends FeatureValidationCheck
{

	private final static String INVALID_HOST_MESSAGE_ID = "HostQualifierCheck_1";

	@Override
	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();

		if (feature == null||!(feature instanceof SourceFeature))
		{
			return result;
		}

		SourceFeature source=(SourceFeature)feature;
		List<Qualifier> hostQualifiers = source.getQualifiers(Qualifier.HOST_QUALIFIER_NAME);
		if(hostQualifiers.size()==0)
		{
			return result;
		}
		
		for (Qualifier hostQualifier : hostQualifiers)
		{
			String hostQualifierValue=hostQualifier.getValue();
			
			Taxon taxon=getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonsByCommonName(hostQualifierValue);
			if(taxon!=null)
			{
				reportError(hostQualifier.getOrigin(), INVALID_HOST_MESSAGE_ID,hostQualifier.getName());
			}
		}

		return result;

	}
}
