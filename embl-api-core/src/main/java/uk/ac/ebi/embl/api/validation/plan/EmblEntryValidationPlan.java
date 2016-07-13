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
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceValidationCheck;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements validation plan and it hardcodes the order execution.
 * 
 * @author dlorenc
 * 
 */
public class EmblEntryValidationPlan extends ValidationPlan
{

	public EmblEntryValidationPlan(EmblEntryValidationPlanProperty planProperty)
	{
		super(planProperty);

	}

	private ValidationPlanResult execute(Entry entry) throws ValidationEngineException
	{
		List<Class<? extends EmblEntryValidationCheck<?>>> checks = new ArrayList<Class<? extends EmblEntryValidationCheck<?>>>();
		List<Class<? extends EmblEntryValidationCheck<?>>> fixes = new ArrayList<Class<? extends EmblEntryValidationCheck<?>>>();
		validatePlanProperty();
		switch (planProperty.fileType.get())
		{
		case FASTA:
		case AGP:
			checks.addAll(ValidationUnit.SEQUENCE_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.ASSEMBLY_LEVEL_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.ENTRY_SPECIFIC_HEADER_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.FASTA_AGP_FEATURE_CHECKS.getValidationUnit());
			if (planProperty.isFixMode.get())
 				{
				fixes.addAll(ValidationUnit.AGP_SPECIFIC_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.ASSEMBLY_LEVEL_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.ENTRY_SPECIFIC_HEADER_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.SEQUENCE_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.FASTA_AGP_FEATURE_FIXES.getValidationUnit());

 				}

			break;
		case MASTER:

			checks.addAll(ValidationUnit.MASTER_HEADER_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.SOURCE_FEAURES_ONLY_CHECKS.getValidationUnit());
			if (planProperty.isFixMode.get())
			{

				fixes.addAll(ValidationUnit.MASTER_HEADER_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.SOURCE_FEAURES_ONLY_FIXES.getValidationUnit());
			}
			break;
		default:
			checks.addAll(ValidationUnit.SEQUENCE_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.MASTER_HEADER_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.ENTRY_SPECIFIC_HEADER_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.NON_SOURCE_DEPENDSON_SEQUENCE_AND_SOURCE_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.NON_SOURCE_DEPENDSON_SEQUENCE_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.NON_SOURCE_DEPENDSON_SOURCE_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.NON_SOURCE_FEATURES_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.SEQUENCE_DEPENDSON_NON_SOURCE_FEATURES_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.SEQUENCE_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.SOURCE_DEPENDSON_SEQUENCE_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.SOURCE_FEAURES_ONLY_CHECKS.getValidationUnit());
			checks.addAll(ValidationUnit.ASSEMBLY_LEVEL_CHECKS.getValidationUnit());
			if (planProperty.isFixMode.get())
			{
				fixes.addAll(ValidationUnit.MASTER_HEADER_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.ENTRY_SPECIFIC_HEADER_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.ASSEMBLY_LEVEL_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.SOURCE_FEAURES_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.SOURCE_DEPENDSON_SEQUENCE_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.NON_SOURCE_FEATURES_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.NON_SOURCE_DEPENDSON_SOURCE_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.NON_SOURCE_DEPENDSON_SEQUENCE_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.NON_SOURCE_DEPENDSON_SEQUENCE_AND_SOURCE_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.SEQUENCE_ONLY_FIXES.getValidationUnit());
				fixes.addAll(ValidationUnit.SEQUENCE_DEPENDSON_NON_SOURCE_FEATURES_FIXES.getValidationUnit());
			}
			break;

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
					execute((FeatureValidationCheck) check,feature);
				}
			}
		}
	}

}
