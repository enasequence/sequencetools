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

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@Description("Qualifier {0} must not exist when organism belongs to {1}.")
@RemoteExclude
public class OrganismNotQualifierCheck extends FeatureValidationCheck {

//    private final static String ORGANISM_NOT_FOUND_ID = "OrganismNotQualifierCheck-1";
    private final static String ORGANISM_IS_CHILD_ID = "OrganismNotQualifierCheck-2";

    public OrganismNotQualifierCheck()
{
	// TODO Auto-generated constructor stub
}

	public ValidationResult check(Feature feature) {
        DataSet dataSet = GlobalDataSets.getDataSet(FileName.ORGANISM_NO_QUALIFIER);
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if (!(feature instanceof SourceFeature)) {
            return result;
        }
        SourceFeature source = (SourceFeature) feature;

        Qualifier organismQualifier = source.getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);
        if (organismQualifier == null) {
            return result;
        }

        String organism = organismQualifier.getValue();
        if (organism == null) {
            return result;
        }

        //dropping the message about the organism not being recognized - not helpful
//        if (!taxonHelper.isOrganismValid(organism)) {
//            reportMessage(Severity.INFO, feature.getOrigin(), ORGANISM_NOT_FOUND_ID, organism);
//        }

        for (DataRow dataRow : dataSet.getRows()) {
            String qualifierName = dataRow.getString(0);
            String[] organismFamilyNames = dataRow.getStringArray(1);

            if (organismFamilyNames == null || qualifierName == null) {
                return result;
            }

            if (!SequenceEntryUtils.isQualifierAvailable(qualifierName, feature)) {
                return result;
            }

            for (String familyName : organismFamilyNames) {
                if (getEmblEntryValidationPlanProperty().taxonHelper.get().isChildOf(organism, familyName)) {
                    reportError(feature.getOrigin(), ORGANISM_IS_CHILD_ID, qualifierName, familyName);
                }
            }
        }

        return result;
    }

}
