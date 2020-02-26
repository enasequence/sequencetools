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
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("At least one of the following qualifiers {0} must exist when organism belongs to {1}.")
@ExcludeScope(validationScope = { ValidationScope.EPO,ValidationScope.EPO_PEPTIDE, ValidationScope.NCBI  , ValidationScope.ASSEMBLY_TRANSCRIPTOME, ValidationScope.EMBL_TEMPLATE})
public class OrganismAndRequiredQualifierCheck extends FeatureValidationCheck {

    private final static String MESSAGE_ID = "OrganismAndRequiredQualifierCheck";

    public OrganismAndRequiredQualifierCheck()
	{
	}

	public ValidationResult check(Feature feature) {
        DataSet dataSet = GlobalDataSets.getDataSet(FileName.ORGANISM_REQUIRED_QUALIFIER);
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if (!(feature instanceof SourceFeature)) {
            return result;
        }

        SourceFeature source = (SourceFeature) feature;

        for (DataRow dataRow : dataSet.getRows()) {
            checkFeatureAgainstRow(dataRow, source);
        }

        return result;
    }

    private void checkFeatureAgainstRow(DataRow dataRow, SourceFeature sourceFeature) {

        String[] requiredQualifiers = dataRow.getStringArray(0);
        String[] expectedOrganisms = dataRow.getStringArray(1);
        Severity severity = Severity.valueOf(dataRow.getString(2));//will throw an illegal argument if it cant recognize

        if (expectedOrganisms == null || requiredQualifiers == null || requiredQualifiers.length == 0) {
            return;
        }

        // check qualifiers before taxonomy check - less expensive
        for (String requiredQualifier : requiredQualifiers) {
            if (SequenceEntryUtils.isQualifierAvailable(requiredQualifier, sourceFeature)) {
                return;
            }
        }

        Qualifier organismQualifier = sourceFeature.getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);

        if (organismQualifier == null) {
            return;
        }

        String organism = organismQualifier.getValue();
        if (organism == null) {
            return;
        }

        for (String expectedOrganism : expectedOrganisms) {
            if (getEmblEntryValidationPlanProperty().taxonHelper.get().isChildOf(organism, expectedOrganism)) {
                reportMessage(severity, sourceFeature.getOrigin(), MESSAGE_ID, Utils.paramArrayToString(requiredQualifiers), expectedOrganism);
            }
        }
    }

}
