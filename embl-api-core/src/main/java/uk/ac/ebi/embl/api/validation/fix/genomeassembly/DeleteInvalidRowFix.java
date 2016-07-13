/*
 * # Copyright 2012-2012 EMBL-EBI, Hinxton outstation
 *
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 *
# http://www.apache.org/licenses/LICENSE-2.0
 *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.fix.genomeassembly;

import java.util.ArrayList;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRow;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.GenomeAssemblyValidationCheck;

public class DeleteInvalidRowFix extends GenomeAssemblyValidationCheck
{
	private static final String Delete_Row_ID_FIX = "deleteInvalidRowFix_1";

	@Override
	public ValidationResult check(GenomeAssemblyRecord gaRecord)
	{
		result = new ValidationResult();
		if (gaRecord == null)
		{
			return result;
		}
		@SuppressWarnings("unchecked")
		ArrayList<GenomeAssemblyRow> gaRows = (ArrayList<GenomeAssemblyRow>) gaRecord.getFields();
		ArrayList<GenomeAssemblyRow> invalidRows = new ArrayList<GenomeAssemblyRow>();
		for (GenomeAssemblyRow gaRow : gaRows)
		{
			if (!gaRow.isValid())
			{
				invalidRows.add(gaRow);
			}
		}
		for (GenomeAssemblyRow invalidRow : invalidRows)
		{
			reportMessage(Severity.FIX, invalidRow.getOrigin(), Delete_Row_ID_FIX);
			gaRecord.deleteField(invalidRow);
		}

		return result;
	}

}
