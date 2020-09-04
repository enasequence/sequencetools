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
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

/**
 * Checks that any duplicate features exist
 */
@Description("")
@ExcludeScope(validationScope = {ValidationScope.NCBI , ValidationScope.NCBI_MASTER})
public class DuplicateFeatureCheck extends EntryValidationCheck
{

	protected final static String DUPLICATE_FEATURE_LOCATIONS = "DuplicateFeatureLocations";
	protected final static String DUPLICATE_CDS_PROTEIN_MESSAGE_ID = "DuplicateProteinAccession";
	protected final static String DUPLICATE_SOURCE_ORGANISM_MESSAGE_ID = "DuplicateOrganismAndLocation";
	private static HashMap<MultiKey, Feature> featureMap;
	List<Feature> features;

	public ValidationResult check(Entry entry)
	{
		features = new ArrayList<>();
		List<String>  organismList = new ArrayList<>();
		BidiMap protein_idMap = new DualHashBidiMap();

		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		featureMap = new HashMap<>();
		features = entry.getFeatures();

		for (Feature feature : features)
		{
			boolean duplicateOrganism = false;
			String organismValue = null;
			String featureName = feature.getName();
			if (featureName.equals(Feature.SOURCE_FEATURE_NAME))
			{
				Qualifier organism = SequenceEntryUtils.getQualifier(Qualifier.ORGANISM_QUALIFIER_NAME, feature);
				if (organism != null)
					organismValue = organism.getValue();
				if (organismValue != null && !organismList.isEmpty() && organismList.contains(organismValue))
				{
					duplicateOrganism = true;
				}
				if (organismValue != null)
					organismList.add(organismValue);
			}
			try
			{
				if (featureName.equals(Feature.CDS_FEATURE_NAME))
				{
					String newProtein_id = ((CdsFeature) feature).getProteinAccession();
					if (newProtein_id != null && !protein_idMap.isEmpty() && protein_idMap.containsValue(newProtein_id))
					{
						Feature oldFeature = (Feature) protein_idMap.getKey(newProtein_id);

						ValidationMessage<Origin> message = reportError(feature.getOrigin(), DUPLICATE_CDS_PROTEIN_MESSAGE_ID, newProtein_id);
						message.append(feature.getOrigin());

						FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(oldFeature, message);
						FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(feature, message);
					} else if (newProtein_id != null)
						protein_idMap.put(feature, newProtein_id);
				}
			} catch (ValidationException e)
			{
			}
			MultiKey key = new MultiKey(featureName, feature.getLocations());
			if (!featureMap.containsKey(key))
			{
				addMultiKeyAndValue(key, feature);
			} else
			{
				Feature oldFeature = featureMap.get(key);
				if (featureName.equals(Feature.SOURCE_FEATURE_NAME))
				{
					if (duplicateOrganism)
					{
						ValidationMessage<Origin> message = reportError(oldFeature.getOrigin(), DUPLICATE_SOURCE_ORGANISM_MESSAGE_ID, organismValue,
								oldFeature.getLocations().getMinPosition(), oldFeature.getLocations().getMaxPosition());
						message.append(feature.getOrigin());
						FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(oldFeature, message);
						FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(feature, message);
					}

				} else if (!feature.getName().equals(Feature.CDS_FEATURE_NAME))
				{
					ValidationMessage<Origin> message = reportError(feature.getOrigin(), DUPLICATE_FEATURE_LOCATIONS, feature.getName());
					message.append(oldFeature.getOrigin());

					FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(feature, message);
					FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(oldFeature, message);
				}
			}
		}

		return result;
	}

	void addMultiKeyAndValue(MultiKey key, Feature feature)
	{
		featureMap.put(key, feature);
	}
}
