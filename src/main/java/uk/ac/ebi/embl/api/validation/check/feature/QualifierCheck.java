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
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.helper.QualifierHelper;

import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

@Description("Feature qualifier \\\"{0}\\\" is not recognized\\Feature qualifier \\\"{0}\\\" does not have a value (mandatory for this type)\\" +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" is invalid. Refer to the feature documentation or ask a curator for guidance." +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" does not comply to the qualifier specifications. Refer to the feature documentation or ask a curator for guidance.\"")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class QualifierCheck extends FeatureValidationCheck {

    private static final String NO_QUALIFIER_FOUND_ID = "QualifierCheck-1";

    public QualifierCheck() {
    }

    public ValidationResult check(Feature feature) {

        try {
        	if(feature instanceof SourceFeature
        			&&(ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get())
        			||ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get())
        			||ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get())))
        		return result;
            Map<String, QualifierHelper.QualifierInfo> qualifierMap = QualifierHelper.getQualifierMap();
            Set<String> artemisQualifiersSet = QualifierHelper.getArtemisQualifierSet();
            QualifierHelper.QualifierInfo qInfo = qualifierMap.get(Qualifier.COLLECTION_DATE_QUALIFIER_NAME);
            qInfo.addRegexGroupInfos(QualifierHelper.setNullGroupTolerance(qInfo));
            result = new ValidationResult();

            if (feature == null) {
                return result;
            }

            for (Qualifier qualifier : feature.getQualifiers()) {

                String qualifierName = qualifier.getName();
                if(qualifierName.equalsIgnoreCase("old_locus_tag")) {
                    System.out.println(qualifierName);
                }
                if (qualifierMap.containsKey(qualifierName)) {

                    QualifierHelper.QualifierInfo qualifierInfo = qualifierMap.get(qualifierName);

                    //check the NOVALUE requirement
                    QualifierHelper.checkNoValue(qualifier, qualifierInfo.isNoValue(), feature.getName()).ifPresent(result::append);

                    //todo check the 'NEW' field

                    switch(qualifierName) {
                        case Qualifier.LAT_LON_QUALIFIER_NAME :
                            result.append(QualifierHelper.checkLatLonRange(qualifierInfo, qualifier));
                            break;
                        case Qualifier.PROTEIN_ID_QUALIFIER_NAME:
                            result.append(QualifierHelper.checkProteinIdVersion(qualifierInfo,qualifier));
                            break;
                        default:
                    }

                    result.append(QualifierHelper.checkRegEx(qualifierInfo, qualifier));


                } else {//the qualifier is not in the CV
                    ValidationMessage<Origin> message =
                            reportError(qualifier.getOrigin(), NO_QUALIFIER_FOUND_ID, qualifierName);
                    if(artemisQualifiersSet.contains(qualifierName)) {
                        message.setCuratorMessage("If you are using Artemis to create this file, select the 'EMBL submission' format");
                    }
                }

            }
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid pattern while instantiating QualifierCheck! " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
            return result;
    }
    
 }
