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
package uk.ac.ebi.embl.api.validation.check.sequence;

import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public abstract class SequenceValidationCheck implements EmblEntryValidationCheck<Sequence> {

	private Origin origin;
    private boolean isPopulated;
    private EmblEntryValidationPlanProperty property;
	private EntryDAOUtils entryDAOUtils;
	private EraproDAOUtils eraproDAOUtils;

    
//	protected ValidationResult result;

	public SequenceValidationCheck() {
//		result = new ValidationResult();
	}

    public void init() {
        //no implementation - override when needed
    }

    public boolean isPopulated() {
        return isPopulated;
    }

    public void setPopulated() {
        //override to do any processing of data post-population
        isPopulated = true;
    }

    public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}

	/**
	 * Creates an error validation message for the sequence and adds it to 
	 * the validation result. 
	 * 
	 * @param messageKey a message key
	 * @param params message parameters
	 */	
	protected void reportError(ValidationResult result, String messageKey, 
			Object... params) {
		reportMessage(result, Severity.ERROR, messageKey, params);		
	}	

	/**
	 * Creates a warning validation message for the sequence and adds it to 
	 * the validation result. 
	 * 
	 * @param messageKey a message key
	 * @param params message parameters
	 */
	protected void reportWarning(ValidationResult result, String messageKey, 
			Object... params) {
		reportMessage(result, Severity.WARNING, messageKey, params);
	}

	/**
	 * Creates a validation message for the sequence and adds it to 
	 * the validation result.
	 * 
	 * @param severity message severity
	 * @param messageKey a message key
	 * @param params message parameters
	 */
    protected void reportMessage(ValidationResult result, Severity severity,
                                 String messageKey, Object... params) {
        result.append(EntryValidations.createMessage(origin, severity, messageKey, params));
    }
    
    @Override
   	public void setEmblEntryValidationPlanProperty(
   			EmblEntryValidationPlanProperty property) throws SQLException
   	{
   		this.property=property;
   	}

   	@Override
   	public EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty()
   	{
   		return property;
   	}

   	@Override
   	public void setEntryDAOUtils(EntryDAOUtils entryDAOUtils)
   	{
   		this.entryDAOUtils=entryDAOUtils;
   		
   	}

   	@Override
   	public EntryDAOUtils getEntryDAOUtils()
   	{
   		// TODO Auto-generated method stub
   		return entryDAOUtils;
   	}
       
   	@Override
	public EraproDAOUtils getEraproDAOUtils()
	{
		// TODO Auto-generated method stub
		return eraproDAOUtils;
	}
    
	@Override
	public void setEraproDAOUtils(EraproDAOUtils eraproDAOUtils)
	{
		this.eraproDAOUtils=eraproDAOUtils;
		
	}

}
