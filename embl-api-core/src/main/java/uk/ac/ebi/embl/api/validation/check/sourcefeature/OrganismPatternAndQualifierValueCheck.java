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

import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;

@Description("If the organism belongs to the specified lineage {0} and qualifier {1} exists, the pattern formed by wrapping the qualifier value {1} with patterns {2} and {3} must match")
@RemoteExclude
public class OrganismPatternAndQualifierValueCheck extends FeatureValidationCheck {

    @CheckDataSet("organism-qualifier-pattern.tsv")
    private DataSet dataSet;

    private final static String MESSAGE_ID = "OrganismPatternAndQualifierValueCheck";

   
    OrganismPatternAndQualifierValueCheck(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public OrganismPatternAndQualifierValueCheck()
	{
	}

	public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if (!(feature instanceof SourceFeature)) {
            return result;
        }
        SourceFeature source = (SourceFeature) feature;

        for (DataRow dataRow : dataSet.getRows()) {
            String organismFamilyName = dataRow.getString(0);
            String qualifierName = dataRow.getString(1);
            String valuePrefix = dataRow.getString(2);
            String valuePostfix = dataRow.getStringDefault(3, "");
            if (organismFamilyName == null || qualifierName == null) {
                return result;
            }

            List<Qualifier> qualifiers = feature.getQualifiers(qualifierName);
            if (qualifiers.isEmpty()) {
                return result;
            }

            Qualifier organismQualifier = source.getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);
            if (organismQualifier == null) {
                return result;
            }
            String organism = organismQualifier.getValue();
            if (organism == null) {
                return result;
            }

            if (getEmblEntryValidationPlanProperty().taxonHelper.get().isChildOf(organism, organismFamilyName)) {
                for (Qualifier qualifier : qualifiers) {
                    String value = qualifier.getValue();
                    if (value == null) {
                        continue;
                    }
                    String pattern = StringUtils.join(new String[]{valuePrefix,
                            value, valuePostfix});

                    if (!organism.matches(pattern)) {
                        reportError(feature.getOrigin(), MESSAGE_ID, organismFamilyName,
                                qualifierName, valuePrefix, valuePostfix);
                    }
                }
            }
        }
        return result;
    }

}
