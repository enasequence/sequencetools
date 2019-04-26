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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import java.util.Arrays;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

@Description("Qualifier \"{0}\" can only exist if taxonomic division has one of the values \"{1}\"")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class TaxonomicDivisionQualifierCheck extends EntryValidationCheck
{
	private final static String TAXONOMIC_DIVISION_MESSAGE_ID = "TaxonomicDivisionQualifierCheck_1";

	public TaxonomicDivisionQualifierCheck()
	{

	}

	public ValidationResult check(Entry entry)
	{
		DataSet dataSet = GlobalDataSets.getDataSet(FileName.TAXONOMIC_DIVISION_QUALIFIER);
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}
		if(entry.getFeatures().size()==0)
		{
			return result;
		}
		if(entry.getPrimarySourceFeature()==null)
		{
			return result;
		}
		String scientificName=entry.getPrimarySourceFeature().getScientificName();
		
		Taxon taxon=getEmblEntryValidationPlanProperty().taxonHelper.get().getTaxonByScientificName(scientificName);
		
		if (taxon == null||taxon.getDivision()==null)
			return result;
		
		for (DataRow dataRow : dataSet.getRows())
		{
			String qualifier = dataRow.getString(0);
			String divisionString = dataRow.getString(1);
			List<String> divisions=Arrays.asList(divisionString.split(","));
			if (SequenceEntryUtils.isQualifierAvailable(qualifier, entry) && !divisions.contains(taxon.getDivision()))
			{
				reportError(entry.getOrigin(), TAXONOMIC_DIVISION_MESSAGE_ID, qualifier, divisionString);
			}
		}

		return result;
	}

}
