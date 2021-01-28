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

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.helper.location.LocationToStringCoverter;

@Description("Feature \"{0}\" has remote location,validator requires database connection to validate features having remote locations"
		+ "Invalid remote feature Location \"{0}\" , Location range is not within entry \"{1}\" sequence length.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class FeaturewithRemoteLocationCheck extends EntryValidationCheck
{

	private final static String DBCONNECTION_MESSAGE_ID = "FeaturewithRemoteLocationCheck-1";
	private final static String INVALID_LOCATION_MESSAGE_ID = "FeaturewithRemoteLocationCheck-2";

	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		if (entry.getFeatures() == null || entry.getFeatures().isEmpty())
		{
			return result;
		}
		for (Feature feature : entry.getFeatures())
		{

			if (feature.getLocations() != null && feature.getLocations().hasRemoteLocation() && getEmblEntryValidationPlanProperty().enproConnection.get() == null)
			{
				reportWarning(feature.getOrigin(), DBCONNECTION_MESSAGE_ID, feature.getName());
			}
			if (feature.getLocations().hasRemoteLocation() && getEntryDAOUtils() != null)
			{
				for (Location location : feature.getLocations().getLocations())
				{
					if (location instanceof RemoteLocation)
					{
						try
						{
							if (!validateRemoteLocation(location))
							{
								StringBuilder locationBlock = new StringBuilder();
								LocationToStringCoverter.renderLocation(locationBlock, location, false, false);
								reportError(feature.getOrigin(), INVALID_LOCATION_MESSAGE_ID, locationBlock, ((RemoteLocation) location).getAccession());
							}
						} catch (SQLException e)
						{
							throw new ValidationEngineException(e);
						}
					}
				}

			}

		}

		return result;
	}

	boolean validateRemoteLocation(Location location) throws SQLException
	{
		RemoteLocation remoteLocation = (RemoteLocation) location;
		String accession=remoteLocation.getAccession()+(remoteLocation.getVersion()!=null?"."+remoteLocation.getVersion():"");
		Long seqLength = getEntryDAOUtils().getSequenceLength(accession);

		if (location.getBeginPosition() < 0 || location.getBeginPosition() > seqLength || location.getEndPosition() > seqLength)
		{
			return false;
		}
		return true;
	}

}
