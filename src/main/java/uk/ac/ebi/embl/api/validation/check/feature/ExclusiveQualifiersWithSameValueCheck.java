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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

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

@Description("Qualifiers {0} and {1} cannot have the same value.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class ExclusiveQualifiersWithSameValueCheck extends FeatureValidationCheck {

	private final static String MESSAGE_ID = "ExclusiveQualifiersWithSameValueCheck";

	public ExclusiveQualifiersWithSameValueCheck() {
	}

	public ValidationResult check(Feature feature) {
        result = new ValidationResult();
        DataSet dataSet = GlobalDataSets.getDataSet(FileName.EXCLUSIVE_QUALIFIERS_SAME_VALUE);

        if (feature == null) {
			return result;
		}

        for (DataRow dataRow : dataSet.getRows()) {
            String qualifierName1 = dataRow.getString(0);
            String qualifierName2 = dataRow.getString(1);
            if (qualifierName1 == null || qualifierName2 == null) {
                return result;
            }

            Collection<Qualifier> qualifiers1 = feature.getQualifiers(qualifierName1);
            Collection<Qualifier> qualifiers2 = feature.getQualifiers(qualifierName2);

            if (qualifiers1 == null || qualifiers1.isEmpty() || qualifiers2 == null || qualifiers2.isEmpty()) {
                return result;
            }

            for (Qualifier qualifier1 : qualifiers1) {
                for (Qualifier qualifier2 : qualifiers2) {
                    if (StringUtils.equals(qualifier1.getValue(), qualifier2.getValue())) {
                        reportError(feature.getOrigin(), MESSAGE_ID, qualifierName1, qualifierName2);
                    }
                }
            }
        }
        return result;
	}

}
