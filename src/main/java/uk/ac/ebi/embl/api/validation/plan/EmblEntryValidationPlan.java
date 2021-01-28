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
package uk.ac.ebi.embl.api.validation.plan;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.EmblEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureTranslationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureLocationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceValidationCheck;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.ChromosomeSourceQualifierCheck;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class EmblEntryValidationPlan extends ValidationPlan
{
    
	public EmblEntryValidationPlan(EmblEntryValidationPlanProperty planProperty)
	{
		super(planProperty);
	}

	private ValidationPlanResult execute(Entry entry) throws ValidationEngineException {
		List<Class<? extends EmblEntryValidationCheck<?>>> checks = new ArrayList<>();
		List<Class<? extends EmblEntryValidationCheck<?>>> fixes = new ArrayList<>();
		validatePlanProperty();
		
		checks.addAll(ValidationUnit.SEQUENCE_ENTRY_CHECKS.getValidationUnit());
		if (planProperty.isFixMode.get())
		{
			fixes.addAll(ValidationUnit.SEQUENCE_ENTRY_FIXES.getValidationUnit());
		}

		executeChecksandFixes(fixes,entry);
		executeChecksandFixes(checks,entry);

		return validationPlanResult;

	}

	void validatePlanProperty() throws ValidationEngineException
	{
		if (planProperty == null)
		{
			throw new ValidationEngineException("EmblEntryValidationPlanProperty must not be null");
		}
		
	}

	@Override
	public ValidationPlanResult execute(Object target) throws ValidationEngineException
	{
		validationPlanResult = new ValidationPlanResult();
		// TODO Auto-generated method stub
		if(target instanceof Entry)
		{
			execute((Entry)target);
		}
		return validationPlanResult;
	}
	
	private void executeChecksandFixes(List<Class<? extends EmblEntryValidationCheck<?>>> checks,Entry entry) throws ValidationEngineException
	{

		for (Class<? extends EmblEntryValidationCheck<?>> validationCheck : checks) {
			EmblEntryValidationCheck<?> check;
			try {
				check = (EmblEntryValidationCheck<?>) validationCheck.getConstructor((Class[]) null).newInstance((Object[]) null);
			} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				ValidationEngineException ex = new ValidationEngineException(e);
				ex.setErrorType(ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
				throw ex;
			}
			if (check instanceof SequenceValidationCheck) {
				execute(check, entry.getSequence());
			}
			if (check instanceof EntryValidationCheck) {
				execute((EntryValidationCheck) check, entry);
			}
			if (check instanceof FeatureValidationCheck) {
				for (Feature feature : entry.getFeatures()) {
					if (check instanceof CdsFeatureTranslationCheck) {
						((CdsFeatureTranslationCheck) check).setEntry(entry);
					}
					if (check instanceof ChromosomeSourceQualifierCheck) {
						((ChromosomeSourceQualifierCheck) check).setEntry(entry);
					}
					execute((FeatureValidationCheck) check, feature);
				}
			}
		}

	}
}
