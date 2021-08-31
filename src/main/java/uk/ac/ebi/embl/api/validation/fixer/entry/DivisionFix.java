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
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClientImpl;
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

        if (empty(entry.getDivision())) {

            try {
                SourceFeature primarySF = entry.getPrimarySourceFeature();
                TaxonomyClient taxonomyClient = new TaxonomyClientImpl();

                // Get division using transgenic/pseduo
                String division = (primarySF.isTransgenic()) ? "TGN" : (null != primarySF.getSingleQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME)) ? "ENV" : "";

                // Get division using tax_id
                if (empty(division) && primarySF.getTaxId() != null) {
                    division = taxonomyClient.getTaxonByTaxid(Long.valueOf(primarySF.getTaxId())).getDivision();
                }

                // Get division using Scientific name
                if (empty(division) && primarySF.getScientificName() != null) {
                    List<Taxon> taxonList;
                    if (!(taxonList = taxonomyClient.getTaxonByScientificName(primarySF.getScientificName())).isEmpty()) {
                        division = taxonList.get(0).getDivision();
                    }
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

    public static boolean empty(String input) {
        return StringUtils.isEmpty(input);
    }
}
