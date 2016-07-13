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
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

/**
 * Checks that any duplicate features exist
 */
@Description("")
public class DuplicateFeatureCheck extends EntryValidationCheck
{
	
	boolean duplicateLocation = false;
	protected final static String MESSAGE_ID = "DuplicateFeatureCheck1";
	protected final static String DUPLICATE_CDS_PROTEIN_MESSAGE_ID = "DuplicateFeatureCheck2";
	protected final static String DUPLICATE_CDS_CODON_START_MESSAGE_ID = "DuplicateFeatureCheck3";
	protected final static String DUPLICATE_SOURCE_ORGANISM_MESSAGE_ID = "DuplicateFeatureCheck4";
	protected final static String DIFFERENT_SOURCE_ORGANISM_MESSAGE_ID = "DuplicateFeatureCheck5";
	private static HashMap<MultiKey, Feature> featureMap;
	List<Feature> features;
	private static List<String> organismList;
	private static BidiMap protein_idMap;

	public ValidationResult check(Entry entry)
	{
		features = new ArrayList<Feature>();
		organismList = new ArrayList<String>();
		protein_idMap = new DualHashBidiMap();

		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		featureMap = new HashMap<MultiKey, Feature>();
		features = entry.getFeatures();

		// System.out.println("protein_id map size:"+protein_idMap.size());
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

				} else if (feature.getName().equals(Feature.CDS_FEATURE_NAME))
				{
					try
					{
						Integer newCodon_start = 0;
						Integer oldCodon_start = 0;
						CdsFeature newcdsFeature = (CdsFeature) feature;
						CdsFeature oldcdsFeature = (CdsFeature) oldFeature;
						if (SequenceEntryUtils.isQualifierAvailable(Qualifier.CODON_START_QUALIFIER_NAME, newcdsFeature))
							newCodon_start = ((CdsFeature) feature).getStartCodon();
						if (SequenceEntryUtils.isQualifierAvailable(Qualifier.CODON_START_QUALIFIER_NAME, oldcdsFeature))
							oldCodon_start = ((CdsFeature) oldFeature).getStartCodon();
						if (newCodon_start.equals(oldCodon_start) || (oldCodon_start == 0 && newCodon_start == 1)
								|| (oldCodon_start == 1 && newCodon_start == 0))
						{
							ValidationMessage<Origin> message = reportError(oldFeature.getOrigin(), DUPLICATE_CDS_CODON_START_MESSAGE_ID,
									oldCodon_start);
							message.append(feature.getOrigin());

							FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(oldFeature, message);
							FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(feature, message);
						}
						
					} catch (ValidationException e)
					{
					}
				}

				else
				{
					ValidationMessage<Origin> message = reportError(feature.getOrigin(), MESSAGE_ID, feature.getName());
					message.append(oldFeature.getOrigin());

					FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(feature, message);
					FeatureValidationCheck.appendLocusTadAndGeneIDToMessage(oldFeature, message);
				}
			}
		}
		// System.out.println("protein_id map size:"+protein_idMap.size());
		return result;
	}

	void addMultiKeyAndValue(MultiKey key, Feature feature)
	{
		featureMap.put(key, feature);
	}
}
