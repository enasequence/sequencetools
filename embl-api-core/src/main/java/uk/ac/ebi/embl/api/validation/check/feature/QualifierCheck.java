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
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.QualifierHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

@CheckDataSet(dataSetNames = {FileName.FEATURE_QUALIFIER_VALUES, FileName.FEATURE_REGEX_GROUPS, FileName.ARTEMIS_QUALIFIERS})
@Description("Feature qualifier \\\"{0}\\\" is not recognized\\Feature qualifier \\\"{0}\\\" does not have a value (mandatory for this type)\\" +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" is invalid. Refer to the feature documentation or ask a curator for guidance." +
        "Feature qualifier \\\"{0}\\\" value \\\"{1}\\\" does not comply to the qualifier specifications. Refer to the feature documentation or ask a curator for guidance.\"")
public class QualifierCheck extends FeatureValidationCheck {

    private static final String NO_QUALIFIER_FOUND_ID = "QualifierCheck-1";

    public QualifierCheck() {
    }


    private void init() {

        try {

           // setNullGroupTolerance();

        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid pattern while instantiating QualifierCheck! " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }



    public ValidationResult check(Feature feature) {

        try {
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


                    /**
                     * do a check to see if the date is a legitimate one for collection date
                     * Added separate check for the collection date
                     */
                     /*if (qualifierName.equals(Qualifier.COLLECTION_DATE_QUALIFIER_NAME) && value != null) {

                       String dateFormat1 = "^(\\w){3}\\s*(-)\\s*(\\d{4})$";//MMM-yyy
                        String dateFormat2 = "^(\\d{1,2})\\s*(-)\\s*(.*)\\s*(-)\\s*(\\d{4})$";//dd-MMM-yyyy

                        if (value.matches(dateFormat1) || value.matches(dateFormat2)) {

                            SimpleDateFormat sdf;

                            if (value.matches(dateFormat1)) {
                                sdf = new SimpleDateFormat("MMM-yyyy");
                            } else {
                                sdf = new SimpleDateFormat("dd-MMM-yyyy");
                            }
                            sdf.setLenient(false);

                            try {
                                sdf.parse(qualifier.getValue());
                            } catch (ParseException e) {
                                reportError(qualifier.getOrigin(), COLLECTION_DATE_ID,
                                        qualifierName, qualifier.getValue());

                            }
                        }
                    }*/

                    //todo check the 'NEW' field

                    result.append(QualifierHelper.checkRegexValueRange(qualifierInfo,qualifier));
                    result.append(QualifierHelper.checkRegEx(qualifier, qualifierInfo));


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
