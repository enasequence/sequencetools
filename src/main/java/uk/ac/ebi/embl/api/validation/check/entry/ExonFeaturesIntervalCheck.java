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
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Abutting features cannot be adjacent between neighbouring exons.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class ExonFeaturesIntervalCheck extends EntryValidationCheck
{

	private final static String MESSAGE_ID = "ExonFeaturesIntervalCheck";

	public ValidationResult check(Entry entry)
	{
		result = new ValidationResult();
		List<Feature> exonFeatures = SequenceEntryUtils.getFeatures(Feature.EXON_FEATURE_NAME, entry);
		List<Feature> filteredExonFeatures = new ArrayList<>();

		//filter non remote location exons
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
		String prevLocusTag = null;
		String prevGene = null;
		Long prevEndLocation = null;

		for (Feature feature : sortedFeatures)
		{
			String currLocusTag = feature.getSingleQualifierValue(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
			String currGene = feature.getSingleQualifierValue(Qualifier.GENE_QUALIFIER_NAME);

			if (prevEndLocation != null)
			{
				Long beginLocation = feature.getLocations().getMinPosition();

				if (beginLocation - prevEndLocation == 1 )
				{

					//if both feature has gene and locus_tag gene has priority, compare both gene and add error message if same.
					//if one feature has one of them and the other is not add error.
					//if gene is missing in one of them and locus is available in both then just compare local and add error if same
					if( (currGene == null && prevGene == null) ) {
						if (currLocusTag == null || (prevLocusTag == null || currLocusTag.equalsIgnoreCase(prevLocusTag)) ) {
							reportError(feature.getOrigin(), MESSAGE_ID);
						}
					} else {
						if(currGene != null && prevGene != null) {
							if(currGene.equalsIgnoreCase(prevGene)){
								reportError(feature.getOrigin(), MESSAGE_ID);
							}
						} else if(currLocusTag != null && prevLocusTag != null) {
							if(currLocusTag.equalsIgnoreCase(prevLocusTag)){
								reportError(feature.getOrigin(), MESSAGE_ID);
							}
						} else  {
							reportError(feature.getOrigin(), MESSAGE_ID);
						}
					}

				}
			}

			prevEndLocation = feature.getLocations().getMaxPosition();
			prevLocusTag = currLocusTag;
			prevGene = currGene;
		}

		return result;
	}

	public List<Feature> getSortedExonFeatures(List<Feature> exonFeatures)
	{
		Collections.sort(exonFeatures, (feature1, feature2)->(feature1.getLocations().getMinPosition() < feature2.getLocations().getMinPosition()) ? -1 :(feature1.getLocations().getMinPosition() > feature2.getLocations().getMinPosition())?1:0);
		return exonFeatures;
	}
}
