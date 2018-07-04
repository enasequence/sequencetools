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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("Qualifier \\\"{0}\\\" must have one of values {1} when qualifier \\\"{2}\\\" has value \\\"{3}\\\"in any feature.")
@ExcludeScope(validationScope = {ValidationScope.NCBI})
public class QualifierValueRequiredQualifierValueEntryCheck extends
		EntryValidationCheck {

	private final static String MESSAGE_ID = "QualifierValueRequiredQualifierValueEntryCheck";

	public QualifierValueRequiredQualifierValueEntryCheck() {
	}

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();
		DataSet dataSet = GlobalDataSets.getDataSet(FileName.QUALIFIER_VAL_REQUIRED_QUALIFIER_ENTRY);

		if (entry == null) {
			return result;
		}

		for (DataRow row : dataSet.getRows()) {
			String requiredQualifierName = row.getString(0);
			String[] requiredQualifierValues = row.getStringArray(1);
			String qualifierName = row.getString(2);
			String qualifierValue = row.getString(3);

			if (qualifierName == null || requiredQualifierName == null
					|| ArrayUtils.isEmpty(requiredQualifierValues)) {
				continue;
			}

			if (!SequenceEntryUtils.isQualifierWithValueAvailable(
					qualifierName, qualifierValue, entry))
				continue;

			Collection<Qualifier> requiredQualifiers = new ArrayList<Qualifier>();
			Collection<Qualifier> qualifiers = new ArrayList<Qualifier>();

			for (Feature feature : entry.getFeatures()) {
				requiredQualifiers.addAll(feature
						.getQualifiers(requiredQualifierName));
			}

			if (requiredQualifiers.isEmpty()) {
				continue;
			}

			String reqQualifierValuesStr = Utils
					.paramArrayToString(requiredQualifierValues);
			for (Feature feature : entry.getFeatures()) {
				for (Qualifier qualifier : feature.getQualifiers(qualifierName)) {
					if (qualifier != null && qualifier.getValue() != null
							&& qualifier.getValue().equals(qualifierValue)) {
						for (Qualifier requiredQualifier : requiredQualifiers) {

							if (requiredQualifier == null) {
								continue;
							}
							if (!ArrayUtils.contains(requiredQualifierValues,
									requiredQualifier.getValue())) {
								reportError(feature, requiredQualifierName,
										reqQualifierValuesStr, qualifierName,
										qualifierValue);
							}
						}
					}
				}
			}
		}

		return result;
	}

	private void reportError(Feature feature, String qualifierName,
			String qualifierValue, String requiredQualifierName,
			String reqQualifierValuesStr) {
		reportWarning(feature.getOrigin(), MESSAGE_ID, qualifierName,
				qualifierValue, requiredQualifierName, reqQualifierValuesStr);
	}
}
