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
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@CheckDataSet(dataSetNames = {FileName.EXCLUSIVE_QUALIFIERS })
@Description("Qualifiers {0} and {1} cannot exist together.")
public class ExclusiveQualifiersCheck extends FeatureValidationCheck {

    private final static String MESSAGE_ID = "ExclusiveQualifiersCheck1";
    private final static String PSEUDO_MESSAGE_ID = "ExclusiveQualifiersCheck2";

    public ExclusiveQualifiersCheck() {
    }

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();
        DataSet dataSet = GlobalDataSets.getDataSet(FileName.EXCLUSIVE_QUALIFIERS);

        if (feature == null) {
            return result;
        }

        for (DataRow dataRow : dataSet.getRows()) {

            String qualifierName1 = dataRow.getString(0);
            String qualifierName2 = dataRow.getString(1);
            
            if (qualifierName1 == null || qualifierName2 == null) {
                return result;
            }

            if (SequenceEntryUtils.isQualifierAvailable(qualifierName1, feature)
                    && SequenceEntryUtils.isQualifierAvailable(qualifierName2, feature)) {
            	if((qualifierName1.equals(Qualifier.PSEUDO_QUALIFIER_NAME)||qualifierName1.equals(Qualifier.PSEUDOGENE_QUALIFIER_NAME))&&qualifierName2.equals(Qualifier.PRODUCT_QUALIFIER_NAME))
            	{
            		reportWarning(feature.getOrigin(),PSEUDO_MESSAGE_ID,qualifierName1,qualifierName2,Qualifier.NOTE_QUALIFIER_NAME);
            		
            	}
            	else
            	{
                reportError(feature.getOrigin(), MESSAGE_ID, qualifierName1, qualifierName2);
            	}
            }
        }
        return result;
    }

}
