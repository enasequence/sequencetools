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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.embl.api.entry.genomeassembly.GCSEntry;
import uk.ac.ebi.embl.api.validation.EmblEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.GenomeAssemblyValidationCheck;

public class GenomeAssemblyValidationPlan extends ValidationPlan
{
	public GenomeAssemblyValidationPlan(EmblEntryValidationPlanProperty planProperty)
	{
		super(planProperty);
	}

	private ValidationPlanResult execute(GCSEntry entry) throws ValidationEngineException
	{
		List<Class<? extends GenomeAssemblyValidationCheck<?>>> checks = new ArrayList<Class<? extends GenomeAssemblyValidationCheck<?>>>();
		List<Class<? extends GenomeAssemblyValidationCheck<?>>> fixes = new ArrayList<Class<? extends GenomeAssemblyValidationCheck<?>>>();
		validatePlanProperty();
		switch (planProperty.fileType.get())
		{
		case ASSEMBLYINFO:
			checks.addAll(GenomeAssemblyValidationUnit.ASSEMBLYINFO_CHECKS.getValidationUnit());
			if (planProperty.isFixMode.get())
 				{
				
 				}

			break;
		case CHROMOSOMELIST:

			checks.addAll(GenomeAssemblyValidationUnit.CHROMOSOME_LIST_CHECKS.getValidationUnit());
			if (planProperty.isFixMode.get())
 				{
				
 				}

			break;
		case UNLOCALISEDLIST:
			checks.addAll(GenomeAssemblyValidationUnit.UNLOCALISED_LIST_CHECKS.getValidationUnit());
			if (planProperty.isFixMode.get())
 				{
				
 				}

			break;
		default:
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
		if(target instanceof GCSEntry)
		{
			execute((GCSEntry)target);
		}
		return validationPlanResult;
	}
	
	private void executeChecksandFixes(List<Class<? extends GenomeAssemblyValidationCheck<?>>> checks,GCSEntry entry) throws ValidationEngineException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		for (Class<? extends GenomeAssemblyValidationCheck<?>> validationCheck : checks)
		{
			EmblEntryValidationCheck<?> check = (EmblEntryValidationCheck<?>) validationCheck.getConstructor((Class[]) null).newInstance((Object[]) null);
			if (check instanceof GenomeAssemblyValidationCheck)
			{
				execute(check,entry);
			}
		}
	}

}
