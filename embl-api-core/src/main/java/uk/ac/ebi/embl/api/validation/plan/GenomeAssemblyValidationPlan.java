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

import java.sql.Connection;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyFieldandValueCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeFieldandValueCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.UnlocalisedFieldandValueCheck;
import uk.ac.ebi.embl.api.validation.fix.genomeassembly.DeleteInvalidRowFix;

public class GenomeAssemblyValidationPlan extends ValidationPlan
{
	private boolean FIX_MODE = false;// default
	// Assembly File Checks
	private AssemblyFieldandValueCheck assemblyFieldandValueCheck=new AssemblyFieldandValueCheck();;
	private ChromosomeFieldandValueCheck chromosomeFieldandValueCheck = new ChromosomeFieldandValueCheck();
	private UnlocalisedFieldandValueCheck unlocalisedFieldandValueCheck = new UnlocalisedFieldandValueCheck();
	// Assembly File Fixes
	private DeleteInvalidRowFix deleteInvalidRowFix = new DeleteInvalidRowFix();

	public GenomeAssemblyValidationPlan(EmblEntryValidationPlanProperty planProperty)
	{
		super(planProperty);
	}

	@Override
	public ValidationPlanResult execute(Object target) throws ValidationEngineException
	{

		validationPlanResult = new ValidationPlanResult();

		if (target instanceof GenomeAssemblyRecord)
		{
			GenomeAssemblyRecord gaRecord = (GenomeAssemblyRecord) target;
			execute(gaRecord);
		}

		return validationPlanResult;
	}

	public void execute(GenomeAssemblyRecord gaRecord) throws ValidationEngineException
	{
		if (planProperty.isFixMode.get())
		{
			executeFixes(gaRecord);
		}

		executeChecks(gaRecord);
	}

	public void executeChecks(GenomeAssemblyRecord gaRecord) throws ValidationEngineException
	{
		// Assembly File checks
		if (gaRecord.isAssembly())
		{
			execute(assemblyFieldandValueCheck, gaRecord);
		}
		// Chromosome File checks
		if (gaRecord.isChromosome())
		{
			execute(chromosomeFieldandValueCheck, gaRecord);
		}
		// Unlocalised File checks
		if (gaRecord.isUnLocalised())
		{
			execute(unlocalisedFieldandValueCheck, gaRecord);
		}
	}

	public void executeFixes(GenomeAssemblyRecord gaRecord) throws ValidationEngineException
	{
		execute(deleteInvalidRowFix, gaRecord);
	}

	
}
