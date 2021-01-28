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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.List;


import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("Qualifier {0} is only permitted when organism belongs to {1}.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class OrganismAndPermittedQualifierCheck extends EntryValidationCheck {

    Pattern genePattern =Pattern.compile("^(.)*(rrna)$");
    Pattern productPattern=Pattern.compile("^(.)*(ribosomal rna)$");

	private final static String MESSAGE_ID_1 = "OrganismAndPermittedQualifierCheck1";
	private final static String MESSAGE_ID_2 = "OrganismAndPermittedQualifierCheck3";
	private final static String PROKARYOTIC_MESSAGE = "OrganismAndPermittedQualifierCheck2";


	public OrganismAndPermittedQualifierCheck()
	{
	}

	public ValidationResult check(Entry entry) {

        result = new ValidationResult();
		DataSet dataSet = GlobalDataSets.getDataSet(FileName.ORG_PERMITTED_QUALIFIER);

        if (entry == null) {
			return result;
		}
        
        SourceFeature source=entry.getPrimarySourceFeature();
        if(source==null)
        	return result;

		Qualifier organismQualifier = source.getSingleQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);
        for (DataRow dataRow : dataSet.getRows()) {
            String permittedQualifier = dataRow.getString(0);
            String[] organisms = dataRow.getStringArray(1);
            if (organisms == null || organisms.length == 0 || permittedQualifier == null) {
                continue;
            }

            if (!SequenceEntryUtils.isQualifierAvailable(permittedQualifier, source)) {
                continue;//on to the next one
            }

           
            if (organismQualifier == null) {
                reportError(source, organisms, permittedQualifier);
                continue;
            }

            String organism = organismQualifier.getValue();
            if (organism == null) {
                reportError(source, organisms, permittedQualifier);
                continue;
            }

            if (getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismValid(organism) && !getEmblEntryValidationPlanProperty().taxonHelper.get().isChildOfAny(organism, organisms)) {
            	reportError(source.getOrigin(),MESSAGE_ID_2 , organism , Utils.paramArrayToString(organisms),permittedQualifier, Utils.paramArrayToString(organisms));
                }
            
        }
		if (organismQualifier != null
				&& getEmblEntryValidationPlanProperty().taxonHelper.get().isOrganismValid(organismQualifier.getValue())
				&& (getEmblEntryValidationPlanProperty().taxonHelper.get().isChildOf(organismQualifier.getValue(),"Bacteria")
				|| getEmblEntryValidationPlanProperty().taxonHelper.get().isChildOf(organismQualifier.getValue(),"Archaea"))
				&& getEmblEntryValidationPlanProperty().validationScope.get().equals(ValidationScope.EMBL_TEMPLATE)) {

			List<Qualifier> geneQualifiers=SequenceEntryUtils.getQualifiers(Qualifier.GENE_QUALIFIER_NAME, entry);
			List<Qualifier> productQualifiers =SequenceEntryUtils.getQualifiers(Qualifier.PRODUCT_QUALIFIER_NAME, entry);
			boolean matchGene = false;
			boolean matchProduct = false;
			if (geneQualifiers.size() != 0 || productQualifiers.size() != 0)
			{
				String geneValue;
				String productValue;
				for (Qualifier geneQualifier : geneQualifiers)
				{
					geneValue = geneQualifier.getValue();
					if (geneValue != null && !genePattern.matcher(geneValue.toLowerCase()).matches())
					{
						reportWarning(source.getOrigin(), PROKARYOTIC_MESSAGE);
						return result;
					}
                   matchGene=true;
				}
				for (Qualifier productQualifier : productQualifiers)
				{
					productValue = productQualifier.getValue();
					if (productValue != null && !productPattern.matcher(productValue.toLowerCase()).matches())
					{
						reportWarning(source.getOrigin(), PROKARYOTIC_MESSAGE);
						return result;
					}
					matchProduct=true;
				}

			}
			if(!matchGene&&!matchProduct)
			reportWarning(source.getOrigin(), PROKARYOTIC_MESSAGE);
		}

        return result;
	}

	private void reportError(Feature feature, String[] organisms, String permittedQualifier) {
        reportError(feature.getOrigin(), MESSAGE_ID_1, permittedQualifier, Utils.paramArrayToString(organisms));
	}

}
