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
package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("Qualifier \\\"{0}\\\" must have one of values {1} when qualifier \\\"{2}\\\" value starts with \\\"{3}\\\".")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class QualifierValueRequiredQualifierStartsWithValueCheck extends
		FeatureValidationCheck
{

	private final static String MESSAGE_ID = "QualifierValueRequiredQualifierStartsWithValueCheck";

	public QualifierValueRequiredQualifierStartsWithValueCheck()
	{
	}

	public ValidationResult check(Feature feature)
	{
		DataSet dataSet = GlobalDataSets.getDataSet(FileName.QUALIFIER_VALUE_REQ_QUALIFIER_STARTSWITH_VALUE);
		result = new ValidationResult();

		if (feature == null||feature.getQualifiers()==null||feature.getQualifiers().size()==0)
		{
			return result;
		}
		

		for (DataRow row : dataSet.getRows())
		{
			String requiredQualifierName = row.getString(0);
			String[] requiredQualifierValues = row.getStringArray(1);
			String qualifierName = row.getString(2);
			String qualifierValue = row.getString(3);
			String reqQualifierValuesStr = Utils.paramArrayToString(requiredQualifierValues);
			for (Qualifier qualifier : feature.getQualifiers())
			{
				if (qualifier.getValue() != null && qualifier.getName().equals(qualifierName) && qualifier.getValue().startsWith(qualifierValue))
				{
					String requiredQualifierValue = feature.getSingleQualifierValue(requiredQualifierName);
					if (requiredQualifierValue != null)
					{
						boolean isMatch=false;
						for(String value:requiredQualifierValues)
						{
							if(value.equalsIgnoreCase(requiredQualifierValue))
							{ isMatch=true;
								break;
							}
						}
						if (!isMatch)
							reportError(feature.getOrigin(), MESSAGE_ID, requiredQualifierName, reqQualifierValuesStr, qualifierName, qualifierValue);
					}
				}
			}

		}

		return result;
	}

}
