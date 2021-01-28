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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

import java.sql.SQLException;
import java.util.List;

@Description("invalid protein_id {0}: protein_id can only be assigned by EMBL")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class ProteinIdExistsCheck extends EntryValidationCheck {

	private final static String MESSAGE_ID = "ProteinIdExistsCheck_1";

	public ProteinIdExistsCheck() {
	}

	public ValidationResult check(Entry entry) {
		result = new ValidationResult();

		if (entry == null || entry.getFeatures() == null
				|| entry.getFeatures().size() == 0) {
			return result;
		}
		int assemblyLevel = ValidationScope.ASSEMBLY_CONTIG.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 0 : ValidationScope.ASSEMBLY_SCAFFOLD.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 1 : ValidationScope.ASSEMBLY_CHROMOSOME.equals(getEmblEntryValidationPlanProperty().validationScope.get()) ? 2 :-1;

		if(getEmblEntryValidationPlanProperty().analysis_id.get()==null||assemblyLevel==-1)
		{
			return result;
		}

			for (Feature feature : entry.getFeatures()) {

				if (feature.getQualifiers(Qualifier.PROTEIN_ID_QUALIFIER_NAME) != null
						& feature.getQualifiers(
								Qualifier.PROTEIN_ID_QUALIFIER_NAME).size() != 0) {
					List<Qualifier> protein_idQualifiers=feature.getQualifiers(Qualifier.PROTEIN_ID_QUALIFIER_NAME);
					for(Qualifier qualifier:protein_idQualifiers)
					reportError(feature.getOrigin(), MESSAGE_ID, qualifier.getValue());
				}
			}
				return result;
	}
}
