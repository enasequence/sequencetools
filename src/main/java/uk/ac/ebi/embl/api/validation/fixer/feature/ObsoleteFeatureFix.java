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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

import java.util.Arrays;

@Description(
        "Obsolete feature \\\"{0}\\\" has been replaced with \\\"{1}\\\"")
public class ObsoleteFeatureFix extends FeatureValidationCheck {

    private static final String FEATURE_REPLACED_ID = "ObsoleteFeatureFix";

    private QualifierFactory qualifierFactory;

    public ObsoleteFeatureFix() {
        this.qualifierFactory = new QualifierFactory();
    }

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if (Arrays.asList("scRNA", "snRNA", "snoRNA").contains(feature.getName())) {
            renameFeature(feature, "ncRNA");
            feature.addQualifier(qualifierFactory.createQualifier("ncRNA_class", "scRNA, snRNA or snoRNA"));
        } else if (feature.getName().equals("repeat_unit")) {
            renameFeature(feature, "repeat_region");
        }

        return result;
    }

    private void renameFeature(Feature feature, String newName) {
        String oldName = feature.getName();
        feature.setName(newName);
        reportMessage(Severity.FIX, feature.getOrigin(), FEATURE_REPLACED_ID, oldName, newName);
    }

}
