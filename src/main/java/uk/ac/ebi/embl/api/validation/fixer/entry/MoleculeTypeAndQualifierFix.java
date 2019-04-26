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

import java.util.Collection;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("\"{0}\" qualifier removed from feature \"{1}\" as mol_type is not equal to \"{2}\" ")
public class MoleculeTypeAndQualifierFix extends EntryValidationCheck
{

	private final static String MESSAGE_ID = "MoleculeTypeAndQualifierFix";

	public MoleculeTypeAndQualifierFix()
	{

	}

	public ValidationResult check(Entry entry)
	{
		DataSet dataSet = GlobalDataSets.getDataSet(FileName.SOURCE_QUALIFIERS_MOLTYPE_VALUES);
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

		if (entry.getPrimarySourceFeature() == null)
			return result;

		String molType = SequenceEntryUtils.getMoleculeType(entry) == null ? entry.getPrimarySourceFeature().getSingleQualifierValue(Qualifier.MOL_TYPE_QUALIFIER_NAME) : SequenceEntryUtils.getMoleculeType(entry);

		if (molType == null)
		{
			return result;
		}
		for (DataRow row : dataSet.getRows())
		{
			String requiredmolType_value = row.getString(0);
			String qualifier = row.getString(1);
			if(molType.equals(requiredmolType_value))
				continue;
			List<Qualifier> qualifiers = SequenceEntryUtils.getQualifiers(qualifier, entry);
			if (qualifiers.size() == 0)
				continue;
			Collection<Feature> features = SequenceEntryUtils.getFeaturesContainingQualifier(qualifier, entry);
			for (Feature feature : features)
			{
				Qualifier qualifiertoRemove = feature.getSingleQualifier(qualifier);
				feature.removeQualifier(qualifiertoRemove);
				reportMessage(Severity.FIX, feature.getOrigin(), MESSAGE_ID, qualifier, feature.getName(), requiredmolType_value);
			}
		}

		return result;
	}

}
