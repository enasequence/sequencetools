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
package uk.ac.ebi.embl.api.validation;

import java.sql.SQLException;
import java.util.List;

import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

/**
 * An interface for the validation checks.
 */
public interface ValidationCheck<E> {
	
	/**
	 * Validates object.
	 * 
	 * @param object an object to be validated
	 * @return the validation result
	 * @throws ValidationEngineException 
	 */
	ValidationResult check(E object) throws ValidationEngineException;

    /**
     * initializes the object (may be empty)
     */
    void setPopulated();

    boolean isPopulated();
    
    void setEmblEntryValidationPlanProperty(EmblEntryValidationPlanProperty property) throws SQLException;
    
    EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty();
    
    void setEntryDAOUtils(EntryDAOUtils daoUtils);
    
    EntryDAOUtils getEntryDAOUtils();

	EraproDAOUtils getEraproDAOUtils();
	
    void setEraproDAOUtils(EraproDAOUtils daoUtils);


}
