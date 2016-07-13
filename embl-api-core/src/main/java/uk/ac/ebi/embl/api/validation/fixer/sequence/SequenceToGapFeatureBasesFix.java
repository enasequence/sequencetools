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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.ValidationScope.Group;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceToGapFeatureBasesCheck;
import static uk.ac.ebi.embl.api.validation.SequenceEntryUtils.getFeatures;
import static uk.ac.ebi.embl.api.validation.SequenceEntryUtils.getQualifier;

/**
 * Checks that extended stretches of n characters in the sequence are matched by
 * a gap feature
 */

@Description("gap feature created for \"{0}\" n characters between \"{1}\" and \"{2}\" "
		+ "{0} gap features created for the entry"
		+ "greater than 90% of newly created gap features are 100bp long,so the estimated_length qualifier value for these gap features is set to :\"unknown\"")
public class SequenceToGapFeatureBasesFix extends
		SequenceToGapFeatureBasesCheck
{

	protected final static String ASSEMBLY_GAP_MESSAGE_ID = "SequenceToGapFeatureBasesFix-1";
	protected final static String GAP_MESSAGE_ID = "SequenceToGapFeatureBasesFix-4";
	protected final static String COUNT_MESSAGE_ID = "SequenceToGapFeatureBasesFix-2";
	protected final static String ESTIMATED_LENGTH_ID = "SequenceToGapFeatureBasesFix-3";

	FeatureFactory features = new FeatureFactory();
	LocationFactory locationFactory = new LocationFactory();
	// private Entry entry;
	private static int newGapFeatureCount = 0;
	/**
	 * a count of the number of gap features that are of length 100 (the default
	 * value for unknown)
	 */
	private static int unknownEstimatedLength = 0;
		
	protected void processMissingGapFeature(Entry entry, NRegion nRegion)
	{
		// this.entry = entry;
		// newGapFeatureCount=0;
		if (getEmblEntryValidationPlanProperty().minGapLength.get() != 0)
		{
			ERROR_THRESHOLD = getEmblEntryValidationPlanProperty().minGapLength.get()-1;//>=minimum_gap_length 
		} else 
		{
			ERROR_THRESHOLD=Entry.DEFAULT_MIN_GAP_LENGTH;
		}
		if (nRegion.getLength() > ERROR_THRESHOLD)
		{

			Location location = locationFactory.createLocalRange(
					(long) nRegion.getStart(),
					(long) nRegion.getEnd());
			Order<Location> compoundJoin = new Order<Location>();
			compoundJoin.setSimpleLocation(true);
			compoundJoin.addLocation(location);
			Feature newGapFeature;
			if (getEmblEntryValidationPlanProperty().validationScope.get().isInGroup(Group.ASSEMBLY)||getEmblEntryValidationPlanProperty().isAssembly.get())
			{
				newGapFeature = features
						.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
				newGapFeature.setLocations(compoundJoin);
				newGapFeature.setSingleQualifierValue(
						Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME,
						Integer.toString(nRegion.getLength()));
				newGapFeature.setSingleQualifierValue(
						Qualifier.GAP_TYPE_QUALIFIER_NAME,
						"unknown");
				entry.addFeature(newGapFeature);
				newGapFeatureCount++;// number of gap features created
				if (nRegion.getLength() == GAP_ESTIMATED_LENGTH)
				{
					unknownEstimatedLength++;
				}

				reportMessage(
						Severity.FIX,
						entry.getOrigin(),
						ASSEMBLY_GAP_MESSAGE_ID,
						Integer.toString(nRegion.getLength()),
						Integer.toString(nRegion.getStart()),
						Integer.toString(nRegion.getEnd()));
			} else
			{
				newGapFeature = features
						.createFeature(Feature.GAP_FEATURE_NAME);
				newGapFeature.setLocations(compoundJoin);
				newGapFeature.setSingleQualifierValue(
						Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME,
						Integer.toString(nRegion.getLength()));
				entry.addFeature(newGapFeature);
				newGapFeatureCount++;// number of gap features created
				if (nRegion.getLength() == GAP_ESTIMATED_LENGTH)
				{
					unknownEstimatedLength++;
				}

				reportMessage(
						Severity.FIX,
						entry.getOrigin(),
						GAP_MESSAGE_ID,
						Integer.toString(nRegion.getLength()),
						Integer.toString(nRegion.getStart()),
						Integer.toString(nRegion.getEnd()));
			}
		}
	}

	@Override
	protected void finish(Entry entry)
	{
		// check all or 90% of newly created gap features are of 100bp long, if
		// so, make their "estimated length"
		// qualifiers "UNKNOWN" where the length was 100
		List<Feature> gapFeatures;
		if (getEmblEntryValidationPlanProperty().validationScope.get().isInGroup(Group.ASSEMBLY))
		{
			gapFeatures = getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
		} else
		{
			gapFeatures = getFeatures(Feature.GAP_FEATURE_NAME, entry);
		}

		if (newGapFeatureCount != 0)
		{
			boolean unknownLength = false;
			if (newGapFeatureCount == unknownEstimatedLength
					|| unknownEstimatedLength >= (newGapFeatureCount * 0.90))
			{

				for (Feature gapFeature : gapFeatures)
				{
					Qualifier estimatedLengthQualifier = getQualifier(
							Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME,
							gapFeature);
					if (estimatedLengthQualifier != null
							&& estimatedLengthQualifier.getValue().equals(
									GAP_ESTIMATED_LENGTH_STRING))
					{

						estimatedLengthQualifier.setValue("unknown");
						unknownLength = true;
					}
				}
			}
			if (unknownLength)
			{
				reportMessage(
						Severity.FIX,
						entry.getOrigin(),
						ESTIMATED_LENGTH_ID,
						newGapFeatureCount);
			}
			reportMessage(
					Severity.FIX,
					entry.getOrigin(),
					COUNT_MESSAGE_ID,
					newGapFeatureCount);
		}
		newGapFeatureCount = 0;
	}
}
