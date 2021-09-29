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
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureTranslationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureLocationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceValidationCheck;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.ChromosomeSourceQualifierCheck;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EmblEntryValidationPlan extends ValidationPlan
{
	int assemblySeqnumber=1;
	public static HashMap<String, String> divisionCache = new HashMap<>();
    
	public EmblEntryValidationPlan(EmblEntryValidationPlanProperty planProperty)
	{
		super(planProperty);
	}

	private ValidationResult execute(Entry entry) throws ValidationEngineException
	{
		List<Class<? extends EmblEntryValidationCheck<?>>> checks = new ArrayList<Class<? extends EmblEntryValidationCheck<?>>>();
		List<Class<? extends EmblEntryValidationCheck<?>>> fixes = new ArrayList<Class<? extends EmblEntryValidationCheck<?>>>();
		validatePlanProperty();
		if(planProperty.isSourceUpdate.get()) {
			checks.addAll(ValidationUnit.SOURCE_FEATURE_CHECKS.getValidationUnit());
			fixes.addAll(ValidationUnit.SOURCE_FEATURE_FIXES.getValidationUnit());
		} else {
			checks.addAll(ValidationUnit.SEQUENCE_ENTRY_CHECKS.getValidationUnit());
			if (planProperty.isFixMode.get()) {
				fixes.addAll(ValidationUnit.SEQUENCE_ENTRY_FIXES.getValidationUnit());
			}
		}

		try
		{
			executeChecksandFixes(fixes,entry);
			executeChecksandFixes(checks,entry);
		}
		catch (Exception e)
		{
			throw new ValidationEngineException(e);
		}

		return validationResult;

	}

	void validatePlanProperty() throws ValidationEngineException
	{
		if (planProperty == null)
		{
			throw new ValidationEngineException("EmblEntryValidationPlanProperty must not be null");
		}
		
	}

	@Override
	public ValidationResult execute(Object target) throws ValidationEngineException
	{
		validationResult = new ValidationResult();
		// TODO Auto-generated method stub
		if(target instanceof Entry)
		{
			execute((Entry)target);
		}
		return validationResult;
	}
	
	private void executeChecksandFixes(List<Class<? extends EmblEntryValidationCheck<?>>> checks,Entry entry) throws ValidationEngineException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		for (Class<? extends EmblEntryValidationCheck<?>> validationCheck : checks)
		{
			EmblEntryValidationCheck<?> check = (EmblEntryValidationCheck<?>) validationCheck.getConstructor((Class[]) null).newInstance((Object[]) null);
			if (check instanceof SequenceValidationCheck)
			{
				execute(check,entry.getSequence());
			}
			if (check instanceof EntryValidationCheck)
			{
				execute((EntryValidationCheck) check,entry);
			}
			if (check instanceof FeatureValidationCheck)
			{
				for (Feature feature : entry.getFeatures())
				{ 
				 if(check instanceof CdsFeatureTranslationCheck)
				 {
					 ((CdsFeatureTranslationCheck) check).setEntry(entry);
				 }
				 if(check instanceof FeatureLocationCheck)
				 {
					 ((FeatureLocationCheck) check).setEntry(entry);
				 }
				 if(check instanceof ChromosomeSourceQualifierCheck)
				 {
					 ((ChromosomeSourceQualifierCheck) check).setEntry(entry);
				 }
					execute((FeatureValidationCheck) check,feature);
				}
			}
		}
	}
}
