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
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Description("The feature name \\\"{0}\\\" is not recognized\\\\Mandatory qualifier \\\"{0}\\\" not present in feature {1}\\\\" +
        "Qualifier \\\"{0}\\\" must occur exactly 1 time for feature \\\"{1}\\\", not \"{2}\".\\\\Qualifier \\\"{0}\\\" " +
        "is recomended for feature \\\"{1}\\\".")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class FeatureKeyCheck extends FeatureValidationCheck {

    private HashMap<String, FeatureKeyInfo> keysMap = new HashMap<String, FeatureKeyInfo>();

    private final static String KEY_NOT_FOUND_ID = "FeatureKeyCheck-1";
    private final static String MANDATORY_QUALIFIER_ABSENT = "FeatureKeyCheck-2";
    private final static String SINGLE_QUALIFIER_FAILURE = "FeatureKeyCheck-3";
    private final static String RECOMENDED_QUALIFIER_ABSENT1 = "FeatureKeyCheck-4";
    private final static String CODON_START_QUALIFIER_MESSAGE = "FeatureKeyCheck-5";
    private final static String RECOMENDED_QUALIFIER_ABSENT2 = "FeatureKeyCheck-6";
    private final static String NOT_PERMITTED_QUALIFIER_MESSAGE = "FeatureKeyCheck-7";


    public FeatureKeyCheck() {
    }


    private void init() {
        DataSet keySet = GlobalDataSets.getDataSet(GlobalDataSetFile.FEATURE_KEYS);
        DataSet keyQualifiersSet = GlobalDataSets.getDataSet(GlobalDataSetFile.FEATURE_KEY_QUALIFIERS);

        if (keySet != null && keyQualifiersSet != null) {
            for (DataRow dataRow : keySet.getRows()) {
                String key = dataRow.getString(0);
//                String noVal = dataRow.getString(1);
//                String newField = dataRow.getString(2);
                keysMap.put(key, new FeatureKeyInfo(key));
            }

            for (DataRow dataRow : keyQualifiersSet.getRows()) {
                String key = dataRow.getString(0);
                String qual = dataRow.getString(1);
                String mandatory = dataRow.getString(2);
                String single = dataRow.getString(3);
                String recomended = dataRow.getString(4);

                if (keysMap.containsKey(key)) {
                    FeatureKeyCheck.FeatureKeyInfo keyInfo = keysMap.get(key);
                    FeatureQualifierInfo featureQualifierInfo =
                            new FeatureQualifierInfo(key, qual, mandatory, single, recomended);
                    keyInfo.addQualifierInfo(featureQualifierInfo);
                } else {
                    throw new IllegalArgumentException("No matching key for " + key + " in FeatureKeyCheck");
                }
            }
//            System.out.println("Feature map made " + keysMap.size());
        }
    }

    public ValidationResult check(Feature feature) {
        init();
        result = new ValidationResult();

        if (feature == null) {
            return result;
        }

        if (keysMap.containsKey(feature.getName())) {//check the key is in the CV

            FeatureKeyCheck.FeatureKeyInfo keyInfo = keysMap.get(feature.getName());
            ArrayList<String> validFeatureQualifiers=new ArrayList<String>();
            if(keyInfo.getQualifierInfos().size() > 0){

                //for all the qualifier infos for this feature....
                for(FeatureQualifierInfo qualInfo : keyInfo.getQualifierInfos()){

                	validFeatureQualifiers.add(qualInfo.getQual());
                    //CHECK FOR MANDATORY QUALIFIERS
                    if(qualInfo.isMandatory() && !SequenceEntryUtils.isQualifierAvailable(qualInfo.getQual(), feature)){
                        reportFeatureError(feature.getOrigin(), MANDATORY_QUALIFIER_ABSENT, feature, qualInfo.getQual(),feature.getName());
                    }

                    //ADD A WARNING IF RECOMENDED AND ABSENT
                   if(qualInfo.isRecomended() && !SequenceEntryUtils.isQualifierAvailable(qualInfo.getQual(), feature))
                   {
                    if(feature.getName().equals(Feature.INTRON_FEATURE_NAME)&&qualInfo.getQual().equals(Qualifier.NUMBER_QUALIFIER_NAME)&&getEmblEntryValidationPlanProperty().validationScope.get().equals(ValidationScope.EMBL_TEMPLATE))//EMD-4447
                     {
                      if(SequenceEntryUtils.isQualifierAvailable(Qualifier.GENE_QUALIFIER_NAME, feature)&&!SequenceEntryUtils.getQualifierValue(Qualifier.GENE_QUALIFIER_NAME, feature).equals("tRNA"))
                      {
                    	  reportWarning(feature.getOrigin(), RECOMENDED_QUALIFIER_ABSENT2, qualInfo.getQual(), feature.getName());
                      }
                      else
                      {
                    	  reportWarning(feature.getOrigin(), RECOMENDED_QUALIFIER_ABSENT1, qualInfo.getQual(), feature.getName());
                      }
                    	  
                     }
                    else
                        reportWarning(feature.getOrigin(), RECOMENDED_QUALIFIER_ABSENT1, qualInfo.getQual(), feature.getName());
                    }

                    int qualifierCount = SequenceEntryUtils.getFeatureQualifierCount(qualInfo.getQual(), feature);
                    if(qualInfo.isSingle() && qualifierCount > 1){//we dont mind a count of 0 as mandatory check will have picked these up
                        reportFeatureError(feature.getOrigin(), SINGLE_QUALIFIER_FAILURE, feature, qualInfo.getQual(), feature.getName(), qualifierCount);
                    }

                }
            }

			for (Qualifier qualifier : feature.getQualifiers())
			{
				if (!validFeatureQualifiers.contains(qualifier.getName()))

					reportError(feature.getOrigin(),
								NOT_PERMITTED_QUALIFIER_MESSAGE,
								qualifier.getName(),
								feature.getName());

			}
        } else {//the key is not in the CV
            reportError(feature.getOrigin(), KEY_NOT_FOUND_ID, feature.getName());
        }

        if(feature instanceof CdsFeature){
            validateCdsFeature((CdsFeature)feature);
        }
        return result;
    }

    private void validateCdsFeature(CdsFeature cdsFeature) {
        CompoundLocation<Location> locations = cdsFeature.getLocations();

        if(locations.isLeftPartial() && !SequenceEntryUtils.isQualifierAvailable("codon_start", cdsFeature)){
            reportWarning(cdsFeature.getOrigin(), CODON_START_QUALIFIER_MESSAGE);
        }
    }

    class FeatureKeyInfo {
        private String key;
        private List<FeatureQualifierInfo> qualifierInfos = new ArrayList<FeatureQualifierInfo>();

        FeatureKeyInfo(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void addQualifierInfo(FeatureQualifierInfo info) {
            qualifierInfos.add(info);
        }

        public List<FeatureQualifierInfo> getQualifierInfos() {
            return qualifierInfos;
        }
    }

    class FeatureQualifierInfo {
        private String key;
        private String qual;
        private String mandatory;
        private String single;
        private String recomended;

        FeatureQualifierInfo(String key, String qual, String mandatory, String single, String recomended) {
            this.key = key;
            this.qual = qual;
            this.mandatory = mandatory;
            this.single = single;
            this.recomended = recomended;
        }

        public String getKey() {
            return key;
        }

        public String getQual() {
            return qual;
        }

        public String getMandatory() {
            return mandatory;
        }

        public String getSingle() {
            return single;
        }

        public boolean isMandatory() {
            return mandatory.equals("Y");
        }

        public boolean isSingle(){
            return single.equals("Y");
        }

        public boolean isRecomended() {
            return recomended.equals("Y");
        }
    }
}
