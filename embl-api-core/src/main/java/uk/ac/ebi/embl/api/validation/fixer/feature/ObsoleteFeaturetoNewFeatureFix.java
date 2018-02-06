/*
 * # Copyright 2012-2012 EMBL-EBI, Hinxton outstation
 *
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 *
# http://www.apache.org/licenses/LICENSE-2.0
 *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.fixer.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("Feature {0} transformed to {1} feature with \"{2}\" qualiifer value \"{3}\"")
public class ObsoleteFeaturetoNewFeatureFix extends FeatureValidationCheck
{
	private static final String ObsoleteFeaturetoFeatureFix_ID_1 = "ObsoleteFeaturetoFeatureFix_1";

	public ObsoleteFeaturetoNewFeatureFix()
	{
	}

	public ValidationResult check(Feature feature)
	{
		DataSet obsoleteFeatureDataSet = GlobalDataSets.getDataSet(FileName.OBSOLETE_FEATURE_TO_FEATURE);
		result = new ValidationResult();

		if (feature == null)
		{
			return result;
		}
		String featureName = feature.getName();

		if (featureName == null)
		{
			return result;
		}

		for (DataRow dataRow : obsoleteFeatureDataSet.getRows())
		{
			String obsoleteFeature = dataRow.getString(0);
			String newFeature = dataRow.getString(1);
			String newFeatureQualifierName=dataRow.getString(2);
			String newFeatureQualifierValue=dataRow.getString(3);
			if (featureName.equals(obsoleteFeature))
			{
				feature.setName(newFeature);
				feature.addQualifier(newFeatureQualifierName, newFeatureQualifierValue);
				reportMessage(Severity.FIX, feature.getOrigin(), ObsoleteFeaturetoFeatureFix_ID_1, obsoleteFeature, newFeature,newFeatureQualifierName,newFeatureQualifierValue);
			}

		}
		return result;
	}

}
