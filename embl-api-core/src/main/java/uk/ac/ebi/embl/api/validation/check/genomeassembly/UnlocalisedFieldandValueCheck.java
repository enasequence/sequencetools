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

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.embl.api.genomeassembly.ChromosomeRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.ChromosomeDataRow;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.UnlocalisedDataRow;
import uk.ac.ebi.embl.api.genomeassembly.UnlocalisedRecord;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("")
public class UnlocalisedFieldandValueCheck extends GenomeAssemblyValidationCheck
{

	private final static String MISSING_MANDATORY_ID = "UnlocalisedFieldandValueCheck-1";
	private final static String INVALID_VALUE_ID = "UnlocalisedFieldandValueCheck-2";
	private String object_name, chromosome_name;

	public UnlocalisedFieldandValueCheck()
	{

	}

	public ValidationResult check(GenomeAssemblyRecord unLocalisedRecord)
	{
		result = new ValidationResult();

		if (unLocalisedRecord == null)
		{
			return result;
		}
		@SuppressWarnings("unchecked")
		ArrayList<UnlocalisedDataRow> rows = (ArrayList<UnlocalisedDataRow>) unLocalisedRecord.getFields();

		for (UnlocalisedDataRow row : rows)
		{

			object_name = row.get_object_name();
			chromosome_name = row.get_chromosome_name();
			if (object_name == null)
			{
				reportError(row.getOrigin(), MISSING_MANDATORY_ID, UnlocalisedRecord.OBJECT_NAME_KEYWORD);
			} else if (object_name.split(" ").length > 1)
			{
				reportError(row.getOrigin(), INVALID_VALUE_ID, UnlocalisedRecord.OBJECT_NAME_KEYWORD, object_name);
			}
			if (chromosome_name == null)
			{
				reportError(row.getOrigin(), MISSING_MANDATORY_ID, UnlocalisedRecord.CHROMOSOME_NAME_KEYWORD);
			} else if (StringUtils.indexOfAny(chromosome_name, UnlocalisedRecord.INVALID_CHNAME_VALUES) != -1)
			{
				reportError(row.getOrigin(), INVALID_VALUE_ID, UnlocalisedRecord.CHROMOSOME_NAME_KEYWORD, chromosome_name);
			}

		}
		return result;
	}
}
