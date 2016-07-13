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

public abstract class Validator {
	
	/**
	 * Validates an entry and all its elements (sequence, features etc.).
	 * 
	 * @param entry an entry to be checked 
	 * @return a validation result
	 * "throws ValidationEngineException
	 */
	public ValidationPlanResult validateAll(final Entry entry)
			throws ValidationEngineException {
		final ValidationPlanResult result = new ValidationPlanResult();
		if (entry == null) {
			return result;
		}

		//validate entry
		result.append(validate(entry));

		//validate sequence
		result.append(validate(entry.getSequence()));

		return result;
	}
	
	

	/**
	 * Validates an entry.
	 * 
	 * @param entry an entry to be checked 
	 * @return a validation result
	 * @throws ValidationEngineException
	 */
	public abstract ValidationPlanResult validate(final Entry entry)
			throws ValidationEngineException;

	/**
	 * Validates a sequence.
	 * 
	 * @param sequence a sequence to be checked
	 * @return a validation result
	 * @throws ValidationEngineException
	 */
	public abstract ValidationPlanResult validate(final Sequence sequence)
			throws ValidationEngineException;

	/**
	 * Validates a feature.
	 * 
	 * @param feature a feature to be checked
	 * @return a validation result
	 * @throws ValidationEngineException
	 */
	public abstract ValidationPlanResult validate(final Feature feature) throws ValidationEngineException;

    public abstract void addMessageBundle(String bundlename);

}
