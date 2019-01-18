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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.lang.enums.EnumUtils;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;

@Description("Invalid assembly type : {0}")
public class AssemblyInfoTypeCheck extends GenomeAssemblyValidationCheck<AssemblyInfoEntry>
{
	private final String MESSAGE_KEY_ASSEMBLY_TYPE_ERROR = "AssemblyinfoAssemblyTypeCheck";

	@Override
	public ValidationResult check(AssemblyInfoEntry entry) throws ValidationEngineException
	{

		if (entry == null||entry.getAssemblyType()==null)
			return result;
		if(!Arrays.stream(AssemblyType.class.getEnumConstants()).filter(x->entry.getAssemblyType().toUpperCase().equals(x.getValue())).findAny().isPresent())
		{
			reportError(entry.getOrigin(), MESSAGE_KEY_ASSEMBLY_TYPE_ERROR, entry.getAssemblyType());
		}

		return result;
	}
}
