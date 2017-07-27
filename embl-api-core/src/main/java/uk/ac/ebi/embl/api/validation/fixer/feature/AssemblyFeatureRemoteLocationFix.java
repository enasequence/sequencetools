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

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;


@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})
@GroupIncludeScope(group={ValidationScope.Group.ASSEMBLY})
public class AssemblyFeatureRemoteLocationFix extends FeatureValidationCheck
{
	private static final String REMOTE_LOCATION_FIX_ID = "AssemblyFeatureRemoteLocationFix";
	public ValidationResult check(Feature feature) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (feature == null)
		{
			return result;
		}

		CompoundLocation<Location> compoundLocation = feature.getLocations();

		if (compoundLocation == null || compoundLocation.getLocations() == null
				|| compoundLocation.getLocations().size() == 0)
		{
			return result;
		}
		if (!compoundLocation.hasRemoteLocation())
		{
			return result;
		}
		try
		{

			for (Location location : compoundLocation.getLocations())
			{
				if (location instanceof RemoteLocation)
				{
					String remoteAccession=((RemoteLocation) location).getAccession()+"."+((RemoteLocation) location).getVersion();
					String sequenceAccession=getEntryDAOUtils().getAssemblyEntryAccession(((RemoteLocation) location).getAccession());
					if(sequenceAccession!=null)
					{
					((RemoteLocation) location).setAccession(sequenceAccession.split("\\.")[0]);
					((RemoteLocation) location).setVersion(new Integer(sequenceAccession.split("\\.")[1]));
					if(!remoteAccession.equals(sequenceAccession))
					{
						reportMessage(Severity.FIX, feature.getOrigin(),REMOTE_LOCATION_FIX_ID,feature.getName());
					}
					}
				}
			}

			feature.setLocations(compoundLocation);
		} catch (Exception e)
		{
              throw new ValidationEngineException(e.getMessage());
		}
		return result;
	}

}
