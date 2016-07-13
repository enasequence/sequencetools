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
import uk.ac.ebi.embl.api.storage.tsv.TSVReader;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Qualifier {0} must not have value which matches the pattern {1} + <value> + {2} where <value> is a value of qualifier {3}.")
public class QualifierValueNotQualifierPatternCheck extends FeatureValidationCheck {

    @CheckDataSet("qualifier-value-not-qualifier-pattern.tsv")
    private DataSet dataSet;

    private final static String MESSAGE_ID = "QualifierValueNotQualifierPatternCheck";

    public QualifierValueNotQualifierPatternCheck() {
    }

    QualifierValueNotQualifierPatternCheck(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        for (DataRow dataRow : dataSet.getRows()) {
            String qualifierName1 = dataRow.getString(0);
            String valuePrefix = dataRow.getString(1);
            String valueSuffix = dataRow.getString(2);
            String qualifierName2 = dataRow.getString(3);

            if (qualifierName2 == null || qualifierName1 == null) {
                return result;
            }

            if(valuePrefix.equals(TSVReader.EMPTY_COL)){
                valuePrefix = "";
            }

            if(valueSuffix.equals(TSVReader.EMPTY_COL)){
                valueSuffix = "";
            }

            Collection<Qualifier> qualifiers2 = feature.getQualifiers(qualifierName1);
            if (qualifiers2.isEmpty()) {
                return result;
            }

            Collection<Qualifier> qualifiers1 = feature.getQualifiers(qualifierName2);

            for (Qualifier qualifier1 : qualifiers1) {
                String value1 = qualifier1.getValue();
                String pattern = StringUtils.join(new String[]{valuePrefix, value1, valueSuffix});

                for (Qualifier qualifier2 : qualifiers2) {
                    String value2 = qualifier2.getValue();
                    if (value2 == null) {
                        continue;
                    }

                    if (value2.matches(pattern)) {
                        reportError(feature.getOrigin(), MESSAGE_ID, qualifierName1, valuePrefix, valueSuffix, qualifierName2);
                    }
                }
            }
        }

        return result;
    }
}
