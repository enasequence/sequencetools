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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class ValidatorImpl extends Validator {
	
	private EmblEntryValidationPlan plan;

	public ValidatorImpl(ValidationScope validationScope,
                         boolean devMode,
                         boolean fix) {
	  
		EmblEntryValidationPlanProperty emblEntryValidationProperty = new EmblEntryValidationPlanProperty();
		emblEntryValidationProperty.validationScope.set(validationScope);
		emblEntryValidationProperty.isDevMode.set(devMode);
		emblEntryValidationProperty.isFixMode.set(fix);
		this.plan = new EmblEntryValidationPlan(emblEntryValidationProperty);
	}

    public void addMessageBundle(String bundlename){
        this.plan.addMessageBundle(bundlename);
    }

    public ValidationPlanResult validate(Entry entry)
			throws ValidationEngineException {
        return plan.execute(entry);
	}

	public ValidationPlanResult validate(Sequence sequence) 
			throws ValidationEngineException {
		return plan.execute(sequence);
	}

	public ValidationPlanResult validate(Feature feature)
			throws ValidationEngineException {
		return plan.execute(feature);
	}

}
