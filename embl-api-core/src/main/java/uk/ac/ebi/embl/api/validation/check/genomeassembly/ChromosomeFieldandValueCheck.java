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
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("")
public class ChromosomeFieldandValueCheck extends GenomeAssemblyValidationCheck
{

	@CheckDataSet("chromosome-location-keywords.tsv")
	private DataSet locationSet;
	private final static String MISSING_MANDATORY_ID = "ChromosomeFieldandValueCheck-1";
	private final static String INVALID_VALUE_ID = "ChromosomeFieldandValueCheck-2";
	private final static String INVALID_ROW_ID = "ChromosomeFieldandValueCheck-3";
	private Set<String> locations = new TreeSet<String>();
	private String object_name, chromosome_name, type, location;

	public ChromosomeFieldandValueCheck()
	{

	}

	public ChromosomeFieldandValueCheck(DataSet dataSet)
	{
		this.locationSet = dataSet;
	}

	public ValidationResult check(GenomeAssemblyRecord chromosomeRecord)
	{
		result = new ValidationResult();

		if (chromosomeRecord == null)
		{
			return result;
		}
		@SuppressWarnings("unchecked")
		ArrayList<ChromosomeDataRow> rows = (ArrayList<ChromosomeDataRow>) chromosomeRecord.getFields();

		for (DataRow dataRow : locationSet.getRows())
		{
			String location = Utils.parseTSVString(dataRow.getString(0));
			locations.add(location);
		}
		for (ChromosomeDataRow row : rows)
		{

			object_name = row.get_object_name();
			chromosome_name = row.get_chromosome_name();
			type = row.get_type();
			location = row.get_location();
			if (!row.isValid())
			{
				reportError(row.getOrigin(), INVALID_ROW_ID);
				continue;
			}
			if (object_name == null || chromosome_name == null || type == null)
			{
				reportError(row.getOrigin(), MISSING_MANDATORY_ID);
			}

			if (object_name != null && object_name.split(" ").length > 1)
			{
				reportError(row.getOrigin(), INVALID_VALUE_ID, ChromosomeRecord.OBJECT_NAME_KEYWORD, object_name);
			}
			if (chromosome_name != null && StringUtils.indexOfAny(chromosome_name.toLowerCase(), ChromosomeRecord.INVALID_CHNAME_VALUES) != -1)
			{
				reportError(row.getOrigin(), INVALID_VALUE_ID, ChromosomeRecord.CHROMOSOME_NAME_KEYWORD, chromosome_name);
			}
			if (type != null && StringUtils.indexOfAny(type.toLowerCase(), ChromosomeRecord.VALID_TYPE_VALUE) == -1)
			{
				reportError(row.getOrigin(), INVALID_VALUE_ID, ChromosomeRecord.TYPE_KEYWORD, type);
			}

			if (location != null && !ArrayUtils.contains(locations.toArray(), location.toLowerCase()))
			{
				reportError(row.getOrigin(), INVALID_VALUE_ID, ChromosomeRecord.LOCATION_KEYWORD, location);
			}
		}
		return result;
	}
}
