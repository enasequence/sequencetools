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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

import java.util.List;

@Description("If not already provided then sets the division based on source feature qualifiers.")
public class DivisionFix extends EntryValidationCheck {

    private final static String DIVISION_FIX_ID = "DivisionFix_1";
    private final static String DIVISION_NOT_FOUND_ID = "DivisionFix_2";


    public DivisionFix() {
    }

    public ValidationResult check(Entry entry) throws ValidationEngineException {

        result = new ValidationResult();

        if (entry == null || entry.getPrimarySourceFeature() == null) {
            return result;
        }

        if (shouldSetDivision(entry)) {
            try {
                SourceFeature primarySF = entry.getPrimarySourceFeature();
                
                // Get division using transgenic/pseduo
                String division = (primarySF.isTransgenic()) ? "TGN" : (null != primarySF.getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME)) ? "ENV" : "";
                
                // Get division using tax_id
                if (empty(division) && primarySF.getTaxId() != null) {
                    division = getDivisionByTaxid(primarySF);
                }

                // Get division using Scientific name
                if (empty(division) && primarySF.getScientificName() != null) {
                    division = getDivisionByScientificName(primarySF);
                }

                // Set division if found one.
                if (!empty(division)) {
                    entry.setDivision(division);
                    reportMessage(Severity.FIX, entry.getOrigin(), DIVISION_FIX_ID, division);
                } else {
                    // If division is not found then division will be updated as XXX.
                    entry.setDivision("XXX");
                    reportMessage(Severity.FIX, entry.getOrigin(), DIVISION_NOT_FOUND_ID, "XXX");
                }
            } catch (Exception e) {
                throw new ValidationEngineException(e.getMessage(), e);
            }
        }
        return result;
    }
    
    private String getDivisionByTaxid(SourceFeature primarySF){
        String division = getDivisionFromCache(primarySF.getTaxId().toString());
        if(empty(division)){
            if(primarySF.getTaxId()!=null) {
                Taxon taxon = new TaxonomyClient().getTaxonByTaxid(primarySF.getTaxId());
                if(taxon != null && taxon.getDivision() != null) {
                    division = taxon.getDivision();
                }
                saveDivisionCache(primarySF.getTaxId().toString(), division);
            }
        }
        return division;
    }
    
    private String getDivisionByScientificName(SourceFeature primarySF){
        String division = getDivisionFromCache(primarySF.getScientificName());;
        if(empty(division)){
            List<Taxon> taxonList;
            if (!(taxonList = new TaxonomyClient().getTaxonsByScientificName(primarySF.getScientificName())).isEmpty()) {
                division = taxonList.get(0).getDivision();
                saveDivisionCache(primarySF.getScientificName(), division);
            }
        }
        return division;
    }
    
    private String getDivisionFromCache(String divisionKey){
        if (divisionKey == null) {
            return null;
        }
        return EmblEntryValidationPlan.divisionCache.get(divisionKey)!=null ?  EmblEntryValidationPlan.divisionCache.get(divisionKey) : null;
    }
    
    private void saveDivisionCache(String divisionKey, String division){
        EmblEntryValidationPlan.divisionCache.put(divisionKey, division);
    }
    
    private boolean shouldSetDivision(Entry entry){
        if(empty(entry.getDivision())) {
            return true;
        }else {
            // If division is NOT empty
            if (getValidationScope().equals(ValidationScope.NCBI) || getValidationScope().equals(ValidationScope.NCBI_MASTER)){
                // Do NOT set division for NCBI or NCBI_MASTER ValidationScope.
                return false;
            }else{
                // Set division for non NCBI ValidationScope(s).
                return true;
            }
        }
    }
    
    public ValidationScope getValidationScope(){
        return getEmblEntryValidationPlanProperty().validationScope.get();
    }

    public static boolean empty(String input) {
        return StringUtils.isEmpty(input);
    }
}
