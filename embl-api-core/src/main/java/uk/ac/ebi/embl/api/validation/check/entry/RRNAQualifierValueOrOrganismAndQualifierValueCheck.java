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

import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataRow;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.helper.Utils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;

@CheckDataSet(dataSetNames = {FileName.RRNA_QUALIFIER_VAL_ORGANISM_QUALIFIER_VALUE } )
@Description("Value \"{0}\" of qualifier \"{1}\" is only allowed when source qualifier \"{2}\" has one of values {3} or contains the word \"{5}\" or organism belongs to one of {4}.")
@RemoteExclude
public class RRNAQualifierValueOrOrganismAndQualifierValueCheck extends EntryValidationCheck {

	private final static String MESSAGE_ID = "RRNAQualifierValueOrOrganismAndQualifierValueCheck";
	
	public RRNAQualifierValueOrOrganismAndQualifierValueCheck()
	{
		
	}

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();


		if (entry == null) {
			return result;
		}

		for(DataRow dataRow : GlobalDataSets.getDataSet(FileName.RRNA_QUALIFIER_VAL_ORGANISM_QUALIFIER_VALUE).getRows()) {

			String qualifierValue = dataRow.getString(0);
			String qualifierName = dataRow.getString(1);
			String qualifierName2 = dataRow.getString(2);
			String[] qualifierValue2 = dataRow.getStringArray(3);
			String[] organismNames = dataRow.getStringArray(4);

			if ((qualifierName2 == null && ArrayUtils.isEmpty(organismNames)) || qualifierName == null) {
				continue;
			}

			Collection<Feature> rRNAs = SequenceEntryUtils.getFeatures("rRNA", entry);
			if (rRNAs.isEmpty()) {
				continue;
			}

			SourceFeature sourceFeature = entry.getPrimarySourceFeature();
			if (sourceFeature == null) {
				continue;
			}

			// null value means condition has not been checked yet
			Boolean organismCondition = null;
			for (Feature rRNA : rRNAs) {

				boolean permittedQualifierValueAvailable =
						SequenceEntryUtils.isQualifierWithValueAvailable(qualifierName, qualifierValue, rRNA);
				if (!permittedQualifierValueAvailable) {
					continue; // there is no need to check condition
				}

				// check whether a provided qualifier has a specified value
				boolean qualifierCondition = false;
				for (String value : qualifierValue2) {
					qualifierCondition =
							SequenceEntryUtils.isQualifierWithValueAvailable(qualifierName2, value, sourceFeature) || SequenceEntryUtils.isQualifierwithPatternAvailable(qualifierName2, "(.*)(plastid|PLASTID)(.*)", entry);
					if (qualifierCondition == true)
						break;
				}
				if (!qualifierCondition && organismCondition == null) {
					organismCondition = isAnyOrganismFromFamily(entry, organismNames);
				}

				if (!qualifierCondition && !organismCondition && permittedQualifierValueAvailable) {
					reportError(entry.getOrigin(), MESSAGE_ID, qualifierValue,
							qualifierName, qualifierName2, Utils.paramArrayToString(qualifierValue2), Utils.paramArrayToString(organismNames), "plastid");
				}
			}
		}

		return result;
	}

	// check whether any of available organisms belong to the specified family
	boolean isAnyOrganismFromFamily(Entry entry, String[] organismNames) {
		Collection<String> organisms = SequenceEntryUtils.getAllOrganismNames(entry);
		for (String organism : organisms) {
			if (StringUtils.isEmpty(organism)) {
				continue;
			}

            /**
             * If the organism is not recognized - return to be true as it may or may not be a valid
             * organism - we dont know and cant throw an error.
             */
            if(!getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismValid(organism)){
                return true;
            }

            if (getEmblEntryValidationPlanProperty().taxonHelper.get().isChildOfAny(organism, organismNames)) {
				return true;
			}
		}
		return false;
	}
}
