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

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

import java.util.List;

@Description("")
public class Linkage_evidenceFix extends FeatureValidationCheck
{

	private static final String LINKAGE_EVIDENCE_FIX_ID_1 = "Linkage_evidenceFix_1";

	public ValidationResult check(Feature feature)
	{
		result = new ValidationResult();

		if (feature == null)
		{
			return result;
		}
		List<Qualifier> linkageQualifiers = feature.getQualifiers(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);
		for (Qualifier qualifier : linkageQualifiers)
		{
			String linkageValue = qualifier.getValue();
			if (linkageValue.contains("_"))
			{
				qualifier.setValue(StringUtils.replace(linkageValue, "_", " "));
				reportMessage(Severity.FIX, feature.getOrigin(), LINKAGE_EVIDENCE_FIX_ID_1, linkageValue, qualifier.getValue());
			}
		}
		return result;
	}

}
