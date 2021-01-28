
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
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.helper.QualifierHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Description("Feature qualifier \\\"{0}\\\" is not recognized\\Feature qualifier \\\"{0}\\\" does not have a value (mandatory for this type)\\" +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" is invalid. Refer to the feature documentation or ask a curator for guidance." +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" does not comply to the qualifier specifications. Refer to the feature documentation or ask a curator for guidance.\"")
@ExcludeScope(validationScope={ValidationScope.ARRAYEXPRESS, ValidationScope.ASSEMBLY_CHROMOSOME, ValidationScope.ASSEMBLY_CONTIG, ValidationScope.ASSEMBLY_MASTER,
        ValidationScope.ASSEMBLY_SCAFFOLD, ValidationScope.ASSEMBLY_TRANSCRIPTOME, ValidationScope.EGA, ValidationScope.EMBL, ValidationScope.EMBL,
        ValidationScope.EMBL_TEMPLATE, ValidationScope.EPO, ValidationScope.EPO_PEPTIDE, ValidationScope.INSDC, ValidationScope.NCBI_MASTER})
public class NCBIQualifierCheck extends FeatureValidationCheck {

    private static final String NO_QUALIFIER_FOUND_ID = "QualifierCheck-1";

    public NCBIQualifierCheck() {
    }

    public ValidationResult check(Feature feature) {
        result = new ValidationResult();
        if (feature == null) {
            return result;
        }

        Map<String, QualifierHelper.QualifierInfo> qualifierMap = QualifierHelper.getQualifierMap();
        Set<String> artemisQualifiersSet = QualifierHelper.getArtemisQualifierSet();
        Set<String> ignorable = new HashSet<>(Arrays.asList(Qualifier.OLD_LOCUS_TAG, Qualifier.PROTEIN_ID_QUALIFIER_NAME, Qualifier.ALTITUDE_QUALIFIER_NAME));
        QualifierHelper.QualifierInfo qInfo = qualifierMap.get(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
        qInfo.addRegexGroupInfos(QualifierHelper.setNullGroupTolerance(qInfo));

        for (Qualifier qualifier : feature.getQualifiers()) {

            String qualifierName = qualifier.getName();
            if (!ignorable.contains(qualifierName) ) {

                if (qualifierMap.containsKey(qualifierName)) {
                    QualifierHelper.QualifierInfo qualifierInfo = qualifierMap.get(qualifierName);

                    if(qualifierName.equalsIgnoreCase(Qualifier.LAT_LON_QUALIFIER_NAME)) {
                        result.append(QualifierHelper.checkLatLonRange(qualifierInfo, qualifier));
                    }

                    result.append(QualifierHelper.checkRegEx(qualifierInfo, qualifier));

                } else {
                    ValidationMessage<Origin> message =
                            reportError(qualifier.getOrigin(), NO_QUALIFIER_FOUND_ID, qualifierName);
                    if (artemisQualifiersSet.contains(qualifierName)) {
                        message.setCuratorMessage("If you are using Artemis to create this file, select the 'EMBL submission' format");
                    }
                }
            }
        }

        return result;
    }


}
