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

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.location.LocationToStringCoverter;

@Description("Deleted gene Feature associated with {0} feature with location {1}")
public class GeneAssociatedwithFeatureFix extends EntryValidationCheck
{

	protected final static String MESSAGE_ID = "GeneAssociatedwithFeatureFix";
	
	public ValidationResult check(Entry entry)
	{
		
		result = new ValidationResult();
		
		List<Feature> geneAssociatedFeatures=new ArrayList<Feature>();

		if (entry == null)
		{
			return result;
		}
		if (entry.getFeatures().size() == 0)
		{
			return result;
		}

		List<Feature> cdsFeatures = SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry);
		List<Feature> trnaFeatures = SequenceEntryUtils.getFeatures(Feature.tRNA_FEATURE_NAME, entry);
		List<Feature> rrnaFeatures = SequenceEntryUtils.getFeatures(Feature.rRNA_FEATURE_NAME, entry);
		if (cdsFeatures.size() != 0)
			geneAssociatedFeatures.addAll(cdsFeatures);
		if (trnaFeatures.size() != 0)
			geneAssociatedFeatures.addAll(trnaFeatures);
		if (rrnaFeatures.size() != 0)
			geneAssociatedFeatures.addAll(rrnaFeatures);

		List<Feature> geneFeatures = SequenceEntryUtils.getFeatures(Feature.GENE_FEATURE_NAME, entry);
		
		if (geneAssociatedFeatures.size() == 0 || geneFeatures.size() == 0)
		{
			return result;
		}

		for (Feature geneAssociatedFeature : geneAssociatedFeatures)
		{
			for (Feature gene : geneFeatures)
			{
				if (geneAssociatedFeature.getLocations().equals(gene.getLocations()))
				{
					if(entry.removeFeature(gene))
					reportMessage(Severity.FIX, geneAssociatedFeature.getOrigin(), MESSAGE_ID,geneAssociatedFeature.getName(),LocationToStringCoverter.renderCompoundLocation(geneAssociatedFeature.getLocations()));
				}
			}
		}

		return result;

	}
}
