/*******************************************************************************
 * Copyright 2012-2013 EMBL-EBI, Hinxton outstation
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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

import java.util.List;

@Description("Qualifier \"{0}\" has been removed from Feature \"{1}\" as qualifier \"{0}\" must not exist if taxonomic divison has value {2}.")
public class TaxonomicDivisionNotQualifierFix extends EntryValidationCheck
{

	private final static String TAXONOMIC_DIVISION_MESSAGE_ID = "TaxonomicDivisionNotQualifierFix";

	public TaxonomicDivisionNotQualifierFix()
	{

	}

	public ValidationResult check(Entry entry)
	{
		DataSet dataSet = GlobalDataSets.getDataSet(FileName.TAXONOMIC_DIVISION_NO_QUALIFIER);
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		if (entry.getFeatures().size() == 0)
		{
			return result;
		}
		if (entry.getPrimarySourceFeature() == null)
		{
			return result;
		}
		String scientificName = entry.getPrimarySourceFeature().getScientificName();

		Taxon taxon = getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonByScientificName(scientificName);

		if (taxon == null || taxon.getDivision() == null)
			return result;

		List<Feature> sourceFeatures = SequenceEntryUtils.getFeatures(Feature.SOURCE_FEATURE_NAME, entry);

		if (sourceFeatures == null || sourceFeatures.size() == 0)
			return result;

		for (DataRow dataRow : dataSet.getRows())
		{
			String excludeQualifier = dataRow.getString(0);
			String division = dataRow.getString(1);
			if (division.equals(taxon.getDivision()))
			{
				for (Feature feature : sourceFeatures)
				{
					if (feature.removeQualifier(excludeQualifier))
						reportMessage(Severity.FIX, entry.getOrigin(), TAXONOMIC_DIVISION_MESSAGE_ID, excludeQualifier, feature.getName(), division);

				}
			}

		}

		return result;
	}

}
