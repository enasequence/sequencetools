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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Abutting features cannot be adjacent between neighbouring exons.")
public class ExonFeaturesIntervalCheck extends EntryValidationCheck
{

	private final static String MESSAGE_ID = "ExonFeaturesIntervalCheck";

	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();
		List<Feature> exonFeatures = SequenceEntryUtils.getFeatures(Feature.EXON_FEATURE_NAME, entry);
		List<Feature> filteredExonFeatures = new ArrayList<Feature>();
		for (Feature exonFeature : exonFeatures)
		{
			if (!(exonFeature.getLocations() instanceof RemoteLocation))
			{
				boolean isremoteLocation = false;
				for (Location location : exonFeature.getLocations().getLocations())
				{
					if (location instanceof RemoteRange)
					{
						isremoteLocation = true;
						break;
					}
				}
				if (!isremoteLocation)
					filteredExonFeatures.add(exonFeature);
			}

		}
		if (filteredExonFeatures.size() == 0)
			return result;
		List<Feature> sortedFeatures = getSortedExonFeatures(filteredExonFeatures);
		Long prevEndLocation = null;
		for (Feature feature : sortedFeatures)
		{
			if (prevEndLocation != null)
			{
				Long beginLocation = feature.getLocations().getMinPosition();

				if (beginLocation - prevEndLocation == 1)
				{
					reportError(feature.getOrigin(), MESSAGE_ID);
				}
			}

			prevEndLocation = feature.getLocations().getMaxPosition();
		}

		return result;
	}

	public List<Feature> getSortedExonFeatures(List<Feature> exonFeatures)
	{
		Collections.sort(exonFeatures, (feature1, feature2)->(feature1.getLocations().getMinPosition() < feature2.getLocations().getMinPosition()) ? -1 : 1);
		return exonFeatures;
	}
}
