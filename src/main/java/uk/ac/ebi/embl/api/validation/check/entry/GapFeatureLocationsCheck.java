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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

import java.util.Collection;

/**
 * Checks that features sharing the same locus tag are associated with the same
 * gene and a stable list of gene_synonyms
 */
@Description("bases immediately adjacent to gap location should not be 'n'")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class GapFeatureLocationsCheck extends EntryValidationCheck {

	protected final static String MESSAGE_ID = "GapFeatureLocationsCheck";

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();

		if (entry == null) {
			return result;
		}

		// collect all gene features
		Collection<Feature> gapFeatures = SequenceEntryUtils.getFeatures(
				Feature.GAP_FEATURE_NAME, entry);
			
		gapFeatures.addAll(SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry));
	

		if (gapFeatures.isEmpty())
		{
			gapFeatures = SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
		}
		if (gapFeatures.isEmpty())
		{
			return result;
		}

		if (entry.getSequence() == null
				|| entry.getSequence().getSequenceByte() == null) {
			return result;
		}

		for (Feature gapFeature : gapFeatures) {

			CompoundLocation<Location> compoundLocation = gapFeature.getLocations();
			if (compoundLocation == null
					|| compoundLocation.getLocations() == null
					|| compoundLocation.getLocations().size() != 1) {

				return result;
			}

			Location location = compoundLocation.getLocations().get(0);
			int start = location.getBeginPosition().intValue();
			int end = location.getEndPosition().intValue();
			
			byte[] sequenceByte= entry.getSequence().getSequenceByte();
			
			if(sequenceByte==null)
				return result;

			if(start<=1||sequenceByte.length<=end)
			{
				return result;
			}

			if('n'==(char)sequenceByte[start-2]||'n'==(char)sequenceByte[end])
			{
				reportError(gapFeature.getOrigin(), MESSAGE_ID);
			}

		}

		return result;
	}
}
